package com.example.vuonnha.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable fun ScreenHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable fun SectionHeading(number: String, title: String, description: String) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
            Text(number, Modifier.padding(12.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable fun ProfileInfo(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Ảnh đại diện: dùng ảnh mới nhất trong nhật ký nếu có, không thì emoji của loài. */
@Composable fun EmojiAvatar(emoji: String, photo: String?, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier.size(size).clip(RoundedCornerShape(size * 0.3f)).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (!photo.isNullOrEmpty()) PhotoImage(photo, Modifier.fillMaxSize())
        else Text(emoji, fontSize = (size.value * 0.48f).sp)
    }
}

@Composable fun EmptyState(emoji: String, title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        EmojiAvatar(emoji, null, 72.dp)
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

fun statusText(d: Int) = when {
    d < 0 -> "Trễ ${-d} ngày"; d == 0 -> "Hôm nay"; d == 1 -> "Ngày mai"; else -> "Còn $d ngày"
}

@Composable fun statusColor(d: Int): Color = when {
    d < 0 -> MaterialTheme.colorScheme.error
    d == 0 -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable fun StatusPill(text: String, d: Int) {
    val bg = when {
        d < 0 -> MaterialTheme.colorScheme.errorContainer
        d == 0 -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(shape = CircleShape, color = bg) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium)
    }
}

/** Bộ chỉnh chu kỳ bằng nút − / +, thay cho gõ số (nhanh hơn, không sai định dạng). */
@Composable fun CycleStepper(label: String, value: Int, onChange: (Int) -> Unit) {
    fun step(v: Int) = if (v >= 30) 5 else 1
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, maxLines = 2)
        FilledTonalIconButton({ onChange((value - step(value - 1)).coerceAtLeast(1)) }) { Icon(Icons.Filled.Remove, "Giảm") }
        Text("$value ngày", Modifier.widthIn(min = 68.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleSmall)
        FilledTonalIconButton({ onChange((value + step(value)).coerceAtMost(365)) }) { Icon(Icons.Filled.Add, "Tăng") }
    }
}
