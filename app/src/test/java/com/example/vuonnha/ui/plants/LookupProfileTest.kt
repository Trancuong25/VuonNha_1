package com.example.vuonnha.ui.plants

import org.junit.Assert.assertEquals
import org.junit.Test

class LookupProfileTest {
    @Test fun lookupDoesNotOverwriteManuallyEditedCurrentHeight() {
        val edited = updateProfileField(emptyMap(), emptySet(), "actualHeight", "35")

        val merged = mergeLookupProfile(
            edited.profile,
            edited.editedFields,
            mapOf("actualHeight" to "90", "scientificName" to "Monstera deliciosa")
        )

        assertEquals("35", merged["actualHeight"])
        assertEquals("Monstera deliciosa", merged["scientificName"])
    }
}
