package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup

data class SpeciesChangeResult(
    val profile: Map<String, String>,
    val editedFields: Set<String>
)

fun clearSpeciesDerivedData(
    profile: Map<String, String>,
    editedFields: Set<String>,
    referenceKeys: Set<String>,
    oldSpeciesName: String,
    newSpeciesName: String
): SpeciesChangeResult {
    val sameSpecies = oldSpeciesName.trim().equals(newSpeciesName.trim(), ignoreCase = true)
    if (sameSpecies) return SpeciesChangeResult(profile, editedFields)
    val cleanedProfile = profile.filterKeys { key -> key !in referenceKeys || key in editedFields }
    val cleanedEditedFields = editedFields.filterTo(linkedSetOf()) { it in cleanedProfile }
    return SpeciesChangeResult(cleanedProfile, cleanedEditedFields)
}

fun clearSpeciesGroupReferenceData(
    profile: Map<String, String>,
    editedFields: Set<String>,
    referenceKeys: Set<String>,
    oldGroup: SpeciesGroup,
    newGroup: SpeciesGroup
): SpeciesChangeResult {
    if (oldGroup == newGroup) return SpeciesChangeResult(profile, editedFields)
    val retainedProfile = profile.filterKeys { key -> key !in referenceKeys || key == "actualHeight" }
    val retainedEditedFields = editedFields.filterTo(linkedSetOf()) { it in retainedProfile }
    return SpeciesChangeResult(retainedProfile, retainedEditedFields)
}

internal fun profileAfterGroupChange(
    profile: Map<String, String>,
    editedFields: Set<String>,
    oldGroup: SpeciesGroup,
    newGroup: SpeciesGroup
): SpeciesChangeResult {
    if (oldGroup == newGroup) return SpeciesChangeResult(profile, editedFields)
    val retainedProfile = profile.filterKeys { it == "location" }
    return SpeciesChangeResult(retainedProfile, editedFields.filterTo(linkedSetOf()) { it in retainedProfile })
}
