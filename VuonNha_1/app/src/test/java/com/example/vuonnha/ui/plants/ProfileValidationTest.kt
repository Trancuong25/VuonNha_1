package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup
import org.junit.Assert.*
import org.junit.Test

class ProfileValidationTest {
    @Test fun discardedReferenceCannotBlockSave() {
        for (key in REFERENCE_KEYS) {
            val profile = mapOf(key to "x".repeat(1001), "location" to "Ban công")
            assertTrue(key, profileIsValidForSave(profile, false, SpeciesGroup.PLANT))
            assertFalse(profileToSave(profile, false, SpeciesGroup.PLANT).containsKey(key))
            assertFalse(key, profileIsValidForSave(profile, true, SpeciesGroup.PLANT))
        }
    }

    @Test fun storedFieldsAreValidatedForTheirGroup() {
        for (group in SpeciesGroup.entries) {
            val keys = if (group == SpeciesGroup.PLANT) listOf("location", "actualLight", "pottingMix")
                else groupFields(group).map { it.key }
            for (key in keys) for (keep in listOf(true, false)) {
                assertFalse(profileIsValidForSave(mapOf(key to "x".repeat(1001)), keep, group))
                assertTrue(profileIsValidForSave(mapOf(key to "x".repeat(1000)), keep, group))
            }
        }
    }

    @Test fun nonPlantCannotBeBlockedByPlantReferenceNotBeingStored() {
        assertTrue(profileIsValidForSave(mapOf("soil" to "x".repeat(1001)), true, SpeciesGroup.FISH))
    }
}
