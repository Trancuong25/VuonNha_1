package com.example.vuonnha

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.vuonnha.data.PlantCandidate
import com.example.vuonnha.ui.plants.AddSheet
import com.example.vuonnha.ui.plants.PlantRecognitionResult
import com.example.vuonnha.ui.theme.VuonNhaTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class RecognitionFlowTest {
    @get:Rule val compose = createComposeRule()
    private val candidate = PlantCandidate("Kim tiền", "Zamioculcas zamiifolia", "medium",
        listOf("Lá kép"), "Kiểm tra đất trước khi tưới.", emptyList())

    @Test fun nextReplacesResultAndBackRestoresItWithoutSaving() {
        var saved = false
        compose.setContent {
            var adding by remember { mutableStateOf(false) }
            VuonNhaTheme {
                if (adding) AddSheet(emptyList(), { adding = false }, candidate, fullScreen = true) { _, _, _, _, _ -> saved = true }
                else PlantRecognitionResult(listOf(candidate), candidate, null, {}, { adding = true }, {}, {})
            }
        }
        compose.onNodeWithText("Kết quả nhận diện").assertExists()
        compose.onNodeWithText("Tiếp theo").performClick()
        compose.onNodeWithText("Kết quả nhận diện").assertDoesNotExist()
        compose.onNodeWithText("Thông tin bổ sung về cây").performScrollTo().performClick()
        compose.onNodeWithText("Tên khoa học / loài").performScrollTo().assertExists()
        compose.onNodeWithText("‹ Quay lại kết quả").performScrollTo().performClick()
        compose.onNodeWithText("Kết quả nhận diện").assertExists()
        compose.runOnIdle { assertFalse(saved) }
    }

    @Test fun nicknameDoesNotReplaceSpeciesOrScientificName() {
        var nickname: String? = null
        var profile: String? = null
        var speciesId: String? = null
        compose.setContent { VuonNhaTheme {
            AddSheet(emptyList(), {}, candidate, fullScreen = true) { name, species, _, _, _ ->
                nickname = name; profile = species.profileJson; speciesId = species.id
            }
        } }
        compose.onNodeWithText("Tiếp tục").performClick()
        compose.onNodeWithText("Tên gọi riêng (không bắt buộc)").performScrollTo().performTextReplacement("Bé Lá")
        compose.onNodeWithText("Tiếp tục").performClick()
        compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performClick()
  compose.onNodeWithText("Xác nhận lưu").performClick()
        compose.runOnIdle {
            assertEquals("Bé Lá", nickname)
            assertTrue(requireNotNull(profile).contains("Zamioculcas zamiifolia"))
            assertEquals("Kim tiền", com.example.vuonnha.data.findSpecies(requireNotNull(speciesId))?.name)
        }
    }

    @Test fun repeatedKeyboardInputAndGroupSwitchesKeepFormResponsive() {
        compose.setContent { VuonNhaTheme {
            AddSheet(emptyList(), {}, fullScreen = true) { _, _, _, _, _ -> }
        } }
        repeat(4) { index ->
            compose.onNodeWithText("Tên loài hoặc mô tả *").performScrollTo().performClick().performTextReplacement("Cây thử $index")
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            compose.onNodeWithText("🐟 Cá cảnh").performScrollTo().performClick()
            compose.onNodeWithText("🪴 Cây cảnh").performScrollTo().performClick()
        }
        compose.onNodeWithText("Tiếp tục").performClick()
        compose.onNodeWithText("Tên gọi riêng (không bắt buộc)").performScrollTo().performClick().performTextReplacement("Chậu bàn học")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Tiếp tục").performClick()
        compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).assertIsEnabled()
    }
}
