package com.example.vuonnha.ui.plants

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Assert.fail

class OperationGuardTest {
    @Test fun repositoryFailureProducesUserFacingErrorWithoutEscaping() = runBlocking {
        var message: String? = null
        guardedOperation("Không lưu được", { throw IllegalStateException("database") }) { message = it }
        assertEquals("Không lưu được", message)
    }

    @Test fun cancellationIsRethrown() {
        try {
            runBlocking {
                guardedOperation("Không lưu được", { throw CancellationException("cancelled") }) {}
            }
            fail("CancellationException should propagate")
        } catch (_: CancellationException) {
        }
    }
}
