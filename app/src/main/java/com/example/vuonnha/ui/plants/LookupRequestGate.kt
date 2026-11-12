package com.example.vuonnha.ui.plants

internal data class LookupRequestState<T>(
    val token: Long = 0,
    val looking: Boolean = false,
    val matches: List<T> = emptyList(),
    val error: String? = null
)

internal fun <T> beginLookup(state: LookupRequestState<T>): LookupRequestState<T> =
    state.copy(token = state.token + 1, looking = true, matches = emptyList(), error = null)

internal fun <T> cancelLookup(state: LookupRequestState<T>): LookupRequestState<T> =
    state.copy(token = state.token + 1, looking = false)

internal fun <T> finishLookup(
    state: LookupRequestState<T>,
    token: Long,
    matches: List<T> = state.matches,
    error: String? = state.error
): LookupRequestState<T> =
    if (token != state.token) state else state.copy(looking = false, matches = matches, error = error)
