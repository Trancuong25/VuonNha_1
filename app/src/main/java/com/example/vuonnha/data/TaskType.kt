package com.example.vuonnha.data

/**
 * Loại việc chăm sóc. Lưu trong DB dưới dạng chuỗi (CareTask.type) để logic như gợi ý thời tiết
 * không còn phụ thuộc vào việc người dùng đặt tên việc là gì.
 */
enum class TaskType(val label: String, val emoji: String) {
    WATER("Tưới nước", "💧"),
    FEED("Cho ăn", "🍽️"),
    FERTILIZE("Bón phân", "🌱"),
    WATER_CHANGE("Thay nước", "🪣"),
    CLEAN("Vệ sinh", "🧽"),
    BEDDING("Thay lót chuồng", "🧺"),
    BATH("Tắm / vệ sinh cá thể", "🛁"),
    OTHER("Khác", "✅");

    companion object {
        fun from(key: String?): TaskType = entries.firstOrNull { it.name == key } ?: OTHER

        /** Đoán loại việc từ tên (dùng khi tạo việc mới hoặc nâng cấp dữ liệu cũ). */
        fun infer(name: String): TaskType {
            val n = name.lowercase()
            return when {
                "phân" in n -> FERTILIZE
                "thay nước" in n -> WATER_CHANGE
                "tưới" in n -> WATER
                "cho ăn" in n || "thức ăn" in n -> FEED
                "vệ sinh" in n || "lọc" in n -> CLEAN
                "lót" in n -> BEDDING
                "tắm" in n -> BATH
                else -> OTHER
            }
        }
    }
}

fun CareTask.icon(): String = TaskType.from(type).emoji
