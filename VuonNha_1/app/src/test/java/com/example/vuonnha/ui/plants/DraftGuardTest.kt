package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DraftGuardTest {
    private val today = LocalDate.of(2026, 10, 26)
    private val blank = AddSheetDraftState(
        name = "",
        speciesName = "",
        group = SpeciesGroup.PLANT,
        profile = emptyMap(),
        editedFields = emptySet(),
        derivedFor = "",
        manualPlans = emptyList(),
        suggestedPlans = listOf(DraftCarePlan("Tưới nước", 7, today)),
        plans = emptyList(),
        zoneId = null,
        firstReminder = false,
        usingAi = false,
        custom = true,
        speciesId = "monstera",
        keepExtra = true
    )

    @Test fun unchangedEmptyFormHasNoDraft() {
        assertFalse(hasUnsavedDraft(blank, blank))
    }

    @Test fun enteredTextCountsAsDraft() {
        assertTrue(hasUnsavedDraft(blank.copy(speciesName = "KiemTra"), blank))
    }

    @Test fun unchangedAiPrefillIsPartOfInitialStateNotAnUnsavedDraft() {
        val aiPrefill = blank.copy(
            name = "Kim tiền",
            speciesName = "Kim tiền",
            profile = mapOf("scientificName" to "Zamioculcas zamiifolia", "actualHeight" to "35"),
            derivedFor = "Kim tiền",
            manualPlans = listOf(DraftCarePlan("Kiểm tra đất", 5, today)),
            plans = listOf(DraftCarePlan("Kiểm tra đất", 5, today)),
            usingAi = true
        )

        assertFalse(hasUnsavedDraft(aiPrefill, aiPrefill))
    }

    @Test fun editingAiPrefillCountsAsDraft() {
        val aiPrefill = blank.copy(
            name = "Kim tiền",
            speciesName = "Kim tiền",
            profile = mapOf("scientificName" to "Zamioculcas zamiifolia", "actualHeight" to "35"),
            derivedFor = "Kim tiền",
            usingAi = true
        )

        val edited = aiPrefill.copy(
            profile = aiPrefill.profile + ("actualHeight" to "40"),
            editedFields = setOf("actualHeight")
        )

        assertTrue(hasUnsavedDraft(edited, aiPrefill))
    }

    @Test fun changingOnlyGroupCountsAsDraft() {
        assertTrue(hasUnsavedDraft(blank.copy(group = SpeciesGroup.FISH), blank))
    }

    @Test fun fishDraftIgnoresHiddenPlantFieldsButCountsTankFields() {
        val fish = blank.copy(group = SpeciesGroup.FISH)
        assertFalse(hasUnsavedDraft(fish.copy(profile = mapOf("actualLight" to "Sáng")), fish))
        assertTrue(hasUnsavedDraft(fish.copy(profile = mapOf("tankVolume" to "20")), fish))
    }

    @Test fun changingOnlyReminderCountsAsDraft() {
        assertTrue(hasUnsavedDraft(blank.copy(firstReminder = true), blank))
    }

    @Test fun changingOnlyCarePlanCountsAsDraft() {
        val changed = blank.copy(plans = listOf(DraftCarePlan("Tưới nước", 5, today)))

        assertTrue(hasUnsavedDraft(changed, blank))
    }
}
