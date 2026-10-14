package com.example.vuonnha.domain

import com.example.vuonnha.data.CareTask
import java.util.Calendar
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

// MEMBER 2: logic lịch nhắc thuần (không UI) -> dễ viết unit test.
const val DAY = 86_400_000L
fun startOfDay(t: Long = System.currentTimeMillis()): Long =
    Calendar.getInstance().apply { timeInMillis = t; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
private fun localDate(t: Long) = Instant.ofEpochMilli(t).atZone(ZoneId.systemDefault()).toLocalDate()
private fun addDays(t: Long, days: Int) = localDate(t).plusDays(days.toLong())
    .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
fun CareTask.dueAt() = maxOf(addDays(lastDone, everyDays), snoozeUntil)
fun CareTask.daysLeft(now: Long = System.currentTimeMillis()) =
    ChronoUnit.DAYS.between(localDate(now), localDate(dueAt())).toInt()
fun CareTask.completed(now: Long = System.currentTimeMillis()) = copy(lastDone = startOfDay(now), snoozeUntil = 0)
fun CareTask.snoozed(days: Int = 1, now: Long = System.currentTimeMillis()): CareTask {
    require(days > 0)
    return copy(snoozeUntil = addDays(maxOf(startOfDay(now), dueAt()), days))
}
