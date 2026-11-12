package com.example.vuonnha.ui.plants

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditProfileRulesTest {
    @Test fun editProfileUsesTheSame1000CharacterLimit() {
        assertTrue(editProfileIsValid(mapOf("light" to "x".repeat(1000))))
        assertFalse(editProfileIsValid(mapOf("light" to "x".repeat(1001))))
    }

    @Test fun customSpeciesRenameRequestsReviewAndLeavesReferenceDataUntouched() {
        val profile = mapOf("scientificName" to "Species A", "location" to "Ban công")
        assertTrue(shouldRemindReviewProfileAfterSpeciesRename("Loài A", "Loài B", profile, REFERENCE_KEYS))
        assertEquals("Species A", profile["scientificName"])
        assertFalse(shouldRemindReviewProfileAfterSpeciesRename("Loài A", "loài a", profile, REFERENCE_KEYS))
        assertFalse(shouldRemindReviewProfileAfterSpeciesRename("Loài A", "Loài B", mapOf("location" to "Ban công"), REFERENCE_KEYS))
    }
}
