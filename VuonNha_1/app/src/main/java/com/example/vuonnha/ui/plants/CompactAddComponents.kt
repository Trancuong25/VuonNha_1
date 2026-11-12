package com.example.vuonnha.ui.plants

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vuonnha.data.Species
import com.example.vuonnha.data.SpeciesGroup
import com.example.vuonnha.data.TaskType

@Composable
internal fun CompactProgress(step: Int, modifier: Modifier = Modifier) {
    Row(modifier.semantics { contentDescription = "Bước ${step + 1} trên 3" },
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            Surface(Modifier.size(width = if (index == step) 22.dp else 8.dp, height = 8.dp),
                shape = RoundedCornerShape(50), color = if (index == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant) {}
        }
    }
}

@Composable
internal fun CompactGroupCard(group: SpeciesGroup, selected: Boolean, onClick: () -> Unit) {
    Surface(Modifier.heightIn(min = 72.dp).clickable(onClick = onClick)
        .semantics { contentDescription = "${group.label}, ${if (selected) "đã chọn" else "chưa chọn"}"; this.selected = selected },
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(group.emoji, fontSize = 30.sp)
            Text(if (group == SpeciesGroup.PLANT) "Cây" else if (group == SpeciesGroup.FISH) "Cá" else "Thú cưng",
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun CompactSpeciesRow(species: Species, selected: Boolean, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClick = onClick)
        .semantics { contentDescription = "${species.name}, ${if (selected) "đã chọn" else "chọn loài"}"; this.selected = selected },
        shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) { Text(species.emoji) }
            }
            Text(species.name, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (selected) Text("✓", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
internal fun CompactTaskRow(name: String, days: Int, checked: Boolean, onToggle: (Boolean) -> Unit, onEdit: () -> Unit) {
    Surface(Modifier.fillMaxWidth().heightIn(min = 72.dp), shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(TaskType.infer(name).emoji, fontSize = 26.sp)
            Column(Modifier.weight(1f).heightIn(min = 48.dp).clickable(onClick = onEdit).padding(start = 12.dp),
                verticalArrangement = Arrangement.Center) {
                Text(name, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Mỗi $days ngày", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked, onToggle, modifier = Modifier.semantics { contentDescription = "Nhắc $name" })
        }
    }
}

@Composable
internal fun CompactPrimaryButton(label: String, enabled: Boolean, modifier: Modifier = Modifier,
    loading: Boolean = false, onClick: () -> Unit) {
    Button(onClick, modifier.heightIn(min = 54.dp), enabled = enabled,
        shape = RoundedCornerShape(50)) {
        if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        else Text(label)
    }
}
