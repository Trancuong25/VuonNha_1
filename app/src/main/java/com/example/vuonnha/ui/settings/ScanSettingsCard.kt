package com.example.vuonnha.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.vuonnha.data.ScanSettings

@Composable
fun ScanSettingsCard() {
    val ctx = LocalContext.current
    var visible by remember { mutableStateOf(!ScanSettings.hidden(ctx)) }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        ListItem(headlineContent = { Text("Nút quét cây") }, supportingContent = { Text("Hiển thị trên thanh điều hướng") },
            trailingContent = { Switch(visible, { visible = it; ScanSettings.hide(ctx, !it) }) })
    }
}
