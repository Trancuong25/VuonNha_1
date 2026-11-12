package com.example.vuonnha.ui.plants

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LookupRequestGateTest {
    @Test fun olderCompletionCannotChangeLookingOrApplyResultOrError() {
        val first = beginLookup(LookupRequestState<String>())
        val second = beginLookup(first)

        val afterOldSuccess = finishLookup(second, first.token, matches = listOf("old"))
        val afterOldError = finishLookup(afterOldSuccess, first.token, error = "old error")

        assertEquals(second, afterOldSuccess)
        assertEquals(second, afterOldError)
        assertTrue(afterOldError.looking)
        assertTrue(afterOldError.matches.isEmpty())
        assertEquals(null, afterOldError.error)
    }

    @Test fun cancelThenNewLookupOnlyAppliesTheLatestResult() {
        val first = beginLookup(LookupRequestState<String>())
        val cancelled = cancelLookup(first)
        val second = beginLookup(cancelled)

        val oldCompletion = finishLookup(second, first.token, matches = listOf("old"))
        val finalState = finishLookup(oldCompletion, second.token, matches = listOf("new"))

        assertEquals(second, oldCompletion)
        assertEquals(listOf("new"), finalState.matches)
        assertFalse(finalState.looking)
        assertEquals(second.token, finalState.token)
    }
}
