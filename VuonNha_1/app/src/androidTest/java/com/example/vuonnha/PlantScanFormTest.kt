package com.example.vuonnha

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import com.example.vuonnha.data.PlantCandidate
import com.example.vuonnha.data.Species
import com.example.vuonnha.data.findSpecies
import com.example.vuonnha.ui.plants.AddSheet
import com.example.vuonnha.ui.theme.VuonNhaTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PlantScanFormTest {
    @get:Rule val compose = createComposeRule()
    private val candidate = PlantCandidate("Kim tiền", "Zamioculcas zamiifolia", "medium",
        listOf("Lá kép"), "Kiểm tra độ khô của đất.", emptyList())

    @Test fun blankOptionalTaskExplainsBlockedSaveAndCanBeRemoved() {
        compose.setContent { VuonNhaTheme { AddSheet(emptyList(), {}, candidate, fullScreen = true) { _, _, _, _, _ -> } } }
        compose.onNodeWithText("Tiếp tục").performClick()
        compose.onNodeWithText("Tiếp tục").performClick()
        compose.onNodeWithText("＋ Thêm việc chăm sóc").performScrollTo()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onNodeWithText("Nhập tên việc hoặc bấm thùng rác để bỏ lịch này.").assertExists()
        compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performClick()
        compose.onNodeWithText("Xem lại trước khi lưu").assertDoesNotExist()
        compose.onNodeWithContentDescription("Bỏ việc").performScrollTo()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).assertIsEnabled()
    }

    @Test fun invalidSpeciesExplainsWhySaveIsUnavailableAndCanBeCorrected() {
        compose.setContent { VuonNhaTheme { AddSheet(emptyList(), {}, fullScreen = true) { _, _, _, _, _ -> } } }
        compose.onNodeWithText("Tên loài hoặc mô tả *").performScrollTo().performTextReplacement("a".repeat(121))
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Tên cây dài quá, bạn rút gọn còn tối đa 120 ký tự nhé.").assertExists()
        compose.onNodeWithText("Tiếp tục").performClick()
        compose.onNodeWithText("Bạn đang trồng cây gì?").assertExists()
        compose.onNodeWithText("Tên loài hoặc mô tả *").performScrollTo().performTextReplacement("Kim tiền")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Tiếp tục").assertIsEnabled().performClick()
        compose.onNodeWithText("Tiếp tục").performClick()
        compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).assertIsEnabled()
    }

    @Test fun acceptedSuggestionPrefillsButDoesNotSaveUntilConfirmed() {
        var saved: Species? = null
        compose.setContent { VuonNhaTheme { AddSheet(emptyList(), {}, candidate) { _, species, _, _, _ -> saved = species } } }
        compose.onAllNodesWithText("Kim tiền").onFirst().assertExists()
        compose.runOnIdle { assertNull(saved) }
        compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performScrollTo().performClick()
        compose.runOnIdle { assertEquals("Kim tiền", findSpecies(requireNotNull(saved).id)?.name) }
    }

    @Test fun dismissingAiInfoClearsPrefillWithoutSaving() {
        var saved = false
        compose.setContent { VuonNhaTheme { AddSheet(emptyList(), {}, candidate) { _, _, _, _, _ -> saved = true } } }
        compose.onAllNodesWithText("Kim tiền").onFirst().assertExists()
        compose.onNodeWithText("Bỏ gợi ý, tự nhập").performClick()
        compose.onNodeWithText("Kim tiền").assertDoesNotExist()
        compose.runOnIdle { assertFalse(saved) }
    }

    @Test fun failedSaveKeepsPrefillAndOffersRetry() {
        compose.setContent { VuonNhaTheme {
            AddSheet(emptyList(), {}, candidate, saveError = "Chưa lưu được cây. Hãy thử lại.") { _, _, _, _, _ -> }
        } }
        compose.onAllNodesWithText("Kim tiền").onFirst().assertExists()
        compose.onNodeWithText("Chưa lưu được cây. Hãy thử lại.").performScrollTo().assertExists()
        compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performScrollTo().assertExists()
    }

    @Test fun savingPreventsSubmittingTwice() {
        var saved = false
        compose.setContent { VuonNhaTheme {
            AddSheet(emptyList(), {}, candidate, saving = true) { _, _, _, _, _ -> saved = true }
        } }
        compose.onNodeWithText("Bỏ gợi ý, tự nhập").assertIsNotEnabled()
        compose.runOnIdle { assertFalse(saved) }
    }
}
