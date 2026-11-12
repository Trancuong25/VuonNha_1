package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup

internal data class PlantDraft(val profile: Map<String, String>, val editedFields: Set<String>, val derivedFor: String)
internal data class PlantReview(val draft: PlantDraft, val profile: Map<String, String>)

internal fun preparePlantReview(draft: PlantDraft, speciesName: String, keepExtra: Boolean, group: SpeciesGroup): PlantReview {
    val cleaned = if (draft.derivedFor.isBlank()) SpeciesChangeResult(draft.profile, draft.editedFields)
        else clearSpeciesDerivedData(draft.profile, draft.editedFields, REFERENCE_KEYS - "actualHeight", draft.derivedFor, speciesName)
    return PlantReview(draft, profileToSave(cleaned.profile, keepExtra, group))
}
