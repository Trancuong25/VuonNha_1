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
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.vector.ImageVector
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
    onCalendar: () -> Unit = {}, onWeather: () -> Unit = {}, onReminders: () -> Unit = {}, onScan: () -> Unit = {}, onAccount: () -> Unit = {}) {
    val account by AccountSession.user.collectAsStateWithLifecycle()
    val scanHidden by ScanSettings.hiddenState.collectAsStateWithLifecycle()
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
        AddSheet(zones, { adding = false }, saving = vm.adding, saveError = vm.addError, fullScreen = true, backLabel = "‹ Vườn Nhà") { name, s, cycles, zid, starts ->
            vm.addItem(name, s, cycles, zid, starts) { adding = false }
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocalFlorist, null, Modifier.padding(end = 10.dp).size(32.dp), tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        GardenWordmark()
                        Text(account?.let { "Chào ${it.name}" } ?: "Một chút chăm sóc, mỗi ngày",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onAccount) { Icon(Icons.Outlined.PersonOutline, "Tài khoản") }
                    Box {
                        IconButton({ options = true }) { Icon(Icons.Outlined.MoreVert, "Tiện ích") }
                        DropdownMenu(options, { options = false }) {
                            if (!scanHidden) DropdownMenuItem(text = { Text("Quét cây") }, onClick = { options = false; onScan() })
                            DropdownMenuItem(text = { Text("Thời tiết") }, onClick = { options = false; onWeather() })
                        }
                    }
                }
            }
            item {
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Hôm nay trong vườn", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif), color = MaterialTheme.colorScheme.primary)
                        HomeStatus(Icons.Outlined.LocalFlorist, "${list.size} thành viên trong vườn")
                        HomeStatus(Icons.Outlined.CalendarMonth, "$dueCount việc đến hạn")
                        HomeStatus(Icons.Filled.Check, "$doneToday lượt chăm sóc đã hoàn thành")
                    }
                }
            }
            item {
                Button({ vm.clearAddError(); adding = true }, Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Filled.Add, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Thêm cây vào vườn")
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeShortcut("Lịch chăm sóc", "$dueCount việc đến hạn", Icons.Outlined.CalendarMonth, onCalendar, Modifier.weight(1f))
                    HomeShortcut("Khu vực trồng", "${zones.size} khu vực", Icons.Outlined.LocationOn, { managing = true }, Modifier.weight(1f))
                }
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
                    Text("Khu vườn của bạn", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    IconButton({ searching = !searching; if (!searching) query = "" }) { Icon(Icons.Outlined.Search, "Tìm kiếm") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(!showCare, { showCare = false }, label = { Text("Trong vườn") })
                    FilterChip(showCare, { showCare = true }, label = { Text("Đến hạn ($dueCount)") })
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            if (zones.isNotEmpty() && list.isNotEmpty()) item { ZoneFilterRow(zones, sel, { zoneFilter = it }) }
            if (list.isEmpty()) item {
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        EmojiAvatar("🌱", null, 40.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Bắt đầu với chậu cây đầu tiên", style = MaterialTheme.typography.titleSmall)
                            Text("Thêm tên cây trước. Vị trí, ảnh và lịch chăm có thể bổ sung sau.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
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

@Composable private fun HomeStatus(icon: ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable private fun HomeShortcut(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Serif), color = MaterialTheme.colorScheme.primary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
    var step by rememberSaveable { mutableIntStateOf(0) }
    var conditionsExpanded by rememberSaveable { mutableStateOf(false) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    var reviewing by rememberSaveable { mutableStateOf(false) }
    var keepExtra by rememberSaveable { mutableStateOf(true) }
    val referenceKeys = setOf("scientificName", "cultivar", "actualHeight", "matureHeight", "humidity", "soil", "light", "source")
    val formScroll = rememberScrollState()
    val focus = androidx.compose.ui.platform.LocalFocusManager.current
    fun goToStep(value: Int) { focus.clearFocus(); step = value.coerceIn(0, 2) }
    LaunchedEffect(step) { formScroll.scrollTo(0) }
    androidx.activity.compose.BackHandler(enabled = fullScreen && !saving) {
        if (step > 0) goToStep(step - 1) else onDismiss()
    }
    val plans = if (custom) manualPlans else suggestedPlans
    fun updatePlans(value: List<NewCare>) { if (custom) manualPlans = value else suggestedPlans = value }
    fun updatePlan(index: Int, value: NewCare) { updatePlans(plans.toMutableList().also { it[index] = value }) }
    val invalidDates = !firstReminder && plans.any { it.date > LocalDate.now() }
    val valid = (!custom || speciesName.trim().length in 1..120) && name.trim().length <= 120 && plans.size <= 30 && profile.values.all { it.length <= 1000 } && plans.all { it.name.trim().length in 1..120 && it.days in 1..365 } && !invalidDates && !looking
    val canContinue = if (step == 0) (!custom || speciesName.trim().length in 1..120) && profile.values.all { it.length <= 1000 } && !looking
        else name.trim().length <= 120 && profile.values.all { it.length <= 1000 }
    fun submit() {
        val savedProfile = if (keepExtra) profile else profile.filterKeys { it !in referenceKeys }
        val chosen = if (custom) customSpecies(speciesName, group, plans.map { it.name.trim() to it.days }).copy(
            profileJson = if (group == SpeciesGroup.PLANT && savedProfile.isNotEmpty()) JSONObject(savedProfile).toString() else "")
        else sp.copy(profileJson = if (savedProfile.isNotEmpty()) JSONObject(savedProfile).toString() else "")
        onSave(name.trim().ifEmpty { chosen.name }, chosen, plans.map { it.days }, zoneId,
            plans.map { scheduleStart(it.date, it.days, firstReminder) })
    }
    fun requestLookup(fillSingleResult: Boolean = false) {
        lookupError = null; matches = emptyList(); looking = true
        lookupJob = scope.launch {
            try {
                val results = PlantLookupRepository().search(speciesName)
                if (results.isEmpty()) lookupError = "Chưa tìm được thông tin. Bạn vẫn có thể tự điền hoặc để trống."
                else if (fillSingleResult && results.size == 1) {
                    val match = results.first()
                    speciesName = match.name
                    profile = profile + match.fields.filter { (key, _) -> key !in editedFields }
                    if (manualPlans.isEmpty()) manualPlans = match.tasks.map { NewCare(it.first, it.second) }
                } else matches = results
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { lookupError = e.message ?: "Chưa tra cứu được. Bạn có thể điền sau." }
            finally { looking = false }
        }
    }
    pickingDate?.let { index ->
        plans.getOrNull(index)?.let { plan ->
            CareDatePicker(plan.date, { pickingDate = null }) { selected ->
                updatePlan(index, plan.copy(date = selected)); pickingDate = null
            }
        }
    }
    val form: @Composable () -> Unit = {
        Column(if (fullScreen) Modifier.fillMaxSize().imePadding().navigationBarsPadding() else Modifier.fillMaxWidth()) {
        Column(
            (if (fullScreen) Modifier.weight(1f) else Modifier).fillMaxWidth().verticalScroll(formScroll)
                .padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (fullScreen) TextButton({ if (step > 0) goToStep(step - 1) else onDismiss() }, enabled = !saving) {
                Text(if (step > 0) "‹ Quay lại" else backLabel ?: if (initialCandidate != null) "‹ Quay lại kết quả" else "‹ Quay lại máy ảnh")
            }
            ScreenHeader("Thêm vào vườn", if (fullScreen) "Bước ${step + 1}/3 · ${listOf("Chọn cây", "Góc trồng", "Nhắc chăm")[step]}" else "Tạo hồ sơ riêng cho cây của bạn")
            if (fullScreen) {
                LinearProgressIndicator(progress = { (step + 1) / 3f }, modifier = Modifier.fillMaxWidth())
                Text(if (step == 0) "Bắt đầu với tên cây. Bạn có thể tra cứu hoặc tự nhập."
                    else if (step == 1) "Đặt tên thân quen và chọn một góc cho cây. Các mục này đều tuỳ chọn."
                    else "Chọn việc muốn được nhắc. Bạn có thể lưu cây mà chưa đặt lịch.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!fullScreen || step == 0) {
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
            if (fullScreen) Text("Bạn đang trồng cây gì?", style = MaterialTheme.typography.titleLarge)
            else SectionHeading("01", "Bạn đang trồng cây gì?", "Chọn loài để tạo hồ sơ riêng")
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
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Tên loài hoặc mô tả *") }, placeholder = { Text("VD: Cây kim tiền, cây lá tím chưa rõ tên") },
                    isError = speciesName.trim().length > 120 || (attempted && speciesName.isBlank()),
                    supportingText = { if (attempted && speciesName.isBlank()) Text("Bạn chưa nhập tên cây. Chưa rõ loài thì dùng tên mô tả nhé.")
                        else if (speciesName.trim().length > 120) Text("Tên cây dài quá, bạn rút gọn còn tối đa 120 ký tự nhé.") })
                if (group == SpeciesGroup.PLANT) {
                    OutlinedButton({ requestLookup() }, enabled = !saving && !looking && speciesName.trim().length in 2..120, modifier = Modifier.fillMaxWidth()) {
                        if (looking) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Tra cứu thông tin cây")
                    }
                    if (looking) {
                        Text("Đang tìm gợi ý… Không cần chờ nếu bạn muốn tự nhập.", style = MaterialTheme.typography.bodySmall)
                        TextButton({ lookupJob?.cancel(); looking = false; lookupError = null; matches = emptyList() }) {
                            Text("Bỏ qua tra cứu, tiếp tục tự nhập")
                        }
                    }
                    lookupError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    matches.forEach { match ->
                        OutlinedButton({
                            speciesName = match.name
                            if (name.isBlank()) name = match.name
                            profile = profile + match.fields.filter { (key, _) -> key !in editedFields }
                            detailsExpanded = true
                            // Lookup enriches the profile; it must not erase a schedule the user wrote.
                            if (manualPlans.isEmpty()) manualPlans = match.tasks.map { NewCare(it.first, it.second) }
                            matches = emptyList()
                        }, Modifier.fillMaxWidth().heightIn(min = 72.dp), enabled = !saving) {
                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${match.name} · ${match.fields["scientificName"]}", style = MaterialTheme.typography.titleSmall)
                                Text("Chọn để điền thông tin tham khảo", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    TextButton({ detailsExpanded = !detailsExpanded }) { Text(if (detailsExpanded) "Thu gọn thông tin bổ sung" else "Thông tin bổ sung về cây") }
                    if (detailsExpanded) {
                    Text("Thông tin bổ sung về cây", style = MaterialTheme.typography.titleMedium)
                    if (profile.none { (key,value) -> key in referenceKeys && value.isNotBlank() }) {
                        TextButton({ requestLookup(fillSingleResult = true) }, enabled = !saving && !looking && speciesName.trim().length in 2..120) {
                            Text("Điền gợi ý theo tên cây")
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(keepExtra, { keepExtra = it }, enabled = !saving)
                        Text("Lưu kèm thông tin bổ sung", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("Không bắt buộc. Thông tin tra cứu là gợi ý để bạn xem và chỉnh lại.", style = MaterialTheme.typography.bodySmall)
                    listOf("scientificName" to "Tên khoa học / loài", "cultivar" to "Giống (nếu biết)",
                        "actualHeight" to "Chiều cao hiện tại (cm, tự đo)", "matureHeight" to "Chiều cao trưởng thành tham khảo",
                        "humidity" to "Độ ẩm không khí phù hợp", "soil" to "Đất / giá thể và tưới", "light" to "Ánh sáng").forEach { (field,label) ->
                        OutlinedTextField(profile[field].orEmpty(), { editedFields = editedFields + field; profile = profile + (field to it) }, Modifier.fillMaxWidth(), label = { Text(label) }, enabled = !saving,
                            isError = profile[field].orEmpty().length > 1000, supportingText = { if (profile[field].orEmpty().length > 1000) Text("Bạn rút gọn còn tối đa 1000 ký tự nhé.") })
                    }
                    profile["source"]?.takeIf { it.isNotBlank() }?.let { Text("Nguồn tham khảo: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            } else {
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text("💡 ${sp.tip}", Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
            }
            if (!fullScreen || step == 1) {
            HorizontalDivider()
            if (fullScreen) Text("Cây sẽ ở góc nào?", style = MaterialTheme.typography.titleLarge)
            else SectionHeading("02", "Góc nhỏ của cây", "Một cái tên và nơi cây sẽ lớn lên")
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(16.dp),
                label = { Text("Tên gọi riêng (không bắt buộc)") }, placeholder = { Text("VD: Chậu ở bàn làm việc") },
                isError = name.trim().length > 120,
                supportingText = { Text(if (name.trim().length > 120) "Tên gọi tối đa 120 ký tự." else "Để trống sẽ dùng tên loài. Bạn có thể đổi tên sau.") })
            OutlinedTextField(profile["location"].orEmpty(), { profile = profile + ("location" to it) }, Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(16.dp), label = { Text("Vị trí đặt cây") }, placeholder = { Text("VD: Ban công, cạnh cửa sổ hướng đông") })
            TextButton({ conditionsExpanded = !conditionsExpanded }) { Text(if (conditionsExpanded) "Thu gọn điều kiện trồng" else "＋ Thêm ánh sáng & giá thể") }
            if (conditionsExpanded || !fullScreen) {
            OutlinedTextField(profile["actualLight"].orEmpty(), { profile = profile + ("actualLight" to it) }, Modifier.fillMaxWidth(),
                label = { Text("Ánh sáng nơi trồng") }, placeholder = { Text("VD: Nắng sáng 2 giờ, còn lại ánh sáng gián tiếp") })
            OutlinedTextField(profile["pottingMix"].orEmpty(), { profile = profile + ("pottingMix" to it) }, Modifier.fillMaxWidth(),
                label = { Text("Chậu & giá thể (không bắt buộc)") }, placeholder = { Text("VD: Chậu có lỗ thoát nước, đất trộn xơ dừa") })
            }
            if (zones.isNotEmpty()) {
                Text("Khu vực", style = MaterialTheme.typography.titleSmall)
                ZonePicker(zones, zoneId) { zoneId = it }
            }
            }
            if (!fullScreen || step == 2) {
            HorizontalDivider()
            if (fullScreen) Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    EmojiAvatar("🌿", null, 44.dp)
                    Column(Modifier.weight(1f)) {
                        Text(name.trim().ifEmpty { if (custom) speciesName.trim() else sp.name }, style = MaterialTheme.typography.titleMedium)
                        Text(profile["location"].orEmpty().ifBlank { "Chưa chọn vị trí · bổ sung sau" }, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (fullScreen) Text("Lịch chăm cây", style = MaterialTheme.typography.titleMedium)
            else SectionHeading("03", "Chăm theo nhịp của bạn", "Chọn việc cần nhắc, không bắt buộc phải có lịch")
            Text("Đặt lịch kiểm tra cây; chỉ tưới hoặc bón khi cây cần.", style = MaterialTheme.typography.bodySmall)
            if (custom && group == SpeciesGroup.PLANT) Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Kiểm tra độ ẩm đất" to 3, "Kiểm tra dinh dưỡng" to 30).forEach { (task, days) ->
                    AssistChip({ if (plans.none { it.name == task }) updatePlans(plans + NewCare(task, days)) }, label = { Text("＋ $task") })
                }
            }
            if (plans.isNotEmpty()) Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(!firstReminder, { firstReminder = false }, label = { Text("Tính từ lần đã chăm") })
                FilterChip(firstReminder, { firstReminder = true }, label = { Text("Chọn ngày nhắc đầu") })
            }
            plans.forEachIndexed { index, plan ->
                OutlinedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (custom) Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(plan.name, { updatePlan(index, plan.copy(name = it)) }, Modifier.weight(1f),
                        singleLine = true, label = { Text("Tên việc *") }, placeholder = { Text("VD: Tưới nước, thay nước") },
                        isError = plan.name.isBlank() || plan.name.trim().length > 120,
                        supportingText = { if (plan.name.isBlank()) Text("Nhập tên việc hoặc bấm thùng rác để bỏ lịch này.") })
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
                }
            }
            if (plans.isNotEmpty()) TextButton({ updatePlans(emptyList()); pickingDate = null }) { Text("Chưa cần nhắc, đặt lịch sau") }
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
            Text(if (plans.isEmpty()) "Sẽ lưu hồ sơ cây, chưa tạo lịch nhắc." else "Sẽ lưu hồ sơ cây và ${plans.size} việc chăm sóc.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (custom && speciesName.isBlank()) Text("Nhập tên loài hoặc mô tả ở mục 01 để thêm vào vườn.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (plans.any { it.name.isBlank() }) Text("Có việc chăm sóc chưa có tên. Điền tên việc hoặc bỏ việc đó để lưu.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            if (!fullScreen) Button({ submit() }, Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = valid && !saving) {
                if (saving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Thêm vào vườn")
            }
            }
        }
        if (fullScreen) Surface(shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (step > 0) OutlinedButton({ goToStep(step - 1) }, Modifier.weight(1f).heightIn(min = 52.dp), enabled = !saving) { Text("Quay lại") }
                Button({
                    attempted = true
                    if (custom && speciesName.trim().length !in 1..120) { goToStep(0); scope.launch { formScroll.scrollTo(0) } }
                    else if (profile.values.any { it.length > 1000 }) {
                        val referenceInvalid = profile.any { (key,value) -> key in referenceKeys && value.length > 1000 }
                        detailsExpanded = referenceInvalid; conditionsExpanded = !referenceInvalid
                        goToStep(if (referenceInvalid) 0 else 1)
                    }
                    else if (name.trim().length > 120 && step > 0) { goToStep(1) }
                    else if (step < 2 && canContinue) { attempted = false; goToStep(step + 1) }
                    else if (valid) { focus.clearFocus(); reviewing = true }
                    else { goToStep(2); scope.launch { formScroll.scrollTo(0) } }
                }, Modifier.weight(1.6f).heightIn(min = 52.dp),
                    enabled = !saving && !looking, shape = RoundedCornerShape(16.dp)) {
                    if (saving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text(if (step < 2) "Tiếp tục" else "Thêm vào vườn")
                }
            }
        }
        }
    }
    if (reviewing) AlertDialog(onDismissRequest = { if (!saving) reviewing = false },
        title = { Text("Xem lại trước khi lưu") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileInfo("Tên trong vườn", name.trim().ifEmpty { if (custom) speciesName.trim() else sp.name })
                ProfileInfo("Loài", if (custom) speciesName.trim() else sp.name)
                ProfileInfo("Vị trí", profile["location"].orEmpty().ifBlank { "Chưa thêm" })
                profile["actualLight"]?.takeIf { it.isNotBlank() }?.let { ProfileInfo("Ánh sáng nơi trồng",it) }
                profile["pottingMix"]?.takeIf { it.isNotBlank() }?.let { ProfileInfo("Chậu và giá thể",it) }
                zoneId?.let { id -> zones.find { it.id == id }?.let { ProfileInfo("Khu vực", it.name) } }
                Text(if (plans.isEmpty()) "Chưa đặt lịch nhắc" else "Lịch nhắc", style = MaterialTheme.typography.titleSmall)
                plans.forEach { Text("${it.name} · mỗi ${it.days} ngày · ${it.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", style = MaterialTheme.typography.bodySmall) }
                if (profile.any { (key,value) -> key in referenceKeys && value.isNotBlank() }) {
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(keepExtra, { keepExtra = it }, enabled = !saving)
                        Text("Lưu thông tin bổ sung về cây", style = MaterialTheme.typography.bodyMedium)
                    }
                    listOf("scientificName" to "Tên khoa học", "cultivar" to "Giống", "actualHeight" to "Chiều cao hiện tại (cm)", "matureHeight" to "Chiều cao trưởng thành", "humidity" to "Độ ẩm không khí", "soil" to "Đất và tưới", "light" to "Ánh sáng", "source" to "Nguồn gợi ý").forEach { (key,label) ->
                        profile[key]?.takeIf { it.isNotBlank() }?.let { ProfileInfo(label,it) }
                    }
                }
                saveError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton({ if (valid && !saving) submit() }, enabled = valid && !saving) { Text(if (saving) "Đang lưu…" else "Xác nhận lưu") } },
        dismissButton = { TextButton({ reviewing = false }, enabled = !saving) { Text("Chỉnh lại") } })
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
