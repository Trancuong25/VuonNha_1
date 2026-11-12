package com.example.vuonnha.ui.plants

internal data class LookupPlansResult<T>(val plans: List<T>, val sourceSpecies: String, val userEdited: Boolean)

internal fun <T> updateLookupPlans(
    current: List<T>,
    sourceSpecies: String,
    userEdited: Boolean,
    newSpecies: String,
    suggested: List<T>
): LookupPlansResult<T> {
    val changedSpecies = sourceSpecies.isNotBlank() &&
        !sourceSpecies.trim().equals(newSpecies.trim(), ignoreCase = true)
    val replace = !userEdited && (current.isEmpty() || changedSpecies)
    return if (replace) LookupPlansResult(suggested, newSpecies.takeIf { suggested.isNotEmpty() }.orEmpty(), false)
    else LookupPlansResult(current, sourceSpecies, userEdited)
}
