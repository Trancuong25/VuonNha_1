package com.example.vuonnha

import android.app.Notification
import android.app.NotificationManager
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.vuonnha.data.*
import com.example.vuonnha.domain.startOfDay
import com.example.vuonnha.notify.Notifications
import com.example.vuonnha.notify.ReminderWorker
import com.example.vuonnha.ui.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Real WorkManager and Android notification, explicitly on the isolated AVD.
 * Does not prove delivery under overnight Doze or device vendor restrictions. */
class NotificationDeliveryAuditTest {
    @Test fun dueCareNotificationArrivesWithActivityInBackground() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("deliveryAudit") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val dao = AppDb.get(context).dao()
        assumeTrue("Use only the isolated empty audit emulator", dao.itemsOnce().isEmpty())
        val prefs = context.getSharedPreferences("reminder", android.content.Context.MODE_PRIVATE)
        val hadLast = prefs.contains("last"); val oldLast = prefs.getLong("last", 0)
        val manager = context.getSystemService(NotificationManager::class.java)
        val itemId = dao.insertItem(Item(name = "Kiểm thử nhắc việc", speciesId = customSpecies("Kim tiền", SpeciesGroup.PLANT).id))
        val request = OneTimeWorkRequestBuilder<ReminderWorker>().build()
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            dao.insertTask(CareTask(itemId = itemId, name = "Tưới nước", everyDays = 1, lastDone = startOfDay() - 3 * 86400000L))
            prefs.edit().remove("last").commit()
            instrumentation.uiAutomation.executeShellCommand("input keyevent KEYCODE_HOME").close()
            WorkManager.getInstance(context).enqueue(request).result.get()
            val deadline = SystemClock.elapsedRealtime() + 60000
            while (SystemClock.elapsedRealtime() < deadline && manager.activeNotifications.none { it.id == Notifications.NOTIF_ID }) SystemClock.sleep(500)
            val notification = manager.activeNotifications.firstOrNull { it.id == Notifications.NOTIF_ID }?.notification
            assertNotNull("Expected real background care notification", notification)
            assertTrue(requireNotNull(notification).extras.getCharSequence(Notification.EXTRA_TEXT).toString().contains("Kiểm thử nhắc việc"))
            assertEquals(2, notification.actions.size)
            while (SystemClock.elapsedRealtime() < deadline && WorkManager.getInstance(context).getWorkInfoById(request.id).get()?.state?.isFinished != true) SystemClock.sleep(200)
            assertEquals(androidx.work.WorkInfo.State.SUCCEEDED, WorkManager.getInstance(context).getWorkInfoById(request.id).get()?.state)
        } finally {
            WorkManager.getInstance(context).cancelWorkById(request.id).result.get()
            WorkManager.getInstance(context).cancelUniqueWork("care_reminder_daily").result.get()
            dao.deleteItem(itemId)
            manager.cancel(Notifications.NOTIF_ID)
            if (hadLast) prefs.edit().putLong("last", oldLast).commit() else prefs.edit().remove("last").commit()
            Notifications.setup(context)
            scenario.close()
        }
    }
}
