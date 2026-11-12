package com.example.vuonnha

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.vuonnha.ui.plants.AddSheet
import com.example.vuonnha.ui.theme.VuonNhaTheme
import org.junit.Rule
import org.junit.Test

class FishAddSheetTest {
    @get:Rule val compose = createComposeRule()

    @Test fun fishDraftShowsTankFieldsWithoutPlantConditions() {
        compose.setContent { VuonNhaTheme { AddSheet(emptyList(), {}, fullScreen = true) { _, _, _, _, _ -> } } }
        compose.onNodeWithText("Cá").performClick()
        compose.onNodeWithText("Tên loài cá *").performTextInput("Cá koi")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Tiếp tục").performClick()
        compose.onNodeWithText("＋ Thêm chi tiết").performClick()
        compose.onNodeWithText("Dung tích bể (lít)").assertExists()
        compose.onNodeWithText("Ánh sáng nơi trồng").assertDoesNotExist()
        compose.onNodeWithText("Chậu & giá thể (không bắt buộc)").assertDoesNotExist()
    }
}
