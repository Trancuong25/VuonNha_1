package com.example.vuonnha

import android.app.Application
import android.content.ContextWrapper
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vuonnha.data.*
import com.example.vuonnha.ui.settings.AccountScreen
import com.example.vuonnha.ui.settings.AccountViewModel
import com.example.vuonnha.ui.theme.VuonNhaTheme
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.runBlocking

/** Uses a separate in-memory backend on 8788, never the user's accounts database. */
class AccountFlowTest {
    @get:Rule val compose = createComposeRule()
    @Test fun registerLogoutWrongPasswordAndLoginUseRealBackend() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("accountAudit") == "true")
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        assumeTrue("Run only as guest in the isolated emulator", AccountSession.user.value == null)
        val isolated = object : ContextWrapper(app) {
            override fun getSharedPreferences(name: String, mode: Int) = app.getSharedPreferences("account_audit_$name", mode)
        }
        val repo = AccountRepository(isolated, "http://127.0.0.1:8788")
        val vm = AccountViewModel(app, repo)
        val username = "audit_" + java.lang.Long.toUnsignedString(System.nanoTime(), 16)
        val password = "garden-test-password-42"
        try {
            compose.setContent { VuonNhaTheme { AccountScreen({}, vm) } }
            compose.onNodeWithText("Tạo tài khoản", substring = false).performClick()
            compose.onNodeWithText("Tên hiển thị").performScrollTo().performTextInput("Người thử vườn")
            compose.onNodeWithText("Tên đăng nhập", substring = false).performScrollTo().performTextInput(username)
            compose.onNodeWithText("Mật khẩu", substring = false).performScrollTo().performTextInput(password)
            compose.onNodeWithText("Nhập lại mật khẩu").performScrollTo().performTextInput(password)
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            compose.onNodeWithText("Tạo tài khoản và đăng nhập").performScrollTo().performClick()
            compose.waitUntil(30000) { AccountSession.user.value?.username == username && !vm.busy }
            compose.onNodeWithText("Đã đăng nhập").assertExists()
            val stored = isolated.getSharedPreferences("account_session", 0).getString("encrypted", "").orEmpty()
            assertTrue(stored.isNotBlank()); assertFalse(stored.contains(password)); assertFalse(stored.contains(AccountSession.credential(isolated)!!))
            compose.onNodeWithText("Đăng xuất").performScrollTo().performClick()
            compose.onAllNodesWithText("Đăng xuất").onLast().performClick()
            compose.waitUntil(15000) { AccountSession.user.value == null }
            compose.onNodeWithText("Đăng nhập", substring = false).performScrollTo().performClick()
            compose.onNodeWithText("Mật khẩu", substring = false).performScrollTo().performTextInput("wrong-password-42")
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            compose.onNodeWithText("Đăng nhập vào Vườn Nhà").performScrollTo().performClick()
            compose.waitUntil(15000) { compose.onAllNodesWithText("Tên đăng nhập hoặc mật khẩu chưa đúng.").fetchSemanticsNodes().isNotEmpty() }
            assertNull(AccountSession.user.value)
            compose.onNodeWithText("Mật khẩu", substring = false).performScrollTo().performTextReplacement(password)
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            compose.onNodeWithText("Đăng nhập vào Vườn Nhà").performScrollTo().performClick()
            compose.waitUntil(15000) { AccountSession.user.value?.username == username }
        } finally {
            try { if (AccountSession.credential(isolated) != null) runBlocking { repo.logout() } }
            finally { AccountSession.clear(isolated) }
        }
    }
}
