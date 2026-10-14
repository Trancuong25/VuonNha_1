package com.example.vuonnha.ui.plants

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vuonnha.data.*
import com.example.vuonnha.domain.*
import com.example.vuonnha.ui.common.*
import com.example.vuonnha.ui.journal.CareChart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemDetailScreen(itemId: Long, onBack: () -> Unit, onJournal: () -> Unit, vm: PlantsViewModel = viewModel()) {
    val members by vm.items.collectAsStateWithLifecycle()
    val allTasks by vm.tasks.collectAsStateWithLifecycle()
    val allLogs by vm.logs.collectAsStateWithLifecycle()
    val diary by vm.diary.collectAsStateWithLifecycle()
    val zones by vm.zones.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }
    val member = members.firstOrNull { it.id == itemId }
    val tasks = allTasks.filter { it.itemId == itemId }.sortedBy { it.dueAt() }
    val logs = allLogs.filter { it.itemId == itemId }.sortedByDescending { it.at }
    val entries = diary.filter { it.itemId == itemId }
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { TextButton(onBack) { Text("‹ Quay lại") } }
        if (member == null) {
            item { EmptyState("🪴", "Chưa có thông tin", "Dữ liệu đang tải hoặc mục này đã được xoá.") }
        } else {
            val species = findSpecies(member.speciesId)
            val profile = runCatching { org.json.JSONObject(member.profileJson) }.getOrNull()
            item {
                EmojiAvatar(species?.emoji ?: "🌿", entries.firstOrNull { it.photoUri.isNotBlank() }?.photoUri, 96.dp)
                ScreenHeader(member.name, listOfNotNull(species?.name, zones.firstOrNull { it.id == member.zoneId }?.name).joinToString(" · "))
                species?.let { Text(it.tip, style = MaterialTheme.typography.bodyMedium) }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ editing = true }) { Text("Sửa lịch & thông tin") }
                    Button(onJournal) { Text("Ghi nhật ký") }
                }
            }
            if (profile != null) item {
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Thông tin cây", style = MaterialTheme.typography.titleMedium)
                        listOf("scientificName" to "Tên khoa học", "cultivar" to "Giống", "actualHeight" to "Chiều cao hiện tại (cm)",
                            "matureHeight" to "Chiều cao trưởng thành tham khảo", "humidity" to "Độ ẩm không khí", "soil" to "Đất / tưới", "light" to "Ánh sáng tham khảo", "location" to "Vị trí", "actualLight" to "Ánh sáng nơi trồng", "pottingMix" to "Chậu & giá thể", "source" to "Nguồn tham khảo").forEach { (key,label) ->
                            profile.optString(key).takeIf { it.isNotBlank() }?.let { ProfileInfo(label, it) }
                        }
                    }
                }
            }
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${tasks.count { it.daysLeft() <= 0 }} việc đến hạn · ${entries.size} mốc nhật ký")
                        Text(logs.firstOrNull()?.let { "Chăm sóc gần nhất: ${it.task} · ${fmt.format(Date(it.at))}" }
                            ?: "Chưa có lịch sử hoàn thành.")
                    }
                }
            }
            item { Text("Lịch chăm sóc", style = MaterialTheme.typography.titleLarge) }
            if (tasks.isEmpty()) item { Text("Chưa có việc. Bấm Sửa lịch & thông tin để thêm.") }
            items(tasks, key = { "task-${it.id}" }) { task ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${task.icon()} ${task.name}", style = MaterialTheme.typography.titleMedium)
                        Text("Mỗi ${task.everyDays} ngày · Đến hạn ${fmt.format(Date(task.dueAt())).substringBefore(' ')}")
                        StatusPill(statusText(task.daysLeft()), task.daysLeft())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton({ vm.snooze(task) }) { Text("Hoãn 1 ngày") }
                            Button({ vm.done(task) }) { Text("Hoàn thành") }
                        }
                    }
                }
            }
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Chăm sóc 14 ngày qua", style = MaterialTheme.typography.titleMedium)
                        CareChart(logs)
                    }
                }
            }
            item { Text("Nhật ký gần đây", style = MaterialTheme.typography.titleLarge) }
            if (entries.isEmpty()) item { Text("Chưa có mốc ảnh hoặc ghi chú. Bấm Ghi nhật ký để thêm.") }
            items(entries.take(3), key = { "entry-${it.id}" }) { entry ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(fmt.format(Date(entry.at)), style = MaterialTheme.typography.labelLarge)
                        if (entry.note.isNotBlank()) Text(entry.note)
                        if (entry.photoUri.isNotBlank()) PhotoImage(entry.photoUri, Modifier.fillMaxWidth().height(180.dp))
                    }
                }
            }
            item { TextButton(onJournal) { Text("Xem toàn bộ nhật ký & so sánh ảnh") } }
            item { Text("Lịch sử chăm sóc gần đây", style = MaterialTheme.typography.titleLarge) }
            items(logs.take(10), key = { "log-${it.id}" }) { log ->
                ListItem(headlineContent = { Text("✓ ${log.task}") }, supportingContent = { Text(fmt.format(Date(log.at))) })
            }
        }
    }
    if (editing && member != null) EditSheet(member, tasks, zones, vm) { editing = false }
}
