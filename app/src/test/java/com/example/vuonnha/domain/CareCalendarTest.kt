package com.example.vuonnha.domain

import com.example.vuonnha.data.CareTask
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CareCalendarTest {
    private val today = LocalDate.of(2026, 10, 9)
    private fun task(date: LocalDate, cycle: Int) = CareTask(id = 1, itemId = 1, name = "Tưới nước",
        everyDays = cycle, lastDone = scheduleStart(date, cycle, true))

    @Test fun firstReminderMatchesChosenDate() {
        assertEquals(today, dayOf(task(today, 7).dueAt()))
    }
    @Test fun lastCareStartsCycleFromActualDate() {
        val t = task(today, 7).copy(lastDone = scheduleStart(today, 7, false))
        assertEquals(today.plusDays(7), dayOf(t.dueAt()))
    }
    @Test fun repeatsAcrossMonthBoundaryAndMarksProjections() {
        val plans = task(LocalDate.of(2026, 10, 30), 2)
            .occurrences(LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 3), today)
        assertEquals(listOf(LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3)), plans.map { it.date })
        assertEquals(listOf(false, true, true), plans.map { it.projected })
    }
    @Test fun overdueTaskDoesNotInventMissedRepeats() {
        val t = task(today.minusDays(5), 1)
        assertEquals(1, t.occurrences(today.minusDays(5), today.plusDays(10), today).size)
        assertTrue(t.occurrences(today, today.plusDays(10), today).isEmpty())
    }
    @Test fun futureRangeSkipsEarlierOccurrences() {
        val dates = task(today, 7).occurrences(today.plusDays(10), today.plusDays(30), today).map { it.date }
        assertEquals(listOf(today.plusDays(14), today.plusDays(21), today.plusDays(28)), dates)
    }
    @Test fun snoozeMovesNextOccurrenceAndProjection() {
        val t = task(today, 7).copy(snoozeUntil = today.plusDays(2).timestamp())
        assertEquals(listOf(today.plusDays(2), today.plusDays(9)), t.occurrences(today, today.plusDays(10), today).map { it.date })
    }
    @Test fun completedTaskRecalculatesFutureCalendar() {
        val t = task(today, 7).completed(today.plusDays(1).timestamp())
        assertEquals(today.plusDays(8), dayOf(t.dueAt()))
        assertEquals(today.plusDays(8), t.occurrences(today, today.plusDays(10), today).single().date)
    }
}
