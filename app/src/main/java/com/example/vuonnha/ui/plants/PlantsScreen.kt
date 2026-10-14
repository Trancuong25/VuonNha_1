package com.example.vuonnha.ui.plants

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vuonnha.data.*
import com.example.vuonnha.domain.daysLeft
import com.example.vuonnha.domain.scheduleStart
import com.example.vuonnha.domain.dayOf
import com.example.vuonnha.domain.dueAt
import java.time.LocalDate
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import java.time.format.DateTimeFormatter
import com.example.vuonnha.ui.common.*

@Composable fun PlantsScreen(vm: PlantsViewModel = viewModel(), onOpen: (Long) -> Unit = {},
    onCalendar: () -> Unit = {}, onWeather: () -> Unit = {}, onReminders: () -> Unit = {}, onScan: () -> Unit = {}) {
    val list by vm.items.collectAsStateWithLifecycle()
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val diary by vm.diary.collectAsStateWithLifecycle()
    val zones by vm.zones.collectAsStateWithLifecycle()
    val logs by vm.logs.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var adding by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editing = list.firstOrNull { it.id == editingId }
    var deleting by remember { mutableStateOf<Item?>(null) }
    var managing by remember { mutableStateOf(false) }
    var zoneFilter by remember { mutableStateOf<Long?>(null) }
    var searching by remember { mutableStateOf(false) }
    var options by remember { mutableStateOf(false) }
    var showCare by remember { mutableStateOf(false) }
    val sel = zoneFilter?.takeIf { id -> zones.any { it.id == id } }
    val zoneById = remember(zones) { zones.associateBy { it.id } }
    val byItem = remember(tasks) { tasks.groupBy { it.itemId } }
    // diary đã sắp xếp mới -> cũ, nên first() là ảnh mới nhất
    val covers = remember(diary) { diary.filter { it.photoUri.isNotEmpty() }.groupBy { it.itemId }.mapValues { it.value.first().photoUri } }
    val shown = remember(list, query, sel) { list.filter {
        (it.name.contains(query.trim(), ignoreCase = true) || findSpecies(it.speciesId)?.name?.contains(query.trim(), ignoreCase = true) == true) &&
            (sel == null || it.zoneId == sel)
    } }
    val dueCount = tasks.count { it.daysLeft() <= 0 }
    val today = LocalDate.now()
    val doneToday = logs.count { dayOf(it.at) == today }
    val shownIds = shown.map { it.id }.toSet()
    val checklist = tasks.filter { it.itemId in shownIds && it.daysLeft() <= 0 }.sortedBy { it.dueAt() }
    val itemById = list.associateBy { it.id }

    if (adding) {
        androidx.activity.compose.BackHandler(enabled = !vm.adding) { adding = false }
        AddSheet(zones, { adding = false }, saving = vm.adding, saveError = vm.addError, fullScreen = true, backLabel = "‹ Vườn nhà") { name, s, cycles, zid, starts ->
            vm.addItem(name, s, cycles, zid, starts) { adding = false }
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Vườn nhà", style = MaterialTheme.typography.headlineMedium)
                        Text(if (list.isEmpty()) "Chăm chút góc xanh mỗi ngày" else "${list.size} cây & vật nuôi · $dueCount việc đến hạn",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (list.isNotEmpty()) IconButton({ searching = !searching; if (!searching) query = "" }) {
                        Icon(Icons.Outlined.Search, "Tìm kiếm")
                    }
                    Box {
                        IconButton({ options = true }) { Icon(Icons.Outlined.MoreVert, "Tiện ích") }
                        DropdownMenu(options, { options = false }) {
                            DropdownMenuItem(text = { Text("Thời tiết") }, onClick = { options = false; onWeather() })
                            DropdownMenuItem(text = { Text("Quản lý khu vực") }, onClick = { options = false; managing = true })
                            DropdownMenuItem(text = { Text("Lịch chăm sóc") }, onClick = { options = false; onCalendar() })
                        }
                    }
                }
            }
            item {
                if (list.isNotEmpty()) {
                    Surface(Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(if (dueCount > 0) "Góc xanh đang chờ bạn" else "Vườn đã sẵn sàng cho hôm nay", style = MaterialTheme.typography.titleLarge)
                            Text(if (dueCount > 0) "$dueCount việc cần kiểm tra · $doneToday lượt đã chăm sóc hôm nay" else "Chưa có việc đến hạn. Bạn có thể ghi lại một chiếc lá mới hoặc xem lịch sắp tới.", style = MaterialTheme.typography.bodyMedium)
                            TextButton(onCalendar) { Text("Mở lịch chăm sóc →") }
                        }
                    }
                }
                Button({ vm.clearAddError(); adding = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Filled.Add, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Thêm cây vào vườn")
                }
                TextButton(onScan, Modifier.fillMaxWidth()) { Text("Chưa biết tên cây? Nhận diện bằng ảnh") }
            }
            if (searching) item {
                OutlinedTextField(
                    query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, shape = CircleShape,
                    placeholder = { Text("Tìm cây, cá, thú cưng…") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) }
                )
            }
            if (list.isNotEmpty()) item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(!showCare, { showCare = false }, label = { Text("Trong vườn") })
                    FilterChip(showCare, { showCare = true }, label = { Text("Đến hạn ($dueCount)") })
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            if (zones.isNotEmpty() && list.isNotEmpty()) item { ZoneFilterRow(zones, sel, { zoneFilter = it }) }
            if (list.isEmpty()) item {
                Column(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    EmojiAvatar("🌿", null, 80.dp)
                    Text("Một góc xanh, dễ chăm hơn", style = MaterialTheme.typography.headlineMedium)
                    Text("Lưu cây của bạn, nhớ việc cần làm và nhìn lại từng ngày cây lớn lên.", style = MaterialTheme.typography.bodyLarge)
                    listOf("01" to ("Thêm cây đầu tiên" to "Nhập tên hoặc chụp ảnh để nhận gợi ý loài."),
                        "02" to ("Chọn nơi trồng & lịch nhắc" to "Điều chỉnh theo chậu cây và thói quen của bạn."),
                        "03" to ("Chăm sóc & ghi nhận" to "Hoàn thành việc trong Chăm sóc; thêm ảnh khi cây ra lá mới.")).forEach { (step, copy) ->
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(step, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                            Column { Text(copy.first, style = MaterialTheme.typography.titleMedium); Text(copy.second, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        } }
                    }
                }
            }
            if (!showCare && shown.isEmpty() && list.isNotEmpty()) item { Text("Không tìm thấy cây hoặc vật nuôi phù hợp.") }
            if (!showCare) items(shown, key = { "member-${it.id}" }) { p ->
                GardenMember(p, byItem[p.id] ?: emptyList(), covers[p.id], p.zoneId?.let { zoneById[it] },
                    { editingId = p.id }, { deleting = p }, { onOpen(p.id) }, Modifier.fillMaxWidth())
            }
            if (showCare && list.isNotEmpty()) item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Hôm nay", style = MaterialTheme.typography.titleMedium)
                        Text("$doneToday lượt đã hoàn thành", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onReminders) { Text("Tất cả") }
                }
            }
            if (showCare && list.isNotEmpty() && checklist.isEmpty()) item {
                Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(20.dp)) {
                        Text(if (list.isEmpty()) "🌱 Thêm thành viên đầu tiên" else "✓ Chưa có việc đến hạn", style = MaterialTheme.typography.titleMedium)
                        Text(if (list.isEmpty()) "Tạo lịch chăm sóc để bắt đầu." else "Xem lịch để chuẩn bị cho những ngày tới.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (showCare) items(checklist, key = { "care-${it.id}" }) { task ->
                GardenChecklist(task, itemById[task.itemId]?.name.orEmpty(), { onOpen(task.itemId) }, { vm.done(task) }, { vm.snooze(task) })
            }
        }
    }

    editing?.let { EditSheet(it, byItem[it.id] ?: emptyList(), zones, vm) { editingId = null } }
    if (managing) ZonesSheet(zones, { n, o, c, e -> vm.addZone(n, o, c, e) }, { z -> vm.removeZone(z.id) }) { managing = false }
    deleting?.let { d ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Xoá \"${d.name}\"?") },
            text = { Text("Toàn bộ lịch nhắc và nhật ký của mục này sẽ bị xoá.") },
            confirmButton = { TextButton(onClick = { vm.removeItem(d.id); deleting = null }) { Text("Xoá") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Huỷ") } })
    }
}

@Composable private fun GardenMember(item: Item, tasks: List<CareTask>, cover: String?, zone: Zone?, onEdit: () -> Unit,
    onDelete: () -> Unit, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val sp = findSpecies(item.speciesId)
    var menu by remember { mutableStateOf(false) }
    val due = tasks.count { it.daysLeft() <= 0 }
    Surface(modifier.clickable(onClick = onOpen), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiAvatar(sp?.emoji ?: "🌿", cover, 56.dp)
            Column(Modifier.weight(1f).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(sp?.name, zone?.name).joinToString(" · "), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (due > 0) Text("$due việc đến hạn", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Box {
                IconButton({ menu = true }) { Icon(Icons.Outlined.MoreVert, "Tuỳ chọn ${item.name}", Modifier.size(20.dp)) }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem(text = { Text("Sửa thông tin & lịch") }, onClick = { menu = false; onEdit() })
                    DropdownMenuItem(text = { Text("Xoá", color = MaterialTheme.colorScheme.error) }, onClick = { menu = false; onDelete() })
                }
            }
        }
    }
}
@Composable private fun GardenChecklist(task: CareTask, memberName: String, onOpen: () -> Unit, onDone: () -> Unit, onSnooze: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiAvatar(task.icon(), null, 44.dp)
            Column(Modifier.weight(1f).clickable(onClick = onOpen).padding(horizontal = 10.dp)) {
                Text(task.name, style = MaterialTheme.typography.titleSmall)
                Text(memberName, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(statusText(task.daysLeft()), style = MaterialTheme.typography.labelSmall, color = statusColor(task.daysLeft()))
            }
            IconButton(onSnooze) { Icon(Icons.Outlined.Snooze, "Hoãn ${task.name} 1 ngày", Modifier.size(20.dp)) }
            FilledTonalIconButton(onDone, Modifier.size(40.dp)) { Icon(Icons.Filled.Check, "Hoàn thành ${task.name}", Modifier.size(20.dp)) }
        }
    }
}

private data class NewCare(val name: String, val days: Int, val date: LocalDate = LocalDate.now())
private val CarePlansSaver = listSaver<List<NewCare>, Any>(
    save = { plans -> plans.flatMap { listOf(it.name, it.days, it.date.toEpochDay()) } },
    restore = { values -> values.chunked(3).map { NewCare(it[0] as String, it[1] as Int, LocalDate.ofEpochDay(it[2] as Long)) } }
)
private val ProfileSaver = mapSaver<Map<String, String>>(save = { it }, restore = { saved -> saved.mapValues { it.value as String } })
private val EditedFieldsSaver = listSaver<Set<String>, String>(save = { it.toList() }, restore = { it.toSet() })
private val SpeciesSaver = Saver<Species, String>(save = { it.id }, restore = { findSpecies(it) ?: SPECIES.first() })

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun AddSheet(zones: List<Zone>, onDismiss: () -> Unit, initialCandidate: PlantCandidate? = null,
    saving: Boolean = false, saveError: String? = null, scanHeader: (@Composable () -> Unit)? = null,
    fullScreen: Boolean = false, backLabel: String? = null,
    onSave: (String, Species, List<Int>, Long?, List<Long>) -> Unit) {
    var sp by rememberSaveable(stateSaver = SpeciesSaver) { mutableStateOf(SPECIES.first()) }
    var custom by rememberSaveable { mutableStateOf(true) }
    var speciesName by rememberSaveable { mutableStateOf(initialCandidate?.name.orEmpty()) }
    var usingAi by rememberSaveable { mutableStateOf(initialCandidate != null) }
    var group by rememberSaveable { mutableStateOf(SpeciesGroup.PLANT) }
    var name by rememberSaveable { mutableStateOf(initialCandidate?.name.orEmpty()) }
    var zoneId by rememberSaveable { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    var lookupJob by remember { mutableStateOf<Job?>(null) }
    var looking by remember { mutableStateOf(false) }
    var lookupError by remember { mutableStateOf<String?>(null) }
    var matches by remember { mutableStateOf<List<PlantProfile>>(emptyList()) }
    var profile by rememberSaveable(stateSaver = ProfileSaver) { mutableStateOf<Map<String,String>>(initialCandidate?.let {
        it.profile + mapOf("scientificName" to it.scientificName, "source" to "Gợi ý từ nhận diện AI · kiểm tra trước khi lưu")
    } ?: emptyMap()) }
    var detailsExpanded by rememberSaveable { mutableStateOf(initialCandidate != null) }
    var editedFields by rememberSaveable(stateSaver = EditedFieldsSaver) { mutableStateOf<Set<String>>(emptySet()) }
    // Keep manual and suggested schedules separately, so typing a species name never resets care dates.
    var manualPlans by rememberSaveable(stateSaver = CarePlansSaver) { mutableStateOf<List<NewCare>>(initialCandidate?.tasks?.map { NewCare(it.first, it.second) } ?: emptyList()) }
    var suggestedPlans by rememberSaveable(sp.id, stateSaver = CarePlansSaver) { mutableStateOf(sp.tasks.map { NewCare(it.first, it.second) }) }
    var firstReminder by rememberSaveable { mutableStateOf(false) }
    var pickingDate by remember { mutableStateOf<Int?>(null) }
    val plans = if (custom) manualPlans else suggestedPlans
    fun updatePlans(value: List<NewCare>) { if (custom) manualPlans = value else suggestedPlans = value }
    fun updatePlan(index: Int, value: NewCare) { updatePlans(plans.toMutableList().also { it[index] = value }) }
    val invalidDates = !firstReminder && plans.any { it.date > LocalDate.now() }
    val valid = (!custom || speciesName.trim().length in 1..120) && name.trim().length <= 120 && plans.size <= 30 && profile.values.all { it.length <= 1000 } && plans.all { it.name.trim().length in 1..120 && it.days in 1..365 } && !invalidDates && !looking
    pickingDate?.let { index ->
        plans.getOrNull(index)?.let { plan ->
            CareDatePicker(plan.date, { pickingDate = null }) { selected ->
                updatePlan(index, plan.copy(date = selected)); pickingDate = null
            }
        }
    }
    val form: @Composable () -> Unit = {
        Column(
            Modifier.fillMaxWidth().imePadding().navigationBarsPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (fullScreen) TextButton({ if (!saving) onDismiss() }, enabled = !saving) { Text(backLabel ?: if (initialCandidate != null) "‹ Quay lại kết quả" else "‹ Quay lại máy ảnh") }
            ScreenHeader("Thêm vào vườn", "Tạo hồ sơ riêng cho cây của bạn")
            scanHeader?.invoke()
            if (usingAi && initialCandidate != null) {
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Gợi ý từ ảnh · kiểm tra trước khi lưu", style = MaterialTheme.typography.labelLarge)
                        Text("Độ chắc chắn của gợi ý: ${when (initialCandidate.confidence) { "high" -> "cao"; "medium" -> "vừa"; else -> "thấp — có thể nhầm loài" }}",
                            style = MaterialTheme.typography.bodySmall)
                        Text(initialCandidate.careTips, style = MaterialTheme.typography.bodySmall)
                        TextButton({ usingAi = false; speciesName = ""; if (name == initialCandidate.name) name = ""; profile = emptyMap(); manualPlans = emptyList(); custom = true }, enabled = !saving) { Text("Bỏ gợi ý, tự nhập") }
                    }
                }
            }
            SectionHeading("01", "Cây của bạn", "Tra cứu tên loài hoặc chọn từ danh mục")
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(custom, { custom = true; pickingDate = null }, label = { Text("Tự nhập tên loài") })
                FilterChip(!custom, { lookupJob?.cancel(); looking = false; custom = false; matches = emptyList(); pickingDate = null }, label = { Text("Chọn từ danh sách") })
            }
            if (!custom) {
                var choosingSpecies by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton({ choosingSpecies = true }, Modifier.fillMaxWidth()) { Text("${sp.emoji} ${sp.name}") }
                    DropdownMenu(choosingSpecies, { choosingSpecies = false }) {
                        SPECIES.forEach { s -> DropdownMenuItem(text = { Text("${s.emoji} ${s.name}") },
                            onClick = { sp = s; pickingDate = null; choosingSpecies = false }) }
                    }
                }
            }
            if (custom) {
                Text("Nhóm", style = MaterialTheme.typography.titleSmall)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SpeciesGroup.entries.forEach { value ->
                        FilterChip(group == value, { lookupJob?.cancel(); looking = false; matches = emptyList(); profile = emptyMap(); group = value }, label = { Text("${value.emoji} ${value.label}") })
                    }
                }
                OutlinedTextField(speciesName, { lookupJob?.cancel(); looking = false; speciesName = it; matches = emptyList(); lookupError = null }, Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("Tên loài hoặc mô tả *") }, placeholder = { Text("VD: Cây kim tiền, cây lá tím chưa rõ tên") },
                    supportingText = { Text("Bạn có thể nhập mô tả tạm khi chưa biết tên loài.") })
                if (group == SpeciesGroup.PLANT) {
                    Button({
                        lookupError = null; matches = emptyList(); looking = true
                        lookupJob = scope.launch {
                            try {
                                matches = PlantLookupRepository().search(speciesName)
                                if (matches.isEmpty()) lookupError = "Chưa xác định được cây. Hãy nhập tên cụ thể hơn hoặc tự bổ sung thông tin."
                            } catch(e: CancellationException) { throw e }
                            catch(e: Exception) { lookupError = e.message ?: "Chưa tra cứu được." }
                            finally { looking = false }
                        }
                    }, enabled = !saving && !looking && speciesName.trim().length in 2..120, modifier = Modifier.fillMaxWidth()) {
                        if (looking) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Tra cứu thông tin cây")
                    }
                    lookupError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    matches.forEach { match ->
                        OutlinedButton({
                            speciesName = match.name
                            if (name.isBlank()) name = match.name
                            profile = profile + match.fields.filter { (key, _) -> key !in editedFields }
                            detailsExpanded = true
                            manualPlans = match.tasks.map { NewCare(it.first, it.second) }
                            matches = emptyList()
                        }, Modifier.fillMaxWidth(), enabled = !saving) { Text("${match.name} · ${match.fields["scientificName"]}") }
                    }
                    Text("Chọn kết quả để điền thông tin. Lịch gợi ý sẽ thay lịch trong biểu mẫu; hãy kiểm tra trước khi lưu.", style = MaterialTheme.typography.bodySmall)
                    TextButton({ detailsExpanded = !detailsExpanded }) { Text(if (detailsExpanded) "Thu gọn thông tin cây" else "Bổ sung thông tin cây") }
                    if (detailsExpanded) {
                    Text("Chiều cao trưởng thành là tham khảo. Chiều cao hiện tại do bạn đo; độ ẩm bên dưới là không khí, không phải số đo đất.", style = MaterialTheme.typography.bodySmall)
                    listOf("scientificName" to "Tên khoa học / loài", "cultivar" to "Giống (nếu biết)",
                        "actualHeight" to "Chiều cao hiện tại (cm, tự đo)", "matureHeight" to "Chiều cao trưởng thành tham khảo",
                        "humidity" to "Độ ẩm không khí phù hợp", "soil" to "Đất / giá thể và tưới", "light" to "Ánh sáng").forEach { (field,label) ->
                        OutlinedTextField(profile[field].orEmpty(), { editedFields = editedFields + field; profile = profile + (field to it) }, Modifier.fillMaxWidth(), label = { Text(label) }, enabled = !saving)
                    }
                    profile["source"]?.takeIf { it.isNotBlank() }?.let { Text("Nguồn tham khảo: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
                Text("Bạn có thể chỉnh lịch theo nhu cầu thực tế hoặc bổ sung sau.", style = MaterialTheme.typography.bodySmall)
            } else {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text("💡 ${sp.tip}", Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider()
            SectionHeading("02", "Đặt tên & vị trí", "Ghi điều kiện thực tế của cây trong vườn bạn")
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Tên gọi riêng (không bắt buộc)") }, placeholder = { Text("VD: Chậu ở bàn làm việc") })
            OutlinedTextField(profile["location"].orEmpty(), { profile = profile + ("location" to it) }, Modifier.fillMaxWidth(),
                singleLine = true, label = { Text("Vị trí đặt cây") }, placeholder = { Text("VD: Ban công, cạnh cửa sổ hướng đông") })
            OutlinedTextField(profile["actualLight"].orEmpty(), { profile = profile + ("actualLight" to it) }, Modifier.fillMaxWidth(),
                label = { Text("Ánh sáng nơi trồng") }, placeholder = { Text("VD: Nắng sáng 2 giờ, còn lại ánh sáng gián tiếp") })
            OutlinedTextField(profile["pottingMix"].orEmpty(), { profile = profile + ("pottingMix" to it) }, Modifier.fillMaxWidth(),
                label = { Text("Chậu & giá thể (không bắt buộc)") }, placeholder = { Text("VD: Chậu có lỗ thoát nước, đất trộn xơ dừa") })
            if (zones.isNotEmpty()) {
                Text("Khu vực", style = MaterialTheme.typography.titleSmall)
                ZonePicker(zones, zoneId) { zoneId = it }
            }
            HorizontalDivider()
            SectionHeading("03", "Lịch chăm sóc", "Chọn việc, chu kỳ và ngày bắt đầu nhắc")
            Text("Lịch nhắc giúp bạn kiểm tra cây. Chỉ tưới hoặc bón khi cây cần; không coi chu kỳ gợi ý là yêu cầu bắt buộc.", style = MaterialTheme.typography.bodySmall)
            if (custom && group == SpeciesGroup.PLANT) Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Kiểm tra độ ẩm đất" to 3, "Kiểm tra dinh dưỡng" to 30).forEach { (task, days) ->
                    AssistChip({ if (plans.none { it.name == task }) updatePlans(plans + NewCare(task, days)) }, label = { Text("＋ $task") })
                }
            }
            if (plans.isNotEmpty()) Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(!firstReminder, { firstReminder = false }, label = { Text("Lần chăm sóc gần nhất") })
                FilterChip(firstReminder, { firstReminder = true }, label = { Text("Ngày nhắc đầu tiên") })
            }
            plans.forEachIndexed { index, plan ->
                if (custom) Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(plan.name, { updatePlan(index, plan.copy(name = it)) }, Modifier.weight(1f),
                        singleLine = true, label = { Text("Tên việc *") }, placeholder = { Text("VD: Tưới nước, thay nước") })
                    IconButton({ updatePlans(plans.filterIndexed { i, _ -> i != index }); pickingDate = null }) {
                        Icon(Icons.Outlined.Delete, "Bỏ việc")
                    }
                }
                CycleStepper(if (custom) "Lặp lại mỗi" else "${taskEmoji(plan.name)} ${plan.name}", plan.days) {
                    updatePlan(index, plan.copy(days = it))
                }
                OutlinedButton({ pickingDate = index }) {
                    Text("${if (firstReminder) "Nhắc từ" else "Đã chăm sóc"}: ${plan.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                }
            }
            if (custom) {
                OutlinedButton({ updatePlans(plans + NewCare("", 7)) }) { Text("＋ Thêm việc chăm sóc") }
                if (plans.isEmpty()) Text("Chưa có lịch nhắc. Bạn vẫn có thể lưu và ghi nhật ký.", style = MaterialTheme.typography.bodySmall)
            }
            if (invalidDates) Text("Lần chăm sóc gần nhất không thể ở tương lai.", color = MaterialTheme.colorScheme.error)
            if (name.trim().length > 120 || plans.any { it.name.trim().length > 120 }) Text("Tên cây và tên việc tối đa 120 ký tự.", color = MaterialTheme.colorScheme.error)
            if (profile.values.any { it.length > 1000 }) Text("Mỗi thông tin mô tả tối đa 1000 ký tự.", color = MaterialTheme.colorScheme.error)
            if (plans.size > 30) Text("Mỗi cây có tối đa 30 việc chăm sóc.", color = MaterialTheme.colorScheme.error)
            if (plans.isNotEmpty()) Text("Ngày này chỉ dùng để lập lịch, không tạo lịch sử hoàn thành.", style = MaterialTheme.typography.bodySmall)
            saveError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button({
                val chosen = if (custom) customSpecies(speciesName, group, plans.map { it.name.trim() to it.days }).copy(
                    profileJson = if (group == SpeciesGroup.PLANT && profile.isNotEmpty()) JSONObject(profile).toString() else "") else sp.copy(profileJson = if (profile.isNotEmpty()) JSONObject(profile).toString() else "")
                onSave(name.trim().ifEmpty { chosen.name }, chosen, plans.map { it.days }, zoneId,
                    plans.map { scheduleStart(it.date, it.days, firstReminder) })
            }, Modifier.fillMaxWidth().height(52.dp), enabled = valid && !saving) {
                if (saving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Thêm vào vườn")
            }
        }
    }
    if (fullScreen) Surface(Modifier.fillMaxSize()) { form() }
    else ModalBottomSheet(onDismissRequest = { if (!saving) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) { form() }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun EditSheet(item: Item, tasks: List<CareTask>, zones: List<Zone>, vm: PlantsViewModel, onDismiss: () -> Unit) {
    var profile by remember(item.id) { mutableStateOf(runCatching {
        val json = JSONObject(item.profileJson)
        json.keys().asSequence().associateWith { json.optString(it) }
    }.getOrDefault(emptyMap<String,String>())) }
    var name by remember { mutableStateOf(item.name) }
    val species = findSpecies(item.speciesId)
    val customGroup = SpeciesGroup.entries.firstOrNull { item.speciesId.startsWith("custom:${it.name}:") }
    var speciesName by remember { mutableStateOf(species?.name.orEmpty()) }
    var zoneId by remember { mutableStateOf(item.zoneId) }
    val cycles = remember { mutableStateMapOf<Long, Int>() }
    var newName by remember { mutableStateOf("") }
    var newDays by remember { mutableIntStateOf(7) }
    var pickedType by remember { mutableStateOf<TaskType?>(null) }
    val effType = pickedType ?: TaskType.infer(newName) // chưa chọn thì đoán theo tên
    LaunchedEffect(item.id) { vm.clearEditError() }
    ModalBottomSheet(onDismissRequest = { if (!vm.editing) onDismiss() }) {
        Column(
            Modifier.fillMaxWidth().imePadding().navigationBarsPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Sửa thông tin", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Tên gọi") })
            if (profile.isNotEmpty()) {
                Text("Thông tin cây", style = MaterialTheme.typography.titleSmall)
                listOf("scientificName" to "Tên khoa học", "cultivar" to "Giống", "actualHeight" to "Chiều cao hiện tại (cm)",
                    "matureHeight" to "Chiều cao trưởng thành tham khảo", "humidity" to "Độ ẩm không khí", "soil" to "Đất / tưới", "light" to "Ánh sáng", "location" to "Vị trí đặt cây", "actualLight" to "Ánh sáng nơi trồng", "pottingMix" to "Chậu & giá thể").forEach { (key,label) ->
                    OutlinedTextField(profile[key].orEmpty(), { profile = profile + (key to it) }, Modifier.fillMaxWidth(), label = { Text(label) })
                }
            }
            if (customGroup != null) OutlinedTextField(speciesName, { speciesName = it }, Modifier.fillMaxWidth(),
                singleLine = true, label = { Text("Tên loài hoặc mô tả *") })
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
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskType.entries.forEach { ty ->
                    FilterChip(selected = ty == effType, onClick = { pickedType = ty }, label = { Text("${ty.emoji} ${ty.label}") })
                }
            }
            CycleStepper("Lặp lại mỗi", newDays) { newDays = it }
            OutlinedButton(enabled = newName.trim().length in 1..120 && !vm.editing, onClick = {
                vm.addTask(item.id, newName.trim(), newDays, effType); newName = ""; pickedType = null
            }) { Text("Thêm việc") }
            vm.editError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button({
                val speciesId = if (customGroup != null) customSpecies(speciesName, customGroup).id else item.speciesId
                vm.saveEdit(item, name.trim().ifEmpty { item.name }, zoneId, speciesId,
                    if (profile.isEmpty()) "" else JSONObject(profile).toString(), cycles.toMap(), onDismiss)
            }, Modifier.fillMaxWidth().height(52.dp), enabled = !vm.editing && name.trim().length <= 120 && (customGroup == null || speciesName.trim().length in 1..120)) { Text(if (vm.editing) "Đang lưu…" else "Lưu") }
        }
    }
}
