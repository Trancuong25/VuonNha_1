package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SPECIES
import com.example.vuonnha.data.SpeciesGroup
import org.junit.Assert.*
import org.junit.Test

class SpeciesSuggestionsTest {
    @Test fun emptyQueryReturnsOnlySelectedGroupWithinLimit() {
        assertEquals(listOf("betta", "vang"), speciesSuggestions(SpeciesGroup.FISH, "", 2).map { it.id })
        assertEquals(listOf("hamster"), speciesSuggestions(SpeciesGroup.PET, "", 4).map { it.id })
    }

    @Test fun queryMatchesWithoutVietnameseAccentsAndPrefersPrefix() {
        assertEquals(listOf("monstera"), speciesSuggestions(SpeciesGroup.PLANT, "trau ba", 4).map { it.id })
        assertEquals(listOf("vang"), speciesSuggestions(SpeciesGroup.FISH, "ca vang", 4).map { it.id })
    }

    @Test fun noMatchAndZeroLimitReturnNothing() {
        assertTrue(speciesSuggestions(SpeciesGroup.PLANT, "khong co", 4).isEmpty())
        assertTrue(speciesSuggestions(SpeciesGroup.PLANT, "", 0).isEmpty())
    }

    @Test fun typingKeepsCustomEvenWhenNameEqualsBuiltInAndClickSelectsBuiltIn() {
        val first = SPECIES.first()
        val typed = resolveSpeciesInput(first, "Sen đá", null)
        assertTrue(typed.custom)
        assertEquals("Sen đá", typed.name)
        val selected = resolveSpeciesInput(first, typed.name, SPECIES.first { it.id == "sen" })
        assertFalse(selected.custom)
        assertEquals("sen", selected.species.id)
    }
}
