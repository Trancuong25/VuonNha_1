package com.example.vuonnha.ui.plants

internal data class EditedProfileField(val profile: Map<String, String>, val editedFields: Set<String>)

internal fun updateProfileField(
    profile: Map<String, String>,
    editedFields: Set<String>,
    key: String,
    value: String
): EditedProfileField = EditedProfileField(profile + (key to value), editedFields + key)

internal fun mergeLookupProfile(
    profile: Map<String, String>,
    editedFields: Set<String>,
    lookupFields: Map<String, String>
): Map<String, String> = profile + lookupFields.filterKeys { it !in editedFields }
