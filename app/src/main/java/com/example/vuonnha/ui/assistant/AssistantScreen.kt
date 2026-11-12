package com.example.vuonnha.ui.assistant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vuonnha.ui.common.ScreenHeader
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.AutoAwesome

@Composable fun AssistantScreen(vm: AssistantViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var draft by rememberSaveable { mutableStateOf("") }
    var deleting by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    val scroll = rememberLazyListState()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(30000); now = System.currentTimeMillis() } }
    val messages = state.messages.filter { now - it.at < 48 * 60 * 60 * 1000L - 60000 }
    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) scroll.animateScrollToItem(messages.size) }
    Column(Modifier.fillMaxSize().imePadding().padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) { ScreenHeader("Trợ lý cây", "Cùng bạn hiểu và chăm góc xanh") }
            Box {
                IconButton({ menu = true }) { Icon(Icons.Outlined.MoreVert, "Tuỳ chọn trò chuyện") }
                DropdownMenu(menu, { menu = false }) { DropdownMenuItem(text = { Text("Xoá dữ liệu Trợ lý") }, onClick = { menu = false; deleting = true }, enabled = !state.busy) }
            }
        }
        Text("Tin nhắn tự xoá sau tối đa 48 giờ. AI có thể nhầm; hãy kiểm tra trước khi áp dụng.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(Modifier.weight(1f), state = scroll, verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
            item {
                if (messages.isEmpty()) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Hôm nay bạn muốn hỏi gì?", style = MaterialTheme.typography.titleLarge)
                        Text("Khi gửi câu hỏi, app đồng bộ tên cây, điều kiện trồng, lịch nhắc và lịch sử chăm sóc của bạn. Không gửi ảnh hoặc ghi chú nhật ký.", style = MaterialTheme.typography.bodyMedium)
                        listOf("Hôm qua tôi đã chăm sóc cây nào?", "Cây trong vườn của tôi cần lưu ý gì khi tưới?").forEach { question ->
                            OutlinedButton({ draft = question }, Modifier.fillMaxWidth(), enabled = !state.busy) { Text(question) }
                        }
                    } }
                }
            }
            items(messages) { message ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.role == "user") Arrangement.End else Arrangement.Start) {
                    Surface(Modifier.widthIn(max = 320.dp), shape = MaterialTheme.shapes.large,
                        color = if (message.role == "user") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(if (message.role == "user") "Bạn" else "Trợ lý", style = MaterialTheme.typography.labelMedium)
                            Text(message.text, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
            if (state.busy) item { Text("Đang xử lý…", color = MaterialTheme.colorScheme.primary) }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(draft, { if (it.length <= 1000) draft = it }, Modifier.weight(1f), label = { Text("Hỏi Trợ lý") }, maxLines = 3, enabled = !state.busy)
            Button({ vm.ask(draft) { draft = "" } }, Modifier.heightIn(min = 52.dp), enabled = !state.busy && draft.isNotBlank()) { Text("Gửi") }
        }
    }
    if (deleting) AlertDialog(onDismissRequest = { deleting = false }, title = { Text("Xoá dữ liệu Trợ lý?") },
        text = { Text("Xoá hội thoại và bản đồng bộ vườn trên backend. Vườn, lịch và ảnh trên điện thoại vẫn được giữ. Lần hỏi tiếp theo sẽ đồng bộ lại.") },
        confirmButton = { TextButton({ deleting = false; vm.clear(true) }) { Text("Xoá") } },
        dismissButton = { TextButton({ deleting = false }) { Text("Giữ lại") } })
}
