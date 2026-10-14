package com.example.vuonnha.domain

import com.example.vuonnha.data.CareTask
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone

class ScheduleTest {
    private fun date(value: String) = LocalDate.parse(value).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun inZone(zone: String, block: () -> Unit) {
        val previous = TimeZone.getDefault()
        try { TimeZone.setDefault(TimeZone.getTimeZone(zone)); block() }
        finally { TimeZone.setDefault(previous) }
    }
    private fun task(last: String, days: Int = 1) = CareTask(itemId = 1, name = "Tưới nước", everyDays = days, lastDone = date(last))

    @Test fun dueDateUsesCalendarDaysAcrossSpringClockChange() = inZone("America/New_York") {
        val t = task("2026-03-08")
        assertEquals(date("2026-03-09"), t.dueAt())
        assertEquals(1, t.daysLeft(date("2026-03-08")))
        assertEquals(0, t.daysLeft(date("2026-03-09")))
    }
    @Test fun dueDateUsesCalendarDaysAcrossAutumnClockChange() = inZone("America/New_York") {
        val t = task("2026-11-01")
        assertEquals(date("2026-11-02"), t.dueAt())
        assertEquals(-1, t.daysLeft(date("2026-11-03")))
    }
    @Test fun overdueSnoozeStartsFromToday() = inZone("Asia/Ho_Chi_Minh") {
        val t = task("2026-10-01").snoozed(now = date("2026-10-09"))
        assertEquals(date("2026-10-10"), t.dueAt())
        assertEquals(date("2026-10-01"), t.lastDone)
    }
    @Test fun earlySnoozeExtendsExistingDueDate() = inZone("Asia/Ho_Chi_Minh") {
        val t = task("2026-10-09", 7).snoozed(now = date("2026-10-10"))
        assertEquals(date("2026-10-17"), t.dueAt())
    }
    @Test fun completionClearsSnoozeAndRestartsCycle() = inZone("Asia/Ho_Chi_Minh") {
        val t = task("2026-10-01", 7).copy(snoozeUntil = date("2026-10-20"))
            .completed(now = date("2026-10-09"))
        assertEquals(0L, t.snoozeUntil)
        assertEquals(date("2026-10-16"), t.dueAt())
    }
    @Test(expected = IllegalArgumentException::class)
    fun snoozeRejectsZeroDays() { task("2026-10-09").snoozed(0) }
}
