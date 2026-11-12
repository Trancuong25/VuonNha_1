package com.example.vuonnha.ui.journal

import android.content.Context
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vuonnha.data.CareLog
import com.example.vuonnha.data.DiaryEntry
import com.example.vuonnha.data.findSpecies
import com.example.vuonnha.domain.DAY
import com.example.vuonnha.domain.startOfDay
import com.example.vuonnha.ui.common.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FMT = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
private fun fmt(t: Long) = FMT.format(Date(t))

/** Tạo Uri (FileProvider) cho file tạm trong cache/camera để app camera ghi ảnh vào. */
private fun newCameraUri(ctx: Context): Uri {
    val dir = File(ctx.cacheDir, "camera").apply { mkdirs() }
    val f = File(dir, "shot_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", f)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun JournalScreen(vm: JournalViewModel = viewModel(), initialItemId: Long? = null) {
    val appCtx = LocalContext.current
    val items by vm.items.collectAsStateWithLifecycle()
    val diary by vm.diary.collectAsStateWithLifecycle()
    val logs by vm.logs.collectAsStateWithLifecycle()
    val saving by vm.saving.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    var selId by rememberSaveable(initialItemId) { mutableStateOf(initialItemId) }
    var note by rememberSaveable { mutableStateOf("") }
    var picked by rememberSaveable { mutableStateOf<Uri?>(null) }
    var camUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var deleting by remember { mutableStateOf<DiaryEntry?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { if (it != null) picked = it }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) picked = camUri
        camUri = null
    }
    val cur = items.firstOrNull { it.id == selId } ?: items.firstOrNull()

    if (cur == null) {
        Column(Modifier.padding(16.dp)) {
            ScreenHeader("Nhật ký")
            EmptyState("📸", "Chưa có gì để ghi", "Thêm cây hoặc thú cưng ở Vườn nhà để bắt đầu theo dõi sự lớn lên.")
        }
        return
    }
    val entries = remember(diary, cur.id) { diary.filter { it.itemId == cur.id } }
    val curLogs = remember(logs, cur.id) { logs.filter { it.itemId == cur.id } }
    // entries sắp xếp mới -> cũ
    val photos = remember(entries) { entries.filter { it.photoUri.isNotEmpty() } }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenHeader("Nhật ký", "Một chiếc lá mới, một lần ra hoa — lưu lại ở đây") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.id }) { i ->
                    FilterChip(
                        selected = i.id == cur.id, enabled = !saving, onClick = { selId = i.id },
                        label = { Text("${findSpecies(i.speciesId)?.emoji ?: "🌿"} ${i.name}") }
                    )
                }
            }
        }
        if (photos.size >= 2) item { CompareCard(first = photos.last(), latest = photos.first()) }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Thêm mốc mới", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), enabled = !saving, label = { Text("Ghi chú (VD: ra lá mới)") })
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton({ picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, enabled = !saving) {
                            Icon(Icons.Outlined.AddPhotoAlternate, null); Spacer(Modifier.width(6.dp)); Text(if (picked == null) "Chọn ảnh" else "Đổi ảnh")
                        }
                        OutlinedButton({
                            val u = newCameraUri(appCtx)
                            camUri = u
                            cameraError = null
                            try { camera.launch(u) }
                            catch (e: ActivityNotFoundException) {
                                camUri = null
                                cameraError = "Thiết bị chưa có ứng dụng camera. Bạn có thể chọn ảnh từ thư viện."
                            }
                        }, enabled = !saving) {
                            Icon(Icons.Outlined.PhotoCamera, null); Spacer(Modifier.width(6.dp)); Text("Chụp ảnh")
                        }
                        if (picked != null) AssistChip(enabled = !saving, onClick = { picked = null }, label = { Text("Đã chọn 1 ảnh · bỏ") })
                    }
                    Button(
                        enabled = !saving && (picked != null || note.isNotBlank()),
                        onClick = { vm.addEntry(cur.id, note.trim(), picked) { note = ""; picked = null } },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text(if (saving) "Đang lưu…" else "Lưu vào nhật ký") }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    cameraError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Chăm sóc 14 ngày qua", style = MaterialTheme.typography.titleMedium)
                    CareChart(curLogs)
                }
            }
        }
        if (entries.isEmpty()) item { EmptyState("🌱", "Chưa có mốc nào", "Chọn ảnh đầu tiên để theo dõi sự lớn lên.") }
        items(entries, key = { it.id }) { e -> EntryCard(e) { deleting = e } }
    }
    deleting?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Xoá mốc nhật ký?") },
            text = { Text("Ảnh và ghi chú của mốc này sẽ bị xoá.") },
            confirmButton = { TextButton({ vm.removeEntry(entry); deleting = null }) { Text("Xoá") } },
            dismissButton = { TextButton({ deleting = null }) { Text("Huỷ") } }
        )
    }
}

@Composable private fun CompareCard(first: DiaryEntry, latest: DiaryEntry) {
    val days = ((latest.at - first.at) / DAY).toInt()
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Sự lớn lên", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Ban đầu" to first, "Mới nhất" to latest).forEach { (label, e) ->
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PhotoImage(e.photoUri, Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)))
                        Text("$label · ${fmt(e.at)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Text("Sau $days ngày theo dõi", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable fun CareChart(logs: List<CareLog>) {
    val today = startOfDay()
    val counts = remember(logs, today) {
        (13 downTo 0).map { d -> val s = today - d * DAY; logs.count { it.at >= s && it.at < s + DAY } }
    }
    val mx = maxOf(counts.max(), 1)
    val color = MaterialTheme.colorScheme.primary
    val base = MaterialTheme.colorScheme.outlineVariant
    Canvas(Modifier.fillMaxWidth().height(110.dp).padding(top = 12.dp)) {
        val w = size.width / counts.size
        val r = CornerRadius(w * 0.2f)
        counts.forEachIndexed { i, c ->
            val h = if (c == 0) 6f else (size.height - 6f) * c / mx + 6f
            drawRoundRect(if (c == 0) base else color, Offset(i * w + w * 0.15f, size.height - h), Size(w * 0.7f, h), r)
        }
    }
    Text("Cao nhất ${counts.max()} lần/ngày · Tổng ${counts.sum()} lần", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable fun EntryCard(e: DiaryEntry, onDelete: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(remember(e.at) { fmt(e.at) }, style = MaterialTheme.typography.titleSmall)
                IconButton(onDelete) { Icon(Icons.Outlined.Delete, "Xoá mốc") }
            }
            if (e.note.isNotBlank()) Text(e.note, Modifier.padding(end = 8.dp))
            if (e.photoUri.isNotEmpty()) PhotoImage(
                e.photoUri,
                Modifier.fillMaxWidth().padding(end = 8.dp, top = 8.dp).height(220.dp).clip(RoundedCornerShape(18.dp))
            )
        }
    }
}
