package com.example.vuonnha.domain

import com.example.vuonnha.data.CareTask
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun dayOf(timestamp: Long): LocalDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
fun LocalDate.timestamp(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

/** Encode a chosen first reminder without changing the database schema. */
fun scheduleStart(date: LocalDate, everyDays: Int, firstReminder: Boolean): Long {
    require(everyDays > 0)
    return (if (firstReminder) date.minusDays(everyDays.toLong()) else date).timestamp()
}

data class PlannedCare(val task: CareTask, val date: LocalDate, val projected: Boolean)

/** Only the next due date is fixed; later dates assume care is completed on time. */
fun CareTask.occurrences(from: LocalDate, through: LocalDate, today: LocalDate): List<PlannedCare> {
    require(everyDays > 0)
    if (through < from) return emptyList()
    val due = dayOf(dueAt())
    val result = mutableListOf<PlannedCare>()
    if (due in from..through) result += PlannedCare(this, due, false)
    // Overdue work stays overdue, rather than inventing repeated missed tasks.
    if (due < today) return result
    var next = due.plusDays(everyDays.toLong())
    if (next < from) {
        val gap = java.time.temporal.ChronoUnit.DAYS.between(next, from)
        next = next.plusDays(((gap + everyDays - 1) / everyDays) * everyDays)
    }
    while (next <= through) {
        result += PlannedCare(this, next, true)
        next = next.plusDays(everyDays.toLong())
    }
    return result
}
