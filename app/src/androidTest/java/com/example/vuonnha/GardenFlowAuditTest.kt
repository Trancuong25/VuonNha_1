package com.example.vuonnha

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vuonnha.data.*
import com.example.vuonnha.ui.plants.AddSheet
import com.example.vuonnha.ui.reminders.CareCalendarScreen
import com.example.vuonnha.ui.theme.VuonNhaTheme
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

/** Local integration checks. Opt-in avoids touching a developer's garden or requiring a server in normal tests. */
class GardenFlowAuditTest {
    @get:Rule val compose = createComposeRule()
    private val arguments get() = InstrumentationRegistry.getArguments()

    @Test fun lookupKeepsScheduleAlreadyWrittenByUser() {
        assumeTrue(arguments.getString("flowAudit") == "true")
        var saved: Species? = null
        compose.setContent { VuonNhaTheme { AddSheet(emptyList(), {}) { _, s, _, _, _ -> saved = s } } }
        compose.onNodeWithText("Tên loài hoặc mô tả *").performScrollTo().performTextInput("Kim tiền")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        // Exercise the chip action directly: scrolling the bottom sheet can move
        // its touch target during the synthetic tap on this emulator.
        compose.onNodeWithText("＋ Kiểm tra độ ẩm đất").performScrollTo()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onNodeWithText("Kiểm tra độ ẩm đất").performScrollTo().assertExists()
        compose.onNodeWithText("Tra cứu thông tin cây").performScrollTo().performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithText("Kim tiền · Zamioculcas zamiifolia").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Kim tiền · Zamioculcas zamiifolia").performScrollTo().performClick()
        compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("Kiểm tra độ ẩm đất" to 3), requireNotNull(saved).tasks) }
    }

    @Test fun unavailableBackendKeepsDraftAndAllowsManualSave() {
        assumeTrue(arguments.getString("offlineAudit") == "true")
        var saved: Species? = null
        compose.setContent { VuonNhaTheme { AddSheet(emptyList(), {}) { _, s, _, _, _ -> saved = s } } }
        compose.onNodeWithText("Tên loài hoặc mô tả *").performScrollTo().performTextInput("Cây táo")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Tra cứu thông tin cây").performScrollTo().performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithText("Không kết nối được danh mục cây. Bạn vẫn có thể tự nhập.").fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(hasText("Thêm vào vườn") and hasClickAction()).performScrollTo().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals("Cây táo", requireNotNull(saved).name) }
        runBlocking {
            val base = InstrumentationRegistry.getInstrumentation().targetContext
            val species = requireNotNull(saved)
            val id = CareRepository.get(base).addItem("Táo tự nhập", species, emptyList(), null)
            try { assertEquals("Táo tự nhập", AppDb.get(base).dao().itemOnce(id)?.name) }
            finally { AppDb.get(base).dao().deleteItem(id) }
        }
    }

    @Test fun savedCareAppearsInCalendarAssistantAndBackup() = runBlocking {
        assumeTrue(arguments.getString("flowAudit") == "true")
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val dao = AppDb.get(base).dao()
        assumeTrue("Use an empty isolated Audit emulator", dao.itemsOnce().isEmpty())
        val isolated = object : ContextWrapper(base) {
            override fun getSharedPreferences(name: String, mode: Int) = base.getSharedPreferences("flow_audit_$name", mode)
        }
        val assistant = AssistantRepository(isolated)
        val repo = CareRepository.get(base)
        val id = repo.addItem("Kim tiền bàn học", customSpecies("Kim tiền", SpeciesGroup.PLANT, listOf("Tưới nước" to 7)), listOf(7), null)
        try {
            val task = dao.tasksOnce().single { it.itemId == id }
            repo.done(task)
            assertEquals(1, dao.logsOnce().count { it.itemId == id && it.task == "Tưới nước" })
            compose.setContent { VuonNhaTheme { CareCalendarScreen({}, {}) } }
            compose.onNodeWithTag("care-calendar").performScrollToNode(hasText("✓ Tưới nước"))
            compose.onNodeWithText("✓ Tưới nước").assertExists()
            val messages = assistant.ask("Hôm nay tôi đã tưới cây nào?")
            assertTrue(messages.last().text.contains("Kim tiền bàn học"))
            val archive = ByteArrayOutputStream()
            repo.exportZip(archive)
            val json = ZipInputStream(ByteArrayInputStream(archive.toByteArray())).use { zip ->
                var entry = zip.nextEntry
                while (entry != null && entry.name != "data.json") entry = zip.nextEntry
                assertNotNull(entry)
                JSONObject(String(zip.readBytes(), Charsets.UTF_8))
            }
            assertEquals("Kim tiền bàn học", json.getJSONArray("items").getJSONObject(0).getString("name"))
            assertEquals("Tưới nước", json.getJSONArray("logs").getJSONObject(0).getString("task"))
        } finally {
            try { assistant.clear(true) } finally { dao.deleteItem(id) }
            isolated.getSharedPreferences("garden_assistant", Context.MODE_PRIVATE).edit().clear().commit()
        }
    }
}
