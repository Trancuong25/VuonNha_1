package com.example.vuonnha.ui.plants

import org.junit.Assert.assertEquals
import org.junit.Test

class LookupPlansTest {
    @Test fun uneditedLookupPlansAreReplacedForAnotherSpecies() {
        val result = updateLookupPlans(listOf("Tưới cây A"), "Loài A", false, "Loài B", listOf("Tưới cây B"))
        assertEquals(listOf("Tưới cây B"), result.plans)
        assertEquals("Loài B", result.sourceSpecies)
        assertEquals(false, result.userEdited)
    }

    @Test fun userEditedPlansSurviveLookupForAnotherSpecies() {
        val result = updateLookupPlans(listOf("Lịch người dùng"), "Loài A", true, "Loài B", listOf("Lịch B"))
        assertEquals(listOf("Lịch người dùng"), result.plans)
        assertEquals("Loài A", result.sourceSpecies)
        assertEquals(true, result.userEdited)
    }

    @Test fun emptyUneditedScheduleCanReceiveLookupPlansAndEmptySuggestionClearsStalePlans() {
        val inserted = updateLookupPlans(emptyList<String>(), "", false, "Loài A", listOf("Tưới"))
        assertEquals(listOf("Tưới"), inserted.plans)
        val cleared = updateLookupPlans(listOf("Lịch cũ"), "Loài A", false, "Loài B", emptyList())
        assertEquals(emptyList<String>(), cleared.plans)
        assertEquals("", cleared.sourceSpecies)
    }
}
