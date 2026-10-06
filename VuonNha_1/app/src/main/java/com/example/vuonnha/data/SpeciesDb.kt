package com.example.vuonnha.data

// Cơ sở dữ liệu kiến thức chăm sóc. Thêm loài = thêm 1 dòng.
data class Species(
    val id: String, val name: String, val tip: String,
    val tasks: List<Pair<String, Int>>, val emoji: String = "🌿"
)

val SPECIES = listOf(
    Species("monstera", "Trầu bà lá xẻ", "Ánh sáng tán xạ; tưới khi đất mặt khô 3–4 cm.", listOf("Tưới nước" to 7, "Bón phân" to 30), "🪴"),
    Species("sen", "Sen đá", "Nhiều nắng, đất thoát nước tốt.", listOf("Tưới nước" to 10, "Bón phân" to 60), "🌵"),
    Species("lan", "Lan hồ điệp", "Không để đọng nước ở nõn.", listOf("Tưới nước" to 5, "Bón phân" to 14), "🌸"),
    Species("basil", "Húng quế", "Giữ đất ẩm nhẹ; bấm ngọn thường xuyên để ra nhiều nhánh.", listOf("Tưới nước" to 2, "Bón phân" to 14), "🌿"),
    Species("betta", "Cá betta", "Thay 25–30% nước, khử clo.", listOf("Thay nước" to 5, "Vệ sinh bể" to 14), "🐟"),
    Species("vang", "Cá vàng", "Cần lọc mạnh.", listOf("Thay nước" to 7, "Vệ sinh lọc" to 21), "🐠"),
    Species("guppy", "Cá bảy màu", "Thay 20–30% nước mỗi tuần, khử clo.", listOf("Thay nước" to 7, "Vệ sinh lọc" to 21), "🐟"),
    Species("hamster", "Hamster", "Không tắm nước, dùng cát tắm.", listOf("Thay lót chuồng" to 7, "Tắm cát" to 4), "🐹"),
)
fun findSpecies(id: String) = SPECIES.firstOrNull { it.id == id }

// Emoji theo tên việc (dùng khi chưa có đối tượng CareTask, ví dụ lúc chọn loài). Với CareTask dùng t.icon().
fun taskEmoji(name: String): String = TaskType.infer(name).emoji
