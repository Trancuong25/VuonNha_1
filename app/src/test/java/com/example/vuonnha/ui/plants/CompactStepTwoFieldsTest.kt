package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup
import org.junit.Assert.assertEquals
import org.junit.Test

class CompactStepTwoFieldsTest {
    @Test fun locationIsPrimaryAndOtherFieldsAreDetailsForEveryGroup() {
        for (group in SpeciesGroup.entries) {
            assertEquals("location", compactLocationField(group).key)
            assertEquals(groupFields(group).map { it.key }.toSet() - "location",
                compactDetailFields(group).map { it.key }.toSet())
        }
    }
}
