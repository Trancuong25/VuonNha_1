package com.example.vuonnha.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.vuonnha.data.AppDb
import com.example.vuonnha.data.CareRepository
import com.example.vuonnha.domain.daysLeft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Xử lý nút "Đã làm xong" / "Hoãn 1 ngày" bấm thẳng trên thông báo, không cần mở app. */
class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Notifications.ACTION_DONE && action != Notifications.ACTION_SNOOZE) return
        val app = ctx.applicationContext
        // Chỉ xử lý đúng các việc đã hiện trong thông báo (tránh đánh dấu nhầm việc khác).
        val ids = intent.getLongArrayExtra(Notifications.EXTRA_IDS)?.toSet() ?: emptySet()
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = CareRepository.get(app)
                val due = AppDb.get(app).dao().tasksOnce().filter { it.id in ids && it.daysLeft() <= 0 }
                due.forEach { if (action == Notifications.ACTION_DONE) repo.done(it) else repo.snooze(it) }
                NotificationManagerCompat.from(app).cancel(Notifications.NOTIF_ID)
            } finally {
                pending.finish()
            }
        }
    }
}
