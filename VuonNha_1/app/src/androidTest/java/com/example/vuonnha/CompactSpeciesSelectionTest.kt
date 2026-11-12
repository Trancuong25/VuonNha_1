package com.example.vuonnha

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.vuonnha.ui.plants.AddSheet
import com.example.vuonnha.ui.theme.VuonNhaTheme
import org.junit.Rule
import org.junit.Test

class CompactSpeciesSelectionTest {
    @get:Rule val compose = createComposeRule()

    @Test fun fishCardShowsVerticalSuggestionAndSelectingItFillsName() {
        compose.setContent { VuonNhaTheme { AddSheet(emptyList(), {}, fullScreen = true) { _, _, _, _, _ -> } } }
        compose.onNodeWithText("Cá").performClick()
        compose.onNodeWithText("Cá betta").performClick()
        compose.onNodeWithText("✓").assertExists()
    }
}
