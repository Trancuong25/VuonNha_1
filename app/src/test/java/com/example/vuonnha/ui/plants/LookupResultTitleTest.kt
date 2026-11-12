package com.example.vuonnha.ui.plants

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LookupResultTitleTest {
    @Test fun missingScientificNameShowsOnlyCommonName() {
        val title = lookupResultTitle("Trầu bà", null)

        assertEquals("Trầu bà", title)
        assertFalse(title.contains("null"))
    }

    @Test fun emptyScientificNameShowsOnlyCommonName() {
        assertEquals("Trầu bà", lookupResultTitle("Trầu bà", ""))
    }

    @Test fun whitespaceScientificNameShowsOnlyCommonName() {
        assertEquals("Trầu bà", lookupResultTitle("Trầu bà", "   "))
    }

    @Test fun literalNullScientificNameShowsOnlyCommonName() {
        assertEquals("Trầu bà", lookupResultTitle("Trầu bà", "null"))
    }

    @Test fun presentScientificNameIsSeparatedFromCommonName() {
        assertEquals("Trầu bà · Monstera deliciosa", lookupResultTitle("Trầu bà", " Monstera deliciosa "))
    }
}
