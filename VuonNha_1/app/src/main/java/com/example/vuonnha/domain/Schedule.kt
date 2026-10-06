package com.example.vuonnha.domain

import com.example.vuonnha.data.CareTask
import java.util.Calendar

// MEMBER 2: logic lịch nhắc thuần (không UI) -> dễ viết unit test.
const val DAY = 86_400_000L
fun startOfDay(t: Long = System.currentTimeMillis()): Long =
    Calendar.getInstance().apply { timeInMillis = t; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
fun CareTask.dueAt() = maxOf(lastDone + everyDays * DAY, snoozeUntil)
fun CareTask.daysLeft() = ((dueAt() - startOfDay()) / DAY).toInt()
fun CareTask.completed() = copy(lastDone = startOfDay(), snoozeUntil = 0)
fun CareTask.snoozed(days: Int = 1) = copy(snoozeUntil = maxOf(startOfDay(), dueAt()) + days * DAY)
// TODO(M2): đặt thông báo bằng AlarmManager/WorkManager + NotificationChannel; xin quyền POST_NOTIFICATIONS.
