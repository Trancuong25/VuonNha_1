package com.example.vuonnha.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vuonnha.ui.common.ScreenHeader
import com.example.vuonnha.ui.theme.ThemePrefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val ctx = LocalContext.current
    var confirm by remember { mutableStateOf(false) }
    val exportL = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) vm.backup(uri)
    }
    val importL = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.restore(uri)
    }
    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenHeader("Cài đặt", "Giao diện, nhắc nhở và dữ liệu")

        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Giao diện", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "Theo hệ thống", 1 to "Sáng", 2 to "Tối").forEach { (m, label) ->
                    FilterChip(selected = ThemePrefs.mode == m, onClick = { ThemePrefs.set(ctx, m) }, label = { Text(label) })
                }
            }
        } }

        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Thông báo nhắc nhở", style = MaterialTheme.typography.titleMedium)
            Text("Nhắc mỗi ngày vào giờ bạn chọn nếu có việc đến hạn. Android có thể lệch vài phút để tiết kiệm pin.", style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Giờ nhắc", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                FilledTonalIconButton({ vm.shiftReminder(-30) }) { Icon(Icons.Filled.Remove, "Sớm hơn 30 phút") }
                Text("%02d:%02d".format(vm.reminderMinutes / 60, vm.reminderMinutes % 60), Modifier.widthIn(min = 64.dp),
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
                FilledTonalIconButton({ vm.shiftReminder(30) }) { Icon(Icons.Filled.Add, "Muộn hơn 30 phút") }
            }
            OutlinedButton(onClick = { vm.testNotification() }) { Text("Gửi thông báo thử") }
        } }

        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sao lưu dữ liệu", style = MaterialTheme.typography.titleMedium)
            Text("File ZIP gồm danh sách, chu kỳ, lịch sử chăm sóc, ghi chú và cả ảnh nhật ký. Vẫn nhập được file JSON cũ (không có ảnh).", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { exportL.launch("vuonnha-" + SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()) + ".zip") }) { Text("Xuất") }
                OutlinedButton(onClick = { confirm = true }) { Text("Nhập") }
            }
            if (vm.message.isNotEmpty()) Text(vm.message, style = MaterialTheme.typography.bodyMedium)
        } }

        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Quyền riêng tư", style = MaterialTheme.typography.titleMedium)
            Text("Dữ liệu và ảnh chỉ lưu trong bộ nhớ riêng của app trên máy này. Riêng tab Thời tiết gửi toạ độ gần đúng (hoặc tên thành phố bạn tìm) tới Open-Meteo để lấy dự báo.", style = MaterialTheme.typography.bodySmall)
        } }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false }, title = { Text("Nhập dữ liệu?") },
        text = { Text("Toàn bộ dữ liệu hiện tại sẽ bị thay bằng dữ liệu trong file.") },
        confirmButton = { TextButton(onClick = { confirm = false; importL.launch(arrayOf("application/zip", "application/json", "*/*")) }) { Text("Chọn file") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Huỷ") } }
    )
}
