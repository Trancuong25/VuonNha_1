package com.example.vuonnha.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vuonnha.data.Zone

fun zoneLabel(z: Zone?) = if (z == null) "" else "${z.emoji} ${z.name}"

/** Hàng chip lọc theo khu vực. selected = null nghĩa là Tất cả. */
@Composable fun ZoneFilterRow(zones: List<Zone>, selected: Long?, onSelect: (Long?) -> Unit, trailing: @Composable () -> Unit = {}) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected == null, { onSelect(null) }, label = { Text("Tất cả") }) }
        items(zones, key = { it.id }) { z -> FilterChip(selected == z.id, { onSelect(z.id) }, label = { Text(zoneLabel(z)) }) }
        item { trailing() }
    }
}

/** Chọn khu vực cho một cây (có lựa chọn "Chưa phân khu"). */
@OptIn(ExperimentalLayoutApi::class)
@Composable fun ZonePicker(zones: List<Zone>, selected: Long?, onSelect: (Long?) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected == null, { onSelect(null) }, label = { Text("Chưa phân khu") })
        zones.forEach { z -> FilterChip(selected == z.id, { onSelect(z.id) }, label = { Text(zoneLabel(z)) }) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ZonesSheet(
    zones: List<Zone>, onAdd: (String, Boolean, Boolean, String) -> Unit, onRemove: (Zone) -> Unit, onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var outdoor by remember { mutableStateOf(true) }
    var covered by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp)
                .navigationBarsPadding().imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Khu vực", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Chia vườn theo khu vực (VD: Sân thượng, Phòng khách). Khu vực ngoài trời sẽ được gợi ý tưới theo thời tiết. Xoá khu vực không xoá cây, cây chỉ về \"Chưa phân khu\".",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            zones.forEach { z ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(z.emoji, fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(z.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (!z.outdoor) "Trong nhà" else if (z.covered) "Ngoài trời · có mái che" else "Ngoài trời",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton({ onRemove(z) }) { Icon(Icons.Outlined.Delete, "Xoá khu vực", tint = MaterialTheme.colorScheme.error) }
                }
            }
            HorizontalDivider()
            Text("Thêm khu vực", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Tên khu vực") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(!outdoor, { outdoor = false }, label = { Text("🏠 Trong nhà") })
                FilterChip(outdoor, { outdoor = true }, label = { Text("☀️ Ngoài trời") })
            }
            if (outdoor) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Có mái che (mưa không tới)", Modifier.weight(1f))
                Switch(covered, { covered = it })
            }
            Button(
                enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth().height(48.dp),
                onClick = {
                    onAdd(name.trim(), outdoor, outdoor && covered, if (!outdoor) "🏠" else if (covered) "⛱️" else "☀️")
                    name = ""
                }
            ) { Text("Thêm khu vực") }
        }
    }
}
