package com.example.vuonnha

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.vuonnha.ui.MainActivity
import org.junit.Rule
import org.junit.Test

/** Exercise the actual camera lifecycle, not just a standalone form with fake recognition. */
class CameraToFormTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun liveCameraToManualFormAndKeyboardRemainsResponsive() {
        com.example.vuonnha.data.ScanSettings.hide(compose.activity, false)
        compose.onNodeWithContentDescription("Quét cây bằng máy ảnh").performClick()
        compose.onNodeWithText("Tự nhập tên cây").assertExists()
        // Give the real camera time to open before leaving it.
        android.os.SystemClock.sleep(3000)
        compose.onNodeWithText("Tự nhập tên cây").performClick()
        compose.onNodeWithText("Tên loài hoặc mô tả *").performScrollTo().assertExists()
        val cameraDump = shellOutput("dumpsys media.camera")
        org.junit.Assert.assertFalse("Camera must release its client before showing the form",
            cameraDump.substringAfter("Active Camera Clients:").substringBefore("Allowed user IDs").contains("com.example.vuonnha"))
        repeat(3) { index ->
            compose.onNodeWithText("Tên loài hoặc mô tả *").performScrollTo().performClick().performTextReplacement("Cây $index")
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            compose.onNodeWithText("Tên gọi riêng (không bắt buộc)").performScrollTo().performClick().performTextClearance()
            shellOutput("input text Chau$index")
            compose.onNodeWithText("Chau$index").assertExists()
            androidx.test.espresso.Espresso.closeSoftKeyboard()
        }
        compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performScrollTo().assertIsEnabled()
        compose.onNodeWithText("‹ Quay lại máy ảnh").performScrollTo().performClick()
        compose.onNodeWithText("Quét cây").assertExists()
    }

    private fun shellOutput(command: String): String {
        val descriptor = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        return android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { String(it.readBytes(), Charsets.UTF_8) }
    }
}
