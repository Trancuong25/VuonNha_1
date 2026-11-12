package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup
import java.time.LocalDate

internal data class DraftCarePlan(
    val name: String,
    val days: Int,
    val date: LocalDate
)

internal data class AddSheetDraftState(
    val name: String,
    val speciesName: String,
    val group: SpeciesGroup,
    val profile: Map<String, String>,
    val editedFields: Set<String>,
    val derivedFor: String,
    val manualPlans: List<DraftCarePlan>,
    val suggestedPlans: List<DraftCarePlan>,
    val plans: List<DraftCarePlan>,
    val zoneId: Long?,
    val firstReminder: Boolean,
    val usingAi: Boolean,
    val custom: Boolean,
    val speciesId: String,
    val keepExtra: Boolean
)

internal fun hasUnsavedDraft(current: AddSheetDraftState, initial: AddSheetDraftState): Boolean {
    if (current.group == SpeciesGroup.PLANT && initial.group == SpeciesGroup.PLANT) return current != initial
    val visibleKeys = groupFields(current.group).mapTo(hashSetOf()) { it.key }
    fun visible(state: AddSheetDraftState): AddSheetDraftState = state.copy(
        profile = state.profile.filterKeys { it in visibleKeys },
        editedFields = state.editedFields.filterTo(linkedSetOf()) { it in visibleKeys }
    )
    return visible(current) != visible(initial)
}
