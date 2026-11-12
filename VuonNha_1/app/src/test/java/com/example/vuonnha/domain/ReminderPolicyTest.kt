package com.example.vuonnha.domain

import com.example.vuonnha.data.CareTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone

class ReminderPolicyTest {
    private fun at(date: String, hour: Int = 8, zone: String = "Asia/Ho_Chi_Minh"): Long =
        LocalDate.parse(date).atTime(hour, 0).atZone(ZoneId.of(zone)).toInstant().toEpochMilli()

    private fun inZone(zone: String, block: () -> Unit) {
        val previous = TimeZone.getDefault()
        try { TimeZone.setDefault(TimeZone.getTimeZone(zone)); block() }
        finally { TimeZone.setDefault(previous) }
    }

    @Test fun dueSelectionUsesLocalCalendarDateAndSkipsDeletedItems() = inZone("America/New_York") {
        val due = CareTask(id = 1, itemId = 10, name = "Tưới", everyDays = 1, lastDone = at("2026-03-07", 8, "America/New_York"))
        val deleted = due.copy(id = 2, itemId = 99)
        assertEquals(listOf(due), dueTasksForReminder(listOf(due, deleted), setOf(10), at("2026-03-08", 9, "America/New_York")))
    }

    @Test fun duplicateTaskRowsDoNotProduceDuplicateReminderEntries() {
        val task = CareTask(id = 3, itemId = 10, name = "Tưới", everyDays = 1, lastDone = at("2026-10-26"))
        assertEquals(listOf(task), dueTasksForReminder(listOf(task, task), setOf(10), at("2026-10-27")))
    }

    @Test fun sameDayReminderIsSuppressedUnlessForced() {
        val today = at("2026-10-27", 0)
        assertFalse(shouldSendReminder(today, today, force = false))
        assertTrue(shouldSendReminder(today, at("2026-10-28", 0), force = false))
        assertTrue(shouldSendReminder(today, today, force = true))
    }

    @Test fun completionMovesNextDueDateFromCompletionDay() {
        val task = CareTask(id = 4, itemId = 10, name = "Bón phân", everyDays = 3, lastDone = at("2026-10-20"))
        val completed = task.completed(at("2026-10-27", 17))
        assertEquals(at("2026-10-30", 0), completed.dueAt())
        assertTrue(dueTasksForReminder(listOf(completed), setOf(10), at("2026-10-27")).isEmpty())
    }
}
