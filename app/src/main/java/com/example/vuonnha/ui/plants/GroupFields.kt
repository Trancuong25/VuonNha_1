package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup

internal data class GroupField(
    val key: String,
    val label: String,
    val placeholder: String = "",
    val singleLine: Boolean = false,
    val detailLabel: String = label,
    val reviewLabel: String = label,
    val numeric: Boolean = false
)

internal fun groupFields(group: SpeciesGroup): List<GroupField> = when (group) {
    SpeciesGroup.PLANT -> listOf(
        GroupField("actualHeight", "Chiều cao hiện tại (cm, tự đo)", "VD: 35", true, "Chiều cao hiện tại (cm)", "Chiều cao hiện tại (cm)"),
        GroupField("location", "Vị trí đặt cây", "VD: Ban công", true, "Vị trí", "Vị trí"),
        GroupField("actualLight", "Ánh sáng nơi trồng", "VD: Nắng sáng 2 giờ, còn lại ánh sáng gián tiếp"),
        GroupField("pottingMix", "Chậu & giá thể (không bắt buộc)", "VD: Chậu có lỗ thoát nước, đất trộn xơ dừa", detailLabel = "Chậu & giá thể", reviewLabel = "Chậu và giá thể")
    )
    SpeciesGroup.FISH -> listOf(
        GroupField("location", "Vị trí đặt bể", "VD: Kệ TV phòng khách", true),
        GroupField("tankVolume", "Dung tích bể (lít)", "VD: 20", true, numeric = true),
        GroupField("fishCount", "Số lượng cá", "VD: 5", true, numeric = true),
        GroupField("filterType", "Hệ thống lọc (không bắt buộc)", "VD: Lọc thùng, lọc treo")
    )
    SpeciesGroup.PET -> listOf(
        GroupField("location", "Nơi ở", "VD: Chuồng ở phòng khách", true),
        GroupField("petAge", "Tuổi hoặc ngày nhận nuôi", "VD: 8 tháng", true),
        GroupField("petWeight", "Cân nặng (g/kg)", "VD: 120 g", true),
        GroupField("housing", "Loại chuồng / chỗ ở (không bắt buộc)")
    )
}

internal fun compactLocationField(group: SpeciesGroup): GroupField = groupFields(group).first { it.key == "location" }

internal fun compactDetailFields(group: SpeciesGroup): List<GroupField> = groupFields(group).filterNot { it.key == "location" }
