package com.example.vuonnha.ui.plants

import org.junit.Assert.assertEquals
import org.junit.Test

class AiSuggestionDiscardTest {
    private val referenceKeys = setOf("scientificName", "cultivar", "actualHeight", "matureHeight", "humidity", "soil", "light", "source")

    @Test fun discardKeepsEnvironmentAndHeightAndClearsAiReferencesAndFlags() {
        val profile = mapOf(
            "location" to "Ban công",
            "actualLight" to "Nắng sáng",
            "pottingMix" to "Đất xơ dừa",
            "actualHeight" to "35",
            "scientificName" to "Monstera",
            "source" to "Gợi ý AI"
        )
        val edited = setOf("location", "actualHeight", "scientificName", "source")

        val result = discardAiSuggestion(profile, edited, "Monstera", emptyList<String>(), emptyList(), referenceKeys)

        assertEquals(mapOf(
            "location" to "Ban công",
            "actualLight" to "Nắng sáng",
            "pottingMix" to "Đất xơ dừa",
            "actualHeight" to "35"
        ), result.profile)
        assertEquals(setOf("location", "actualHeight"), result.editedFields)
        assertEquals("", result.derivedFor)
    }

    @Test fun discardKeepsManualPlansThatDifferFromAiSchedule() {
        val aiPlans = listOf("Tưới nước" to 7, "Bón phân" to 30)
        val changedPlans = listOf("Tưới nước" to 10, "Bón phân" to 30)

        val result = discardAiSuggestion(emptyMap(), emptySet(), "Monstera", changedPlans, aiPlans, referenceKeys)

        assertEquals(changedPlans, result.manualPlans)
    }

    @Test fun discardRemovesManualPlansWhenTheyStillMatchAiSchedule() {
        val aiPlans = listOf("Tưới nước" to 7, "Bón phân" to 30)

        val result = discardAiSuggestion(emptyMap(), emptySet(), "Monstera", aiPlans, aiPlans, referenceKeys)

        assertEquals(emptyList<Pair<String, Int>>(), result.manualPlans)
    }
}
