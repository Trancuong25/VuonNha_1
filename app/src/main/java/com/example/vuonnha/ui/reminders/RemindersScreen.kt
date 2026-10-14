package com.example.vuonnha.ui.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vuonnha.data.*
import com.example.vuonnha.domain.daysLeft
import com.example.vuonnha.domain.dueAt
import com.example.vuonnha.ui.common.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/** Ngữ cảnh dùng chung cho các thẻ việc: cây, khu vực và dự báo của hôm nay. */
private class Ctx(val items: Map<Long, Item>, val zones: Map<Long, Zone>, val fc: DayForecast?)

@Composable fun RemindersScreen(vm: RemindersViewModel = viewModel(), onCalendar: () -> Unit = {}) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val items by vm.items.collectAsStateWithLifecycle()
    val zones by vm.zones.collectAsStateWithLifecycle()
    val weather by vm.weather.collectAsStateWithLifecycle()
    var zoneFilter by remember { mutableStateOf<Long?>(null) }
    val sel = zoneFilter?.takeIf { id -> zones.any { it.id == id } }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val ctx = remember(items, zones, weather) {
        Ctx(items.associateBy { it.id }, zones.associateBy { it.id }, weather?.days?.firstOrNull { it.date == todayStr })
    }
    val sorted = remember(tasks, ctx, sel) {
        tasks.filter { sel == null || ctx.items[it.itemId]?.zoneId == sel }.sortedBy { it.dueAt() }
    }
    val late = remember(sorted) { sorted.filter { it.daysLeft() < 0 } }
    val dueToday = remember(sorted) { sorted.filter { it.daysLeft() == 0 } }
    val soon = remember(sorted) { sorted.filter { it.daysLeft() > 0 } }
    val date = remember { SimpleDateFormat("EEEE, dd/MM", Locale("vi", "VN")).format(Date()).replaceFirstChar { it.uppercase() } }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenHeader("Hôm nay", date) }
        item { OutlinedButton(onCalendar, Modifier.fillMaxWidth()) { Text("📅 Xem lịch tuần / tháng") } }
        if (zones.isNotEmpty()) item { ZoneFilterRow(zones, sel, { zoneFilter = it }) }
        if (tasks.isEmpty()) {
            item { EmptyState("🌱", "Chưa có lịch nhắc", "Thêm cây hoặc thú cưng ở Vườn nhà để đặt lịch chăm sóc.") }
        } else {
            item { SummaryCard(late.size + dueToday.size, late.size) }
            section("Quá hạn", late, ctx, vm)
            section("Cần làm hôm nay", dueToday, ctx, vm)
            section("Sắp tới", soon, ctx, vm)
            if (sorted.isEmpty()) item { EmptyState("🔍", "Khu vực này chưa có việc", "Gán cây vào khu vực ở Vườn nhà.") }
        }
    }
}

private fun LazyListScope.section(title: String, list: List<CareTask>, ctx: Ctx, vm: RemindersViewModel) {
    if (list.isEmpty()) return
    item { Text(title, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    items(list, key = { it.id }) { t -> TaskCard(t, ctx, { vm.done(t) }, { vm.snooze(t) }) }
}

/** Gợi ý theo thời tiết: chỉ cho việc tưới ở khu vực ngoài trời. */
private fun hintFor(t: CareTask, zone: Zone?, fc: DayForecast?, d: Int): String? {
    if (fc == null || zone == null || !zone.outdoor || d > 0 || t.type != TaskType.WATER.name) return null
    return when {
        !zone.covered && fc.isRainy() -> "🌧️ Hôm nay có mưa (~${fc.rainMm.roundToInt()} mm), có thể bỏ tưới. Bấm Hoãn nếu muốn."
        fc.isHot() -> "🔥 Nắng nóng ${fc.tMax.roundToInt()}°C, nên tưới sáng sớm hoặc chiều tối."
        else -> null
    }
}

@Composable private fun SummaryCard(open: Int, late: Int) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(20.dp)) {
            if (open == 0) {
                Text("🎉 Xong hết rồi!", style = MaterialTheme.typography.titleLarge)
                Text("Vườn của bạn hôm nay đã được chăm đủ.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text("$open việc cần làm", style = MaterialTheme.typography.titleLarge)
                Text(if (late > 0) "Trong đó $late việc đã quá hạn." else "Chưa có việc nào quá hạn.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable private fun TaskCard(t: CareTask, ctx: Ctx, onDone: () -> Unit, onSnooze: () -> Unit) {
    val d = t.daysLeft()
    val item = ctx.items[t.itemId]
    val zone = item?.zoneId?.let { ctx.zones[it] }
    val hint = hintFor(t, zone, ctx.fc, d)
    val sub = listOfNotNull(item?.name, zone?.let { zoneLabel(it) }, statusText(d)).joinToString(" · ")
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
        containerColor = if (d < 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    )) {
        Row(Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiAvatar(t.icon(), null, 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(t.name, style = MaterialTheme.typography.titleMedium)
                Text(sub, style = MaterialTheme.typography.bodySmall, color = statusColor(d))
                if (hint != null) Text(hint, Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
            IconButton(onSnooze) { Icon(Icons.Outlined.Snooze, "Hoãn 1 ngày") }
            FilledIconButton(onDone) { Icon(Icons.Filled.Check, "Hoàn thành") }
        }
    }
}
