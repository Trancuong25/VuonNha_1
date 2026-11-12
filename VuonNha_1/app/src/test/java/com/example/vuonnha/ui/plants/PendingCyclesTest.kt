package com.example.vuonnha.ui.plants

import org.junit.Assert.assertEquals
import org.junit.Test

class PendingCyclesTest {
    @Test fun editedThenDeletedTaskDoesNotBlockOtherEdits() {
        assertEquals(mapOf(2L to 14), cyclesForExistingTasks(mapOf(1L to 9, 2L to 14), setOf(2L)))
    }

    @Test fun deletingTheOnlyEditedTaskLeavesNoPendingCycle() {
        assertEquals(emptyMap<Long, Int>(), cyclesForExistingTasks(mapOf(1L to 9), emptySet()))
    }

    @Test fun existingEditsRemainUnchanged() {
        assertEquals(mapOf(1L to 9), cyclesForExistingTasks(mapOf(1L to 9), setOf(1L, 2L)))
    }
}
