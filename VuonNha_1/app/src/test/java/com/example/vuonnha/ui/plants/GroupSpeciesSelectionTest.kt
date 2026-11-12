package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SPECIES
import com.example.vuonnha.data.SpeciesGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupSpeciesSelectionTest {
    @Test fun builtInListContainsOnlySelectedGroup() {
        assertEquals(listOf("monstera", "sen", "lan", "basil"), speciesForGroup(SpeciesGroup.PLANT).map { it.id })
        assertEquals(listOf("betta", "vang", "guppy"), speciesForGroup(SpeciesGroup.FISH).map { it.id })
        assertEquals(listOf("hamster"), speciesForGroup(SpeciesGroup.PET).map { it.id })
    }

    @Test fun changingGroupSelectsFirstSpeciesInThatGroup() {
        assertEquals("betta", speciesAfterGroupChange(SPECIES.first(), SpeciesGroup.FISH).id)
        assertEquals("hamster", speciesAfterGroupChange(SPECIES.first(), SpeciesGroup.PET).id)
        assertEquals("monstera", speciesAfterGroupChange(SPECIES.first { it.id == "betta" }, SpeciesGroup.PLANT).id)
    }

    @Test fun selectingSameGroupKeepsCurrentSpecies() {
        val guppy = SPECIES.first { it.id == "guppy" }
        assertEquals(guppy, speciesAfterGroupChange(guppy, SpeciesGroup.FISH))
    }
}
