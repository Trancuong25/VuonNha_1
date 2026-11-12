package com.example.vuonnha

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso
import com.example.vuonnha.data.PlantCandidate
import com.example.vuonnha.ui.plants.AddSheet
import com.example.vuonnha.ui.theme.VuonNhaTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AddSheetDraftDiscardTest {
    @get:Rule val compose = createComposeRule()

    @Test fun backOnDirtyDraftShowsDialogAndContinueKeepsText() {
        var dismissed = false
        compose.setContent {
            VuonNhaTheme {
                AddSheet(emptyList(), { dismissed = true }, fullScreen = true) { _, _, _, _, _ -> }
            }
        }

        compose.onNodeWithText("Tên loài hoặc mô tả *").performTextInput("KiemTra")
        pressBackUntilDialogAppears()
        compose.onNodeWithText("Tiếp tục nhập").performClick()
        compose.onNode(hasText("KiemTra")).assertExists()
        compose.runOnIdle { assertFalse(dismissed) }
    }

    @Test fun choosingDiscardClosesDirtyDraft() {
        var dismissed = false
        compose.setContent {
            VuonNhaTheme {
                AddSheet(emptyList(), { dismissed = true }, fullScreen = true) { _, _, _, _, _ -> }
            }
        }

        compose.onNodeWithText("Tên loài hoặc mô tả *").performTextInput("KiemTra")
        pressBackUntilDialogAppears()
        compose.onNodeWithText("Bỏ").performClick()
        compose.onNodeWithText("Bỏ bản nhập?").assertDoesNotExist()
        compose.runOnIdle { assertTrue(dismissed) }
    }

    @Test fun emptyFormBackClosesWithoutDialog() {
        var dismissed = false
        compose.setContent {
            VuonNhaTheme {
                AddSheet(emptyList(), { dismissed = true }, fullScreen = true) { _, _, _, _, _ -> }
            }
        }

        Espresso.pressBack()
        compose.onNodeWithText("Bỏ bản nhập?").assertDoesNotExist()
        compose.runOnIdle { assertTrue(dismissed) }
    }

    @Test fun headerBackAtStepZeroUsesDiscardDialog() {
        compose.setContent {
            VuonNhaTheme {
                AddSheet(emptyList(), {}, fullScreen = true, backLabel = "‹ Vườn Nhà") { _, _, _, _, _ -> }
            }
        }

        compose.onNodeWithText("Tên loài hoặc mô tả *").performTextInput("KiemTra")
        Espresso.closeSoftKeyboard()
        compose.onNodeWithContentDescription("Quay lại").performClick()
        compose.onNodeWithText("Bỏ bản nhập?").assertExists()
    }

    @Test fun backAfterAdvancingOnlyReturnsToPreviousStep() {
        compose.setContent {
            VuonNhaTheme {
                AddSheet(emptyList(), {}, fullScreen = true) { _, _, _, _, _ -> }
            }
        }

        compose.onNodeWithText("Tên loài hoặc mô tả *").performTextInput("KiemTra")
        Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Tiếp tục").performClick()
        compose.onNodeWithText("Cây sẽ ở đâu?").assertExists()
        Espresso.pressBack()
        compose.onNodeWithText("Bạn thêm gì vào vườn?").assertExists()
        compose.onNodeWithText("Bỏ bản nhập?").assertDoesNotExist()
    }

    @Test fun dialogVisibilityAndDraftSurviveSavedStateRestore() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            VuonNhaTheme {
                AddSheet(emptyList(), {}, fullScreen = true) { _, _, _, _, _ -> }
            }
        }

        compose.onNodeWithText("Tên loài hoặc mô tả *").performTextInput("KiemTra")
        pressBackUntilDialogAppears()
        compose.onNodeWithText("Bỏ bản nhập?").assertExists()

        restoration.emulateSavedInstanceStateRestore()

        compose.onNodeWithText("Bỏ bản nhập?").assertExists()
        compose.onNodeWithText("Tiếp tục nhập").performClick()
        compose.onNode(hasText("KiemTra")).assertExists()
    }

    @Test fun untouchedAiPrefillCanReturnWithoutDiscardPrompt() {
        var dismissed = false
        val candidate = PlantCandidate(
            "Kim tiền", "Zamioculcas zamiifolia", "medium", emptyList(),
            "Gợi ý chăm sóc", listOf("Kiểm tra đất" to 5), mapOf("light" to "Ánh sáng gián tiếp")
        )
        compose.setContent {
            VuonNhaTheme {
                AddSheet(emptyList(), { dismissed = true }, candidate, fullScreen = true) { _, _, _, _, _ -> }
            }
        }

        Espresso.pressBack()
        compose.onNodeWithText("Bỏ bản nhập?").assertDoesNotExist()
        compose.runOnIdle { assertTrue(dismissed) }
    }

    @Test fun backWhileSavingIsConsumedAndKeepsAddSheetOpen() {
        compose.setContent {
            VuonNhaTheme {
                AddSheet(emptyList(), {}, saving = true, fullScreen = true) { _, _, _, _, _ -> }
            }
        }

        Espresso.pressBack()

        compose.onNodeWithText("Tên loài hoặc mô tả *").assertExists()
        compose.onNodeWithText("Bỏ bản nhập?").assertDoesNotExist()
    }

    @Test fun modalSheetBackWithDraftShowsDialogBeforeHidingSheet() {
        compose.setContent {
            VuonNhaTheme {
                AddSheet(emptyList(), {}, fullScreen = false) { _, _, _, _, _ -> }
            }
        }

        compose.onNodeWithText("Tên loài hoặc mô tả *").performTextInput("KiemTra")
        pressBackUntilDialogAppears()

        compose.onNodeWithText("Bỏ bản nhập?").assertExists()
        compose.onNodeWithText("Tên loài hoặc mô tả *").assertExists()
    }

    private fun pressBackUntilDialogAppears() {
        Espresso.pressBack()
        compose.waitForIdle()
        if (compose.onAllNodesWithText("Bỏ bản nhập?").fetchSemanticsNodes().isEmpty()) {
            Espresso.pressBack()
        }
        compose.onNodeWithText("Bỏ bản nhập?").assertExists()
    }
}
