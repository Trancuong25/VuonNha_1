package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup

internal fun groupCareSuggestions(group: SpeciesGroup): List<Pair<String, Int>> = when (group) {
    SpeciesGroup.PLANT -> listOf("Kiểm tra độ ẩm đất" to 3, "Kiểm tra dinh dưỡng" to 30)
    SpeciesGroup.FISH -> listOf("Cho ăn" to 1, "Thay nước" to 7, "Vệ sinh lọc" to 21)
    SpeciesGroup.PET -> listOf("Cho ăn" to 1, "Thay nước uống" to 1, "Thay lót chuồng" to 7)
}
