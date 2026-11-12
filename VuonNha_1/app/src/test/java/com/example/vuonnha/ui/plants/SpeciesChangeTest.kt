package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class SpeciesChangeTest {
    private val referenceKeys = setOf("scientificName", "cultivar", "actualHeight", "matureHeight", "humidity", "soil", "light", "source")

    @Test fun changingToSameGroupDoesNotClearProfileOrEditedFields() {
        val profile = mapOf("scientificName" to "Monstera", "location" to "Ban công")
        val edited = setOf("scientificName")

        val result = clearSpeciesGroupReferenceData(profile, edited, referenceKeys, SpeciesGroup.PLANT, SpeciesGroup.PLANT)

        assertEquals(profile, result.profile)
        assertEquals(edited, result.editedFields)
    }

    @Test fun changingGroupClearsReferenceFieldsAndKeepsCurrentConditionsAndHeight() {
        val profile = mapOf(
            "scientificName" to "Monstera",
            "cultivar" to "Thai Constellation",
            "actualHeight" to "45",
            "matureHeight" to "200",
            "location" to "Ban công",
            "actualLight" to "Nắng sáng",
            "pottingMix" to "Đất xơ dừa"
        )
        val edited = setOf("scientificName", "actualHeight", "location")

        val result = clearSpeciesGroupReferenceData(profile, edited, referenceKeys, SpeciesGroup.PLANT, SpeciesGroup.FISH)

        assertEquals(
            mapOf("actualHeight" to "45", "location" to "Ban công", "actualLight" to "Nắng sáng", "pottingMix" to "Đất xơ dừa"),
            result.profile
        )
        assertEquals(setOf("actualHeight", "location"), result.editedFields)
    }

    @Test fun sameSpeciesKeepsProfileAndEditedFields() {
        val profile = mapOf("scientificName" to "Monstera deliciosa", "location" to "Ban công", "light" to "Nắng sáng")
        val editedFields = setOf("light")

        val result = clearSpeciesDerivedData(profile, editedFields, referenceKeys, "Kim tiền", "Kim tiền")

        assertEquals(profile, result.profile)
        assertEquals(editedFields, result.editedFields)
    }

    @Test fun sameSpeciesIgnoringCaseAndWhitespaceKeepsProfile() {
        val profile = mapOf("scientificName" to "Zamioculcas zamiifolia", "location" to "Ban công")
        val editedFields = setOf("scientificName")

        val result = clearSpeciesDerivedData(profile, editedFields, referenceKeys, "  Kim Tiền ", "kim tiền")

        assertEquals(profile, result.profile)
        assertEquals(editedFields, result.editedFields)
    }

    @Test fun changedSpeciesAfterTrimmingStillRemovesLookupFields() {
        val profile = mapOf("scientificName" to "Zamioculcas zamiifolia", "location" to "Ban công")

        val result = clearSpeciesDerivedData(profile, emptySet(), referenceKeys, " Kim tiền ", " Cây táo ")

        assertEquals(mapOf("location" to "Ban công"), result.profile)
    }

    @Test fun changedSpeciesRemovesLookupFields() {
        val profile = mapOf("scientificName" to "Zamioculcas zamiifolia", "humidity" to "Trung bình", "source" to "Tra cứu nhanh")

        val result = clearSpeciesDerivedData(profile, emptySet(), referenceKeys, "Kim tiền", "Cây táo")

        assertEquals(emptyMap<String, String>(), result.profile)
        assertEquals(emptySet<String>(), result.editedFields)
    }

    @Test fun changedSpeciesKeepsEnvironmentFields() {
        val profile = mapOf(
            "location" to "Cạnh cửa sổ hướng đông",
            "actualLight" to "Nắng sáng 2 giờ",
            "pottingMix" to "Đất trộn xơ dừa",
            "scientificName" to "Zamioculcas zamiifolia"
        )

        val result = clearSpeciesDerivedData(profile, emptySet(), referenceKeys, "Kim tiền", "Cây táo")

        assertEquals(
            mapOf(
                "location" to "Cạnh cửa sổ hướng đông",
                "actualLight" to "Nắng sáng 2 giờ",
                "pottingMix" to "Đất trộn xơ dừa"
            ),
            result.profile
        )
    }

    @Test fun changedSpeciesKeepsManuallyEditedFields() {
        val profile = mapOf("actualHeight" to "45", "scientificName" to "Zamioculcas zamiifolia", "location" to "Phòng khách")
        val editedFields = setOf("actualHeight")

        val result = clearSpeciesDerivedData(profile, editedFields, referenceKeys, "Kim tiền", "Cây táo")

        assertEquals(mapOf("actualHeight" to "45", "location" to "Phòng khách"), result.profile)
        assertEquals(setOf("actualHeight"), result.editedFields)
    }

    @Test fun changedSpeciesDropsEditedFieldsWithoutValues() {
        val profile = mapOf("scientificName" to "Zamioculcas zamiifolia", "location" to "Ban công")
        val editedFields = setOf("humidity", "location")

        val result = clearSpeciesDerivedData(profile, editedFields, referenceKeys, "Kim tiền", "Cây táo")

        assertEquals(mapOf("location" to "Ban công"), result.profile)
        assertEquals(setOf("location"), result.editedFields)
    }
}
