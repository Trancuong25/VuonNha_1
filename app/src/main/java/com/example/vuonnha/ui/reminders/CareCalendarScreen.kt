package com.example.vuonnha.ui.reminders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vuonnha.data.icon
import com.example.vuonnha.domain.*
import com.example.vuonnha.ui.common.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import kotlinx.coroutines.launch

@Composable
fun CareCalendarScreen(onBack: () -> Unit, onOpen: (Long) -> Unit, vm: RemindersViewModel = viewModel(), onJournal: () -> Unit = {}) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val members by vm.items.collectAsStateWithLifecycle()
    val logs by vm.logs.collectAsStateWithLifecycle()
    val diary by vm.diary.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    var selectedDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    var anchorDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    var monthMode by rememberSaveable { mutableStateOf(true) }
    val selected = LocalDate.ofEpochDay(selectedDay)
    val anchor = LocalDate.ofEpochDay(anchorDay)
    val start = if (monthMode) anchor.withDayOfMonth(1) else anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val end = if (monthMode) start.plusMonths(1).minusDays(1) else start.plusDays(6)
    val byId = members.associateBy { it.id }
    val planned = remember(tasks, members, start, end, today) { tasks.filter { it.itemId in byId }.flatMap { it.occurrences(start, end, today) } }
    val plansByDay = remember(planned) { planned.groupBy { it.date } }
    val logsByDay = remember(logs) { logs.groupBy { dayOf(it.at) } }
    val notesByDay = remember(diary) { diary.groupBy { dayOf(it.at) } }
    val selectedPlans = plansByDay[selected].orEmpty()
    val selectedLogs = logsByDay[selected].orEmpty().sortedByDescending { it.at }
    val selectedNotes = notesByDay[selected].orEmpty().sortedByDescending { it.at }
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LazyColumn(Modifier.testTag("care-calendar"), state = listState, contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            ScreenHeader("Chăm sóc", "Lịch nhắc, việc đã làm và nhật ký theo ngày")
            OutlinedButton(onJournal, Modifier.fillMaxWidth()) { Text("＋ Ghi chú hoặc ảnh phát triển") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(!monthMode, { monthMode = false; anchorDay = selectedDay }, label = { Text("Tuần") })
                FilterChip(monthMode, { monthMode = true; anchorDay = selectedDay }, label = { Text("Tháng") })
                TextButton({ selectedDay = today.toEpochDay(); anchorDay = selectedDay }) { Text("Hôm nay") }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton({
                            val next = if (monthMode) start.minusMonths(1) else start.minusWeeks(1)
                            anchorDay = next.toEpochDay(); selectedDay = anchorDay
                        }) { Text("‹", style = MaterialTheme.typography.titleLarge) }
                        Text(if (monthMode) "Tháng ${start.monthValue}/${start.year}" else "${start.format(fmt)} – ${end.format(fmt)}",
                            Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                        TextButton({
                            val next = if (monthMode) start.plusMonths(1) else start.plusWeeks(1)
                            anchorDay = next.toEpochDay(); selectedDay = anchorDay
                        }) { Text("›", style = MaterialTheme.typography.titleLarge) }
                    }
                    Row { listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(it, style = MaterialTheme.typography.labelMedium) }
                    } }
                    val gridStart = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val cells = if (monthMode) ((start.dayOfWeek.value - 1 + start.lengthOfMonth() + 6) / 7) * 7 else 7
                    (0 until cells step 7).forEach { row ->
                        Row {
                            (0..6).forEach { col ->
                                val date = gridStart.plusDays((row + col).toLong())
                                val inRange = date in start..end
                                val count = plansByDay[date].orEmpty().size
                                val completed = logsByDay[date].orEmpty().size
                                val notes = notesByDay[date].orEmpty().size
                                Surface(
                                    modifier = Modifier.weight(1f).heightIn(min = 64.dp).padding(2.dp)
                                        .semantics(mergeDescendants = true) {
                                            if (inRange) {
                                                contentDescription = "Ngày ${date.format(fmt)}${if (date == today) ", hôm nay" else ""}, $count việc theo lịch, $completed lượt đã làm, $notes ghi chú"
                                                this.selected = date == selected
                                            }
                                        }
                                        .clickable(enabled = inRange, role = Role.Button) { selectedDay = date.toEpochDay(); scope.launch { listState.animateScrollToItem(2) } },
                                    shape = MaterialTheme.shapes.small,
                                    color = if (date == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Text(if (inRange) date.dayOfMonth.toString() else "", color = if (date == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                        if (inRange && count > 0) Text("$count việc", style = MaterialTheme.typography.labelSmall,
                                            color = if (date < today) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                                        if (inRange && completed > 0) Text("✓ $completed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        if (inRange && notes > 0) Text("• Ghi chú", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                    Text("✓ Đã hoàn thành · Việc sau lần đến hạn tiếp theo là dự kiến và sẽ đổi khi bạn hoàn thành hoặc hoãn.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(selected.format(fmt), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
            TextButton({ scope.launch { listState.animateScrollToItem(1) } }) { Text("Đổi ngày") }
        } }
        if (selectedPlans.isEmpty() && selectedLogs.isEmpty() && selectedNotes.isEmpty()) item { EmptyState("📅", "Ngày này chưa có hoạt động", "Lịch nhắc và những lần bạn ghi nhận chăm sóc sẽ xuất hiện ở đây.") }
        if (selectedPlans.isNotEmpty()) item { Text("Việc theo lịch", style = MaterialTheme.typography.titleMedium) }
        items(selectedPlans, key = { "plan-${it.task.id}" }) { plan ->
            ElevatedCard(Modifier.fillMaxWidth().clickable { onOpen(plan.task.itemId) }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${plan.task.icon()} ${plan.task.name}", style = MaterialTheme.typography.titleMedium)
                    Text("${byId[plan.task.itemId]?.name.orEmpty()} · ${if (plan.projected) "Dự kiến" else statusText(plan.task.daysLeft())}")
                    if (!plan.projected) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton({ vm.snooze(plan.task) }) { Text("Hoãn 1 ngày") }
                        Button({ vm.done(plan.task) }) { Text("Hoàn thành") }
                    }
                }
            }
        }
        if (selectedLogs.isNotEmpty()) item { Text("Đã chăm sóc trong ngày", style = MaterialTheme.typography.titleMedium) }
        items(selectedLogs, key = { "log-${it.id}" }) { log ->
            ListItem(headlineContent = { Text("✓ ${log.task}") }, supportingContent = { Text(byId[log.itemId]?.name.orEmpty()) },
                modifier = Modifier.clickable { onOpen(log.itemId) })
        }
        if (selectedNotes.isNotEmpty()) item { Text("Nhật ký trong ngày", style = MaterialTheme.typography.titleMedium) }
        items(selectedNotes, key = { "note-${it.id}" }) { note ->
            OutlinedCard(Modifier.fillMaxWidth().clickable { onOpen(note.itemId) }) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(byId[note.itemId]?.name.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    if (note.note.isNotBlank()) Text(note.note)
                    if (note.photoUri.isNotBlank()) PhotoImage(note.photoUri, Modifier.fillMaxWidth().height(180.dp))
                }
            }
        }
    }
}
