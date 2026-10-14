package com.example.vuonnha

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.vuonnha.data.*
import com.example.vuonnha.ui.reminders.CareCalendarScreen
import com.example.vuonnha.ui.theme.VuonNhaTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CalendarDiaryTest {
 @get:Rule val compose=createComposeRule()
 @Test fun choosingDayShowsOnlyThatDaysLogAndDiary() {
  val dao=AppDb.get(ApplicationProvider.getApplicationContext<Context>()).dao()
  val today=LocalDate.now();val other=today.withDayOfMonth(if(today.dayOfMonth==1)2 else today.dayOfMonth-1)
  val otherAt=other.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()+3600000
  val todayAt=today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()+3600000
  val id=runBlocking{dao.insertItem(Item(name="Cây kiểm thử lịch",speciesId="zz"))}
  try {
   runBlocking {
    dao.insertLog(CareLog(itemId=id,task="Ghi nhận riêng ngày khác",at=otherAt))
    dao.insertEntry(DiaryEntry(itemId=id,at=todayAt,note="Lá mới ngày hiện tại"))
    dao.insertEntry(DiaryEntry(itemId=id,at=otherAt,note="Ghi chú riêng ngày khác"))
   }
   compose.setContent{VuonNhaTheme{CareCalendarScreen({}, {})}}
   compose.waitForIdle()
   compose.onNodeWithTag("care-calendar").performScrollToNode(hasText("Lá mới ngày hiện tại"))
   compose.onNodeWithText("Lá mới ngày hiện tại").assertExists()
   compose.onNodeWithText("Ghi chú riêng ngày khác").assertDoesNotExist()
   compose.onNodeWithTag("care-calendar").performScrollToNode(hasText(other.dayOfMonth.toString()))
   compose.onNodeWithText(other.dayOfMonth.toString()).performClick()
   compose.onNodeWithTag("care-calendar").performScrollToNode(hasText("Ghi chú riêng ngày khác"))
   compose.onNodeWithText("Ghi chú riêng ngày khác").assertExists()
   compose.onNodeWithText("Lá mới ngày hiện tại").assertDoesNotExist()
   compose.onNodeWithTag("care-calendar").performScrollToNode(hasText("✓ Ghi nhận riêng ngày khác"))
   compose.onNodeWithText("✓ Ghi nhận riêng ngày khác").assertExists()
  } finally {runBlocking{dao.deleteItem(id)}}
 }
}
