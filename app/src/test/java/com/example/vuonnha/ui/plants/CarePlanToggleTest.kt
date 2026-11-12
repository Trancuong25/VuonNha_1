package com.example.vuonnha.ui.plants

import org.junit.Assert.assertEquals
import org.junit.Test

class CarePlanToggleTest {
    @Test fun turningOffAndOnRestoresEditedCycle() {
        val edited = CarePlanOption("Thay nước", 12)
        val off = toggleCarePlan(listOf(edited), emptyList(), edited, false)
        assertEquals(emptyList<CarePlanOption>(), off.active)
        assertEquals(listOf(edited), off.inactive)
        val on = toggleCarePlan(off.active, off.inactive, edited, true)
        assertEquals(listOf(edited), on.active)
        assertEquals(emptyList<CarePlanOption>(), on.inactive)
    }

    @Test fun enablingSuggestionUsesItsOwnCycleWithoutDuplicatingIt() {
        val suggestion = CarePlanOption("Cho ăn", 1)
        val once = toggleCarePlan(emptyList(), emptyList(), suggestion, true)
        val twice = toggleCarePlan(once.active, once.inactive, suggestion, true)
        assertEquals(listOf(suggestion), twice.active)
    }
}
