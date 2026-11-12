package com.example.vuonnha.ui.plants

internal fun editProfileIsValid(profile: Map<String, String>): Boolean = !hasOversizedProfileValue(profile)

internal fun shouldRemindReviewProfileAfterSpeciesRename(
    oldSpeciesName: String,
    newSpeciesName: String,
    profile: Map<String, String>,
    referenceKeys: Set<String>
): Boolean = oldSpeciesName.trim().isNotEmpty() &&
    newSpeciesName.trim().isNotEmpty() &&
    !oldSpeciesName.trim().equals(newSpeciesName.trim(), ignoreCase = true) &&
    profile.any { (key, value) -> key in referenceKeys && value.isNotBlank() }
