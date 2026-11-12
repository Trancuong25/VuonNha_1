package com.example.vuonnha.ui.plants

internal data class AiSuggestionDiscardResult<T>(
    val profile: Map<String, String>,
    val editedFields: Set<String>,
    val derivedFor: String,
    val manualPlans: List<T>
)

internal fun <T> discardAiSuggestion(
    profile: Map<String, String>,
    editedFields: Set<String>,
    derivedFor: String,
    manualPlans: List<T>,
    aiSuggestedPlans: List<T>,
    referenceKeys: Set<String>
): AiSuggestionDiscardResult<T> {
    val aiKeysToRemove = referenceKeys - "actualHeight"
    val cleanedProfile = profile.filterKeys { it !in aiKeysToRemove }
    val cleanedEditedFields = editedFields.filterTo(linkedSetOf()) {
        it !in aiKeysToRemove && it in cleanedProfile
    }
    return AiSuggestionDiscardResult(
        profile = cleanedProfile,
        editedFields = cleanedEditedFields,
        derivedFor = "",
        manualPlans = if (manualPlans == aiSuggestedPlans) emptyList() else manualPlans
    )
}
