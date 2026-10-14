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
 @Test fun deletingSuggestionAndChangingFocusNeverRestoresIt() {
  var saved: Species? = null
  val candidate=PlantCandidate("Kim tiền","Zamioculcas zamiifolia","medium",emptyList(),"Tham khảo",emptyList(),mapOf("light" to "Ánh sáng gián tiếp", "soil" to "Thoát nước"))
  compose.setContent { VuonNhaTheme { AddSheet(emptyList(),{},candidate,fullScreen=true){_,species,_,_,_->saved=species} } }
  compose.onNodeWithText("Ánh sáng").performScrollTo().performTextReplacement("")
  compose.onNodeWithText("Tên gọi riêng (không bắt buộc)").performScrollTo().performTextReplacement("Bé Lá")
  compose.onNodeWithText("Vị trí đặt cây").performScrollTo().performTextReplacement("Cửa sổ hướng đông")
  androidx.test.espresso.Espresso.closeSoftKeyboard()
  compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performScrollTo().performClick()
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
  compose.onNodeWithText("Ánh sáng").performScrollTo().performTextReplacement("")
  compose.onNodeWithText("Tên gọi riêng (không bắt buộc)").performScrollTo().performTextReplacement("Bé Lá")
  compose.onNodeWithText("Vị trí đặt cây").performScrollTo().performTextReplacement("Cửa sổ")
  androidx.test.espresso.Espresso.closeSoftKeyboard()
  restoration.emulateSavedInstanceStateRestore()
  compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performScrollTo().performClick()
  compose.runOnIdle { assertEquals("Bé Lá",savedName);assertEquals(listOf(5),savedCycles);val profile=JSONObject(requireNotNull(saved).profileJson);assertEquals("",profile.getString("light"));assertEquals("Cửa sổ",profile.getString("location")) }
 }
}
