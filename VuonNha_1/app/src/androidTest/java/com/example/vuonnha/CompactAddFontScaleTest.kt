package com.example.vuonnha

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.example.vuonnha.ui.plants.AddSheet
import com.example.vuonnha.ui.theme.VuonNhaTheme
import org.junit.Rule
import org.junit.Test

class CompactAddFontScaleTest {
    @get:Rule val compose = createComposeRule()

    @Test fun primaryActionRemainsVisibleAtLargerTextScale() {
        compose.setContent {
            val current = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(current.density, 1.3f)) {
                VuonNhaTheme { AddSheet(emptyList(), {}, fullScreen = true) { _, _, _, _, _ -> } }
            }
        }
        compose.onNode(hasText("Tiếp tục") and hasClickAction()).assertIsDisplayed()
    }
}
