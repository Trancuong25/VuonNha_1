package com.example.vuonnha.data

import org.junit.Assert.*
import org.junit.Test

class CustomSpeciesTest {
    @Test fun userEnteredSpeciesSurvivesStorageAndBackupIdRoundTrip() {
        val original = customSpecies("  Cây lá tím: chưa biết tên 🌿  ", SpeciesGroup.PLANT, listOf("Tưới nước" to 8))
        val loaded = requireNotNull(findSpecies(original.id))
        assertEquals("Cây lá tím: chưa biết tên 🌿", loaded.name)
        assertEquals(original.id, loaded.id)
        assertEquals(SpeciesGroup.PLANT.emoji, loaded.emoji)
        // Care tasks are stored separately, never inferred for an unknown species.
        assertTrue(loaded.tasks.isEmpty())
        assertEquals(listOf("Tưới nước" to 8), original.tasks)
    }
    @Test fun supportsFishAndPetsWithoutPlantSchedules() {
        SpeciesGroup.entries.forEach { group ->
            val loaded = requireNotNull(findSpecies(customSpecies("Loài riêng", group).id))
            assertEquals(group.emoji, loaded.emoji)
            assertTrue(loaded.tasks.isEmpty())
        }
    }
    @Test fun builtInSpeciesRemainUnchanged() {
        SPECIES.forEach { assertEquals(it, findSpecies(it.id)) }
    }
    @Test fun malformedIdsAreNotRecognized() {
        listOf("custom", "custom:PLANT:", "custom:UNKNOWN:test", "unknown").forEach { assertNull(findSpecies(it)) }
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsBlankSpeciesName() {
        customSpecies("  ", SpeciesGroup.PLANT)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsUnnamedCareTask() {
        customSpecies("Kim tiền", SpeciesGroup.PLANT, listOf(" " to 7))
    }
}
