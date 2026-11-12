package com.example.vuonnha.ui.plants

import kotlinx.coroutines.CancellationException

internal suspend fun guardedOperation(
    message: String,
    block: suspend () -> Unit,
    onError: (String) -> Unit
) {
    try { block() }
    catch (error: CancellationException) { throw error }
    catch (error: Exception) { onError(message) }
}
