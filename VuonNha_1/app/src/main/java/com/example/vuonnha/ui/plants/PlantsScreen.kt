package com.example.vuonnha.ui.plants

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vuonnha.data.*
import com.example.vuonnha.domain.daysLeft
import com.example.vuonnha.ui.common.*

@Composable fun PlantsScreen(vm: PlantsViewModel = viewModel()) {
    val list by vm.items.collectAsStateWithLifecycle()
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val diary by vm.diary.collectAsStateWithLifecycle()
    val zones by vm.zones.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Item?>(null) }
    var deleting by remember { mutableStateOf<Item?>(null) }
    var managing by remember { mutableStateOf(false) }
    var zoneFilter by remember { mutableStateOf<Long?>(null) }
    val sel = zoneFilter?.takeIf { id -> zones.any { it.id == id } }
    val zoneById = remember(zones) { zones.associateBy { it.id } }
    val byItem = remember(tasks) { tasks.groupBy { it.itemId } }
    // diary đã sắp xếp mới -> cũ, nên first() là ảnh mới nhất
    val covers = remember(diary) { diary.filter { it.photoUri.isNotEmpty() }.groupBy { it.itemId }.mapValues { it.value.first().photoUri } }
    val shown = remember(list, query, sel) { list.filter { it.name.contains(query.trim(), ignoreCase = true) && (sel == null || it.zoneId == sel) } }
    val dueCount = tasks.count { it.daysLeft() <= 0 }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ScreenHeader(
                    "Vườn nhà",
                    if (list.isEmpty()) "Bắt đầu với cây hoặc thú cưng đầu tiên"
                    else "${list.size} thành viên · $dueCount việc cần làm"
                )
            }
            item {
                OutlinedTextField(
                    query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, shape = CircleShape,
                    placeholder = { Text("Tìm cây, cá, thú cưng…") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) }
                )
            }
            item {
                ZoneFilterRow(zones, sel, { zoneFilter = it }) { AssistChip({ managing = true }, label = { Text("＋ Khu vực") }) }
            }
            if (list.isEmpty()) item { EmptyState("🪴", "Chưa có gì trong vườn", "Bấm Thêm để chọn loài và nhận lịch chăm sóc gợi ý.") }
            else if (shown.isEmpty()) item { EmptyState("🔍", "Không tìm thấy", "Thử một từ khoá khác.") }
            items(shown, key = { it.id }) { p ->
                PlantCard(p, byItem[p.id] ?: emptyList(), covers[p.id], p.zoneId?.let { zoneById[it] }, { editing = p }, { deleting = p })
            }
        }
        ExtendedFloatingActionButton(
            onClick = { adding = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Thêm") }
        )
    }

    if (adding) AddSheet(zones, { adding = false }) { name, s, cycles, zid -> vm.addItem(name, s, cycles, zid); adding = false }
    editing?.let { EditSheet(it, byItem[it.id] ?: emptyList(), zones, vm) { editing = null } }
    if (managing) ZonesSheet(zones, { n, o, c, e -> vm.addZone(n, o, c, e) }, { z -> vm.removeZone(z.id) }) { managing = false }
    deleting?.let { d ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Xoá \"${d.name}\"?") },
            text = { Text("Toàn bộ lịch nhắc và nhật ký của mục này sẽ bị xoá.") },
            confirmButton = { TextButton(onClick = { vm.removeItem(d.id); deleting = null }) { Text("Xoá") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Huỷ") } })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun PlantCard(item: Item, tasks: List<CareTask>, cover: String?, zone: Zone?, onEdit: () -> Unit, onDelete: () -> Unit) {
    val sp = findSpecies(item.speciesId)
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiAvatar(sp?.emoji ?: "🌿", cover, 60.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(listOfNotNull(sp?.name, zone?.let { zoneLabel(it) }).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onEdit) { Icon(Icons.Outlined.Edit, "Sửa") }
                IconButton(onDelete) { Icon(Icons.Outlined.Delete, "Xoá", tint = MaterialTheme.colorScheme.error) }
            }
            if (tasks.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { t -> val d = t.daysLeft(); StatusPill("${t.icon()} ${t.name} · ${statusText(d)}", d) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable private fun AddSheet(zones: List<Zone>, onDismiss: () -> Unit, onSave: (String, Species, List<Int>, Long?) -> Unit) {
    var sp by remember { mutableStateOf(SPECIES.first()) }
    var name by remember { mutableStateOf("") }
    var zoneId by remember { mutableStateOf<Long?>(null) }
    var cycles by remember(sp) { mutableStateOf(sp.tasks.map { it.second }) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)
                .navigationBarsPadding().imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Thêm vào vườn", style = MaterialTheme.typography.headlineSmall)
            Text("Chọn loài", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SPECIES.forEach { s -> FilterChip(selected = s.id == sp.id, onClick = { sp = s }, label = { Text("${s.emoji} ${s.name}") }) }
            }
            Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text("💡 ${sp.tip}", Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Tên gọi (VD: ${sp.name} ban công)") })
            if (zones.isNotEmpty()) {
                Text("Khu vực", style = MaterialTheme.typography.titleSmall)
                ZonePicker(zones, zoneId) { zoneId = it }
            }
            Text("Chu kỳ chăm sóc", style = MaterialTheme.typography.titleSmall)
            sp.tasks.forEachIndexed { i, (n, _) ->
                CycleStepper("${taskEmoji(n)} $n", cycles[i]) { v -> cycles = cycles.toMutableList().also { it[i] = v } }
            }
            Button({ onSave(name.trim().ifEmpty { sp.name }, sp, cycles, zoneId) }, Modifier.fillMaxWidth().height(52.dp)) { Text("Thêm vào vườn") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable private fun EditSheet(item: Item, tasks: List<CareTask>, zones: List<Zone>, vm: PlantsViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(item.name) }
    var zoneId by remember { mutableStateOf(item.zoneId) }
    val cycles = remember { mutableStateMapOf<Long, Int>() }
    var newName by remember { mutableStateOf("") }
    var newDays by remember { mutableIntStateOf(7) }
    var pickedType by remember { mutableStateOf<TaskType?>(null) }
    val effType = pickedType ?: TaskType.infer(newName) // chưa chọn thì đoán theo tên
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)
                .navigationBarsPadding().imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Sửa thông tin", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Tên gọi") })
            if (zones.isNotEmpty()) ZonePicker(zones, zoneId) { zoneId = it }
            tasks.forEach { t ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) {
                        CycleStepper("${t.icon()} ${t.name}", cycles[t.id] ?: t.everyDays) { cycles[t.id] = it }
                    }
                    IconButton({ vm.removeTask(t) }) { Icon(Icons.Outlined.Delete, "Xoá việc") }
                }
            }
            HorizontalDivider()
            Text("Thêm việc mới", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(newName, { newName = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Tên việc (VD: Phun sương)") })
            Text("Loại việc", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskType.entries.forEach { ty ->
                    FilterChip(selected = ty == effType, onClick = { pickedType = ty }, label = { Text("${ty.emoji} ${ty.label}") })
                }
            }
            CycleStepper("Lặp lại mỗi", newDays) { newDays = it }
            OutlinedButton(enabled = newName.isNotBlank(), onClick = {
                vm.addTask(item.id, newName.trim(), newDays, effType); newName = ""; pickedType = null
            }) { Text("Thêm việc") }
            Button({
                vm.editItem(item, name.trim().ifEmpty { item.name }, zoneId)
                tasks.forEach { t -> cycles[t.id]?.let { d -> if (d != t.everyDays) vm.updateCycle(t, d) } }
                onDismiss()
            }, Modifier.fillMaxWidth().height(52.dp)) { Text("Lưu") }
        }
    }
}
