package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.CareTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDeletionConfirmationTest {
    private val task = CareTask(id = 7, itemId = 3, name = "Tưới cây", everyDays = 4, lastDone = 0)

    @Test fun confirmingDeletionCallsRemoveTaskForPendingTask() {
        val removed = mutableListOf<CareTask>()

        resolveTaskDeletion(task, confirmed = true) { removed += it }

        assertEquals(listOf(task), removed)
    }

    @Test fun cancellingDeletionDoesNotCallRemoveTask() {
        val removed = mutableListOf<CareTask>()

        resolveTaskDeletion(task, confirmed = false) { removed += it }

        assertTrue(removed.isEmpty())
    }
}
