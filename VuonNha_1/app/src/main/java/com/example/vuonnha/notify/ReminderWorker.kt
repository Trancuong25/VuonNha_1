package com.example.vuonnha.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.vuonnha.data.AppDb
import com.example.vuonnha.domain.daysLeft
import com.example.vuonnha.domain.startOfDay
import com.example.vuonnha.ui.MainActivity
import java.util.Calendar
import java.util.concurrent.TimeUnit

object Notifications {
    const val CHANNEL = "care_reminders"
    const val NOTIF_ID = 1
    const val ACTION_DONE = "com.example.vuonnha.ACTION_DONE"
    const val ACTION_SNOOZE = "com.example.vuonnha.ACTION_SNOOZE"
    const val EXTRA_IDS = "task_ids"
    private const val WORK = "care_reminder_daily"
    private const val DEFAULT_MINUTES = 7 * 60

    private fun prefs(ctx: Context) = ctx.getSharedPreferences("reminder", Context.MODE_PRIVATE)

    fun setup(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            ctx.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(NotificationChannel(CHANNEL, "Nhắc chăm sóc", NotificationManager.IMPORTANCE_DEFAULT))
        }
        // Bỏ lịch kiểm tra 6 giờ của phiên bản cũ, thay bằng nhắc đúng giờ mỗi ngày.
        WorkManager.getInstance(ctx).cancelUniqueWork("care_reminder")
        scheduleDaily(ctx, ExistingWorkPolicy.KEEP)
    }

    /** Giờ nhắc, tính bằng phút kể từ 00:00 (mặc định 07:00). */
    fun getMinutes(ctx: Context): Int = prefs(ctx).getInt("minutes", DEFAULT_MINUTES)

    fun setMinutes(ctx: Context, minutes: Int) {
        prefs(ctx).edit().putInt("minutes", minutes).apply()
        scheduleDaily(ctx, ExistingWorkPolicy.REPLACE)
    }

    fun scheduleDaily(ctx: Context, policy: ExistingWorkPolicy) {
        val m = getMinutes(ctx)
        val now = System.currentTimeMillis()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, m / 60); set(Calendar.MINUTE, m % 60); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
        val req = OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(next - now, TimeUnit.MILLISECONDS).build()
        WorkManager.getInstance(ctx).enqueueUniqueWork(WORK, policy, req)
    }

    /** Gửi ngay một thông báo thử (bỏ qua giờ và giới hạn mỗi ngày một lần). */
    fun test(ctx: Context) {
        val req = OneTimeWorkRequestBuilder<ReminderWorker>().setInputData(workDataOf("force" to true)).build()
        WorkManager.getInstance(ctx).enqueue(req)
    }
}

class ReminderWorker(ctx: Context, p: WorkerParameters) : CoroutineWorker(ctx, p) {
    override suspend fun doWork(): Result {
        val force = inputData.getBoolean("force", false)
        try { notifyIfNeeded(force) }
        // Hẹn lần nhắc kế tiếp. Dùng APPEND_OR_REPLACE vì REPLACE sẽ huỷ chính worker đang chạy.
        finally { if (!force) Notifications.scheduleDaily(applicationContext, ExistingWorkPolicy.APPEND_OR_REPLACE) }
        return Result.success()
    }

    @SuppressLint("MissingPermission")
    private suspend fun notifyIfNeeded(force: Boolean) {
        val prefs = applicationContext.getSharedPreferences("reminder", Context.MODE_PRIVATE)
        val today = startOfDay()
        if (!force && prefs.getLong("last", 0L) == today) return

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val dao = AppDb.get(applicationContext).dao()
        val items = dao.itemsOnce().associateBy { it.id }
        val due = dao.tasksOnce().filter { it.daysLeft() <= 0 && items.containsKey(it.itemId) }
        if (due.isEmpty() && !force) return

        val title = if (due.isEmpty()) "Thông báo thử" else "Hôm nay có ${due.size} việc chăm sóc"
        // Chỉ những việc được liệt kê trong thông báo mới bị nút "Xong" / "Hoãn" tác động.
        val shown = due.take(3)
        val ids = shown.map { it.id }.toLongArray()
        val text = if (due.isEmpty()) "Chưa có việc nào đến hạn, thông báo hoạt động bình thường."
        else shown.joinToString("; ") { "${it.name} – ${items[it.itemId]?.name}" } + if (due.size > 3) " …" else ""

        val open = PendingIntent.getActivity(
            applicationContext, 0, Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        fun action(code: Int, name: String) = PendingIntent.getBroadcast(
            applicationContext, code,
            Intent(applicationContext, ReminderActionReceiver::class.java).setAction(name).putExtra(Notifications.EXTRA_IDS, ids),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val b = NotificationCompat.Builder(applicationContext, Notifications.CHANNEL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title).setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open).setAutoCancel(true)
        if (due.isNotEmpty()) {
            b.addAction(0, if (shown.size == 1) "Đã làm xong" else "Xong ${shown.size} việc", action(1, Notifications.ACTION_DONE))
            b.addAction(0, "Hoãn 1 ngày", action(2, Notifications.ACTION_SNOOZE))
        }
        NotificationManagerCompat.from(applicationContext).notify(Notifications.NOTIF_ID, b.build())
        if (!force) prefs.edit().putLong("last", today).apply()
    }
}
