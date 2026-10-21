package com.example.vuonnha.ui.weather

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vuonnha.data.*
import com.example.vuonnha.ui.common.EmptyState
import com.example.vuonnha.ui.common.ScreenHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val IN = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val OUT = SimpleDateFormat("EEE dd/MM", Locale("vi", "VN"))
private fun dayLabel(d: String, i: Int) = when (i) {
    0 -> "Hôm nay"; 1 -> "Ngày mai"; else -> runCatching { OUT.format(IN.parse(d)!!) }.getOrDefault(d)
}

@Composable fun WeatherScreen(vm: WeatherViewModel = viewModel(), onBack: (() -> Unit)? = null) {
    val weather by vm.weather.collectAsStateWithLifecycle()
    val place by vm.place.collectAsStateWithLifecycle()
    val items by vm.items.collectAsStateWithLifecycle()
    val zones by vm.zones.collectAsStateWithLifecycle()
    var changing by remember { mutableStateOf(false) }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) { if (vm.useCurrent()) changing = false } else vm.fail("Bạn đã từ chối quyền vị trí. Hãy tìm thành phố.")
    }
    // số cây đang ở khu ngoài trời không mái che (loại bị ảnh hưởng bởi mưa)
    val exposed = remember(items, zones) {
        val open = zones.filter { it.outdoor && !it.covered }.map { it.id }.toSet()
        items.count { it.zoneId in open }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            onBack?.let { TextButton(it) { Text("‹ Vườn nhà") } }
            ScreenHeader("Thời tiết", place?.name ?: "Chưa chọn vị trí")
        }
        if (place == null || changing) item {
            LocationCard(vm, onCurrent = { permLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) }, onPicked = { changing = false })
        }
        vm.error?.let { msg -> item { Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) } }
        val w = weather
        if (place != null && w != null) {
            item { Hero(w) }
            item { AdviceCard(w, exposed) }
            item { ForecastCard(w) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton({ vm.refresh(true) }, enabled = !vm.loading) { Text(if (vm.loading) "Đang tải…" else "Làm mới") }
                    OutlinedButton({ changing = !changing }) { Text("Đổi vị trí") }
                }
            }
            item {
                val t = remember(w.fetchedAt) { SimpleDateFormat("HH:mm dd/MM", Locale.getDefault()).format(Date(w.fetchedAt)) }
                Text("Cập nhật $t · Dữ liệu thời tiết: Open-Meteo.com (CC BY 4.0)", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (place != null) {
            item { EmptyState("⏳", if (vm.loading) "Đang tải dự báo…" else "Chưa có dữ liệu", "Kiểm tra kết nối mạng rồi bấm Đổi vị trí hoặc mở lại tab này.") }
        }
    }
}

@Composable private fun LocationCard(vm: WeatherViewModel, onCurrent: () -> Unit, onPicked: () -> Unit) {
    var q by remember { mutableStateOf("") }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Chọn vị trí vườn", style = MaterialTheme.typography.titleMedium)
            Text("Để gợi ý tưới cây chính xác theo thời tiết nơi bạn ở.", style = MaterialTheme.typography.bodySmall)
            Button(onCurrent, Modifier.fillMaxWidth()) { Text("📍 Dùng vị trí hiện tại") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(q, { q = it }, Modifier.weight(1f), singleLine = true, label = { Text("Hoặc tìm thành phố") })
                Button({ vm.search(q) }, enabled = q.isNotBlank() && !vm.searching) { Text(if (vm.searching) "…" else "Tìm") }
            }
            vm.results.forEach { p ->
                ListItem(headlineContent = { Text(p.name) }, modifier = Modifier.clickable { vm.choose(p); onPicked() })
            }
        }
    }
}

@Composable private fun Hero(w: WeatherData) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(weatherEmoji(w.code), fontSize = 56.sp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text("${w.temp.roundToInt()}°C", style = MaterialTheme.typography.headlineMedium)
                Text(weatherText(w.code), style = MaterialTheme.typography.titleMedium)
                Text("Độ ẩm ${w.humidity}%", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable private fun AdviceCard(w: WeatherData, exposed: Int) {
    val today = w.days.firstOrNull()
    val tomorrow = w.days.getOrNull(1)
    val lines = buildList {
        if (today != null) when {
            today.isRainy() -> add("🌧️ Hôm nay có mưa (~${today.rainMm.roundToInt()} mm, xác suất ${today.rainProb}%). Cây ngoài trời không mái che có thể bỏ tưới.")
            today.isHot() -> add("🔥 Nắng nóng ${today.tMax.roundToInt()}°C. Tưới sáng sớm hoặc chiều tối, tránh lúc nắng gắt.")
            else -> add("✅ Thời tiết ổn, tưới theo lịch bình thường.")
        }
        if (tomorrow != null && tomorrow.isRainy()) add("🌦️ Ngày mai có mưa, có thể để dành việc tưới.")
        if (exposed > 0) add("🪴 $exposed cây đang ở khu vực ngoài trời không mái che.")
    }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Gợi ý cho vườn", style = MaterialTheme.typography.titleMedium)
            lines.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable private fun ForecastCard(w: WeatherData) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("7 ngày tới", style = MaterialTheme.typography.titleMedium)
            w.days.forEachIndexed { i, d ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(weatherEmoji(d.code), fontSize = 24.sp)
                    Text(dayLabel(d.date, i), Modifier.weight(1f).padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
                    Text("💧 ${d.rainProb}%", Modifier.width(72.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall)
                    Text("${d.tMin.roundToInt()}° / ${d.tMax.roundToInt()}°", Modifier.width(84.dp), textAlign = TextAlign.End,
                        style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
