package com.example.vuonnha.ui.plants

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.vuonnha.data.PlantCandidate
import com.example.vuonnha.ui.common.ScreenHeader
import com.example.vuonnha.ui.common.ProfileInfo
import androidx.compose.ui.draw.clip

/** Reviewing recognition never saves a plant; Next opens the separate editable form. */
@Composable
fun PlantRecognitionResult(candidates: List<PlantCandidate>, selected: PlantCandidate, photo: Bitmap?,
    onSelect: (PlantCandidate) -> Unit, onNext: () -> Unit, onRetake: () -> Unit, onBack: () -> Unit,
    existing: List<Pair<Long, String>> = emptyList(), onOpenExisting: (Long) -> Unit = {}) {
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextButton(onBack) { Text("‹ Quay lại ảnh") }
                ScreenHeader("Kết quả nhận diện", "Bước 1/2 · Kiểm tra loài, sau đó đặt tên và lịch nhắc")
                photo?.let { Image(it.asImageBitmap(), "Ảnh vừa quét", Modifier.fillMaxWidth().height(220.dp).clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerLow), contentScale = ContentScale.Fit) }
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(selected.name, style = MaterialTheme.typography.titleLarge)
                        Text(selected.scientificName, style = MaterialTheme.typography.bodyMedium)
                        Text("Mức chắc chắn: ${when(selected.confidence) { "high" -> "cao"; "medium" -> "vừa"; else -> "thấp — hãy kiểm tra lại" }}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (selected.confidence == "low") Text("Chưa đủ chắc để kết luận. Bạn có thể chụp rõ lá hoặc hoa hơn, hoặc sửa loài ở bước tiếp theo.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        HorizontalDivider()
                        Text("Đặc điểm nhận diện", style = MaterialTheme.typography.titleSmall)
                        selected.features.forEach { Text("• $it") }
                        Text("Chăm sóc cơ bản", style = MaterialTheme.typography.titleSmall)
                        Text(selected.careTips)
                        listOf("light" to "Ánh sáng", "soil" to "Giá thể & tưới", "humidity" to "Độ ẩm không khí", "matureHeight" to "Chiều cao tham khảo").forEach { (key, label) ->
                            selected.profile[key]?.takeIf { it.isNotBlank() }?.let { ProfileInfo(label, it) }
                        }
                        Text("Gợi ý có thể nhầm. Tiếp theo chỉ mở biểu mẫu, chưa lưu cây.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (candidates.size > 1) {
                    Text("Loài có thể phù hợp", style = MaterialTheme.typography.titleMedium)
                    candidates.forEach { candidate ->
                        FilterChip(selected == candidate, { onSelect(candidate) }, label = { Text(candidate.name) })
                    }
                }
                if (existing.isNotEmpty()) {
                    Text("Trong vườn có loài tương tự", style = MaterialTheme.typography.titleMedium)
                    Text("Cùng loài chưa có nghĩa là cùng một cây. Bạn có thể mở cây cũ hoặc tiếp tục thêm cây mới.")
                    existing.forEach { (id, name) -> TextButton({ onOpenExisting(id) }) { Text("Mở $name") } }
                }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onRetake, Modifier.weight(1f).heightIn(min = 52.dp)) { Text("Chụp lại") }
                Button(onNext, Modifier.weight(1f).heightIn(min = 52.dp)) { Text("Tiếp theo") }
            }
        }
    }
}
