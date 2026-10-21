package com.example.vuonnha

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.test.core.app.ApplicationProvider
import com.example.vuonnha.data.*
import com.example.vuonnha.ui.plants.AddSheet
import com.example.vuonnha.ui.theme.VuonNhaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.json.JSONObject

class EditablePlantProfileTest {
 @get:Rule val compose = createComposeRule()
 @Test fun reviewCanBeCancelledAndOptionalReferenceExcludedWithoutLosingLocation() {
  var saved:Species?=null
  val candidate=PlantCandidate("Kim tiền","Zamioculcas zamiifolia","medium",emptyList(),"Tham khảo",emptyList(),mapOf("light" to "Gián tiếp"))
  compose.setContent { VuonNhaTheme { AddSheet(emptyList(),{},candidate,fullScreen=true){_,species,_,_,_->saved=species} } }
  compose.onNodeWithText("Tiếp tục").performClick()
  compose.onNodeWithText("Vị trí đặt cây").performTextReplacement("Ban công")
  androidx.test.espresso.Espresso.closeSoftKeyboard()
  compose.onNodeWithText("Tiếp tục").performClick()
  compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performClick()
  compose.runOnIdle { assertNull(saved) }
  compose.onNodeWithText("Chỉnh lại").performClick()
  compose.runOnIdle { assertNull(saved) }
  compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performClick()
  compose.onNode(isToggleable()).performScrollTo().performClick()
  compose.onNodeWithText("Xác nhận lưu").performClick()
  compose.runOnIdle { val json=JSONObject(requireNotNull(saved).profileJson);assertFalse(json.has("light"));assertFalse(json.has("scientificName"));assertEquals("Ban công",json.getString("location")) }
 }
 @Test fun emptyRequiredNameShowsHelpfulErrorInsteadOfAdvancing() {
  compose.setContent { VuonNhaTheme { AddSheet(emptyList(),{},fullScreen=true){_,_,_,_,_->fail("Must not save an empty name")} } }
  compose.onNodeWithText("Tiếp tục").performClick()
  compose.onNodeWithText("Bạn chưa nhập tên cây. Chưa rõ loài thì dùng tên mô tả nhé.").performScrollTo().assertExists()
  compose.onNodeWithText("Tên loài hoặc mô tả *").performTextReplacement("Trầu bà")
  androidx.test.espresso.Espresso.closeSoftKeyboard()
  compose.onNodeWithText("Tiếp tục").performClick()
  compose.onNodeWithText("Cây sẽ ở góc nào?").assertExists()
 }
 @Test fun deletingSuggestionAndChangingFocusNeverRestoresIt() {
  var saved: Species? = null
  val candidate=PlantCandidate("Kim tiền","Zamioculcas zamiifolia","medium",emptyList(),"Tham khảo",emptyList(),mapOf("light" to "Ánh sáng gián tiếp", "soil" to "Thoát nước"))
  compose.setContent { VuonNhaTheme { AddSheet(emptyList(),{},candidate,fullScreen=true){_,species,_,_,_->saved=species} } }
  compose.onNodeWithText("Thông tin bổ sung về cây").performScrollTo().performClick()
  compose.onNodeWithText("Ánh sáng").performScrollTo().performTextReplacement("")
  compose.onNodeWithText("Tiếp tục").performClick()
  compose.onNodeWithText("Tên gọi riêng (không bắt buộc)").performScrollTo().performTextReplacement("Bé Lá")
  compose.onNodeWithText("Vị trí đặt cây").performScrollTo().performTextReplacement("Cửa sổ hướng đông")
  androidx.test.espresso.Espresso.closeSoftKeyboard()
  compose.onNodeWithText("Tiếp tục").performClick()
  compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performClick()
  compose.onNodeWithText("Xác nhận lưu").performClick()
  compose.runOnIdle {
   val profile=JSONObject(requireNotNull(saved).profileJson)
   assertEquals("",profile.getString("light"))
   assertEquals("Thoát nước",profile.getString("soil"))
   assertEquals("Cửa sổ hướng đông",profile.getString("location"))
   assertEquals("Zamioculcas zamiifolia",profile.getString("scientificName"))
  }
 }
 @Test fun formDraftSurvivesStateRestorationIncludingDeletedSuggestions() {
  val restoration=StateRestorationTester(compose)
  var savedName="";var saved:Species?=null;var savedCycles:List<Int> = emptyList()
  val candidate=PlantCandidate("Kim tiền","Zamioculcas zamiifolia","medium",emptyList(),"Tham khảo",listOf("Kiểm tra đất" to 5),mapOf("light" to "Gián tiếp"))
  restoration.setContent { VuonNhaTheme { AddSheet(emptyList(),{},candidate,fullScreen=true){name,species,cycles,_,_->savedName=name;saved=species;savedCycles=cycles} } }
  compose.onNodeWithText("Thông tin bổ sung về cây").performScrollTo().performClick()
  compose.onNodeWithText("Ánh sáng").performScrollTo().performTextReplacement("")
  compose.onNodeWithText("Tiếp tục").performClick()
  compose.onNodeWithText("Tên gọi riêng (không bắt buộc)").performScrollTo().performTextReplacement("Bé Lá")
  compose.onNodeWithText("Vị trí đặt cây").performScrollTo().performTextReplacement("Cửa sổ")
  androidx.test.espresso.Espresso.closeSoftKeyboard()
  restoration.emulateSavedInstanceStateRestore()
  compose.onNodeWithText("Cây sẽ ở góc nào?").assertExists()
  compose.onNodeWithText("Tiếp tục").performClick()
  compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performClick()
  compose.onNodeWithText("Xác nhận lưu").performClick()
  compose.runOnIdle { assertEquals("Bé Lá",savedName);assertEquals(listOf(5),savedCycles);val profile=JSONObject(requireNotNull(saved).profileJson);assertEquals("",profile.getString("light"));assertEquals("Cửa sổ",profile.getString("location")) }
 }
}
