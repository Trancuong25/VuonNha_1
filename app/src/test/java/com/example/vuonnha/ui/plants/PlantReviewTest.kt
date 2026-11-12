package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup
import org.junit.Assert.*
import org.junit.Test

class PlantReviewTest {
    private val draft = PlantDraft(mapOf("scientificName" to "Zamioculcas zamiifolia", "source" to "AI", "actualHeight" to "32", "location" to "Cửa sổ"), setOf("actualHeight", "missing"), "Kim tiền")

    @Test fun openingReviewAndEditingAgainPreservesAllDraftState() {
        val review = preparePlantReview(draft, "Cây táo", true, SpeciesGroup.PLANT)
        assertEquals(draft, review.draft)
        assertEquals(draft, preparePlantReview(review.draft, "Kim tiền", true, SpeciesGroup.PLANT).draft)
    }

    @Test fun savedProfileIsExactlyTheReviewedProjection() {
        val review = preparePlantReview(draft, "Cây táo", true, SpeciesGroup.PLANT)
        assertEquals(mapOf("actualHeight" to "32", "location" to "Cửa sổ"), review.profile)
        assertEquals(review.profile, profileToSave(review.profile, true, SpeciesGroup.PLANT))
    }

    @Test fun decliningReferencesDoesNotEraseDraftAndNeverChangesEnvironment() {
        val review = preparePlantReview(draft, "Kim tiền", false, SpeciesGroup.PLANT)
        assertEquals(draft, review.draft)
        assertEquals(mapOf("location" to "Cửa sổ"), review.profile)
    }

    @Test fun manuallyEnteredFieldsForBuiltInSpeciesAreRetainedInReviewedProfile() {
        val manualProfile = mapOf(
            "location" to "Cửa sổ",
            "actualLight" to "Nắng sáng",
            "pottingMix" to "Đất thoát nước tốt",
            "actualHeight" to "35"
        )
        val review = preparePlantReview(PlantDraft(manualProfile, emptySet(), ""), "Trầu bà", true, SpeciesGroup.PLANT)

        assertEquals(manualProfile, review.profile)
    }

    @Test fun oversizedReferenceIsNotReportedWhenReferencesAreNotSaved() {
        val profile = mapOf("scientificName" to "x".repeat(1001), "location" to "Cửa sổ")
        val savedProfile = preparePlantReview(PlantDraft(profile, emptySet(), ""), "Trầu bà", false, SpeciesGroup.PLANT).profile

        assertEquals(false, hasOversizedProfileValue(savedProfile))
    }

    @Test fun oversizedSavedFieldIsReported() {
        val profile = mapOf("location" to "x".repeat(1001))
        val savedProfile = preparePlantReview(PlantDraft(profile, emptySet(), ""), "Trầu bà", false, SpeciesGroup.PLANT).profile

        assertEquals(true, hasOversizedProfileValue(savedProfile))
    }
}
