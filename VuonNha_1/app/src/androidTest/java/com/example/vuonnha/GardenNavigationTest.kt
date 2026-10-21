package com.example.vuonnha

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vuonnha.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import android.graphics.Bitmap
import java.io.File

class GardenNavigationTest {
 @get:Rule val compose=createAndroidComposeRule<MainActivity>()
 private fun screenshot(name:String) {
  // Captures are supplemental artifacts. A sleeping emulator can return null;
  // functional assertions must still execute rather than abort on Bitmap.compress.
  val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot() ?: return
  File(compose.activity.getExternalFilesDir(null),name).outputStream().use{bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
  bitmap.recycle()
 }
 @Test fun accountCanOpenFromHomeAndReturnWithoutLogin() {
  compose.onNodeWithContentDescription("Tài khoản").performClick()
  compose.onNodeWithText("Tên đăng nhập",substring=false).assertExists()
  screenshot("account-screen.png")
  compose.onNodeWithText("‹ Quay lại").performClick()
  compose.onAllNodesWithText("Vườn Nhà",substring=false).onFirst().assertExists()
 }
 @Test fun calendarShortcutAndCareTabAlwaysAllowReturningHome() {
  repeat(3) {
   compose.onNodeWithText("Lịch chăm sóc",substring=false).performClick()
   compose.onNodeWithText("＋ Ghi chú hoặc ảnh phát triển").assertExists()
   compose.onNode(hasText("Vườn Nhà") and hasClickAction()).performClick()
   compose.onNodeWithText("Thêm cây vào vườn",substring=false).assertExists()
   compose.onNode(hasText("Chăm sóc") and hasClickAction()).performClick()
   compose.onNodeWithText("＋ Ghi chú hoặc ảnh phát triển").assertExists()
   compose.onNode(hasText("Vườn Nhà") and hasClickAction()).performClick()
   compose.onNodeWithText("Thêm cây vào vườn",substring=false).assertExists()
  }
 }
 @Test fun navigationScreensAreAccessibleWithoutSendingChat() {
  compose.onAllNodesWithText("Vườn Nhà",substring=false).onFirst().assertExists()
  screenshot("redesign-home.png")
  compose.onNode(hasText("Chăm sóc") and hasClickAction()).performClick()
  compose.onNodeWithText("＋ Ghi chú hoặc ảnh phát triển").assertExists()
  screenshot("redesign-care.png")
  compose.onNode(hasText("Cài đặt") and hasClickAction()).performClick()
  compose.onNodeWithText("Giờ nhắc mỗi ngày").assertExists()
  compose.onNodeWithText("Giao diện").assertExists()
  compose.onNodeWithText("Gửi thông báo thử").assertDoesNotExist()
  screenshot("redesign-settings.png")
  compose.onNode(hasText("Trợ lý") and hasClickAction()).performClick()
  compose.onNodeWithText("Hỏi Trợ lý").assertExists()
  // Navigation checks must not add test conversations to the user's garden.
  screenshot("redesign-assistant.png")
 }
}
