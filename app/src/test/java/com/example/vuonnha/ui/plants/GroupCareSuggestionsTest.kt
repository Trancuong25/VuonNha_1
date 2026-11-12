package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup
import com.example.vuonnha.data.TaskType
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupCareSuggestionsTest {
    @Test fun plantSuggestionsMatchExistingNamesAndCycles() {
        assertEquals(listOf("Kiểm tra độ ẩm đất" to 3, "Kiểm tra dinh dưỡng" to 30),
            groupCareSuggestions(SpeciesGroup.PLANT))
    }

    @Test fun fishAndPetSuggestionsHaveExpectedNamesAndCycles() {
        assertEquals(listOf("Cho ăn" to 1, "Thay nước" to 7, "Vệ sinh lọc" to 21),
            groupCareSuggestions(SpeciesGroup.FISH))
        assertEquals(listOf("Cho ăn" to 1, "Thay nước uống" to 1, "Thay lót chuồng" to 7),
            groupCareSuggestions(SpeciesGroup.PET))
    }

    @Test fun suggestionNamesInferExpectedTaskTypes() {
        assertEquals(TaskType.FEED, TaskType.infer("Cho ăn"))
        assertEquals(TaskType.WATER_CHANGE, TaskType.infer("Thay nước"))
        assertEquals(TaskType.CLEAN, TaskType.infer("Vệ sinh lọc"))
        assertEquals(TaskType.WATER_CHANGE, TaskType.infer("Thay nước uống"))
        assertEquals(TaskType.BEDDING, TaskType.infer("Thay lót chuồng"))
    }

    @Test fun plantStepThreeIntroIsUnchanged() {
        assertEquals("Chọn việc muốn được nhắc. Bạn có thể lưu cây mà chưa đặt lịch.",
            groupCopy(SpeciesGroup.PLANT).stepThreeIntro)
    }

    @Test fun editAndDetailLabelsMatchTheSelectedGroup() {
        assertEquals("Thông tin cây", groupCopy(SpeciesGroup.PLANT).detailTitle)
        assertEquals("Thông tin bể cá", groupCopy(SpeciesGroup.FISH).detailTitle)
        assertEquals("Thông tin thú cưng", groupCopy(SpeciesGroup.PET).detailTitle)
        assertEquals(listOf("Vị trí đặt bể", "Dung tích bể (lít)", "Số lượng cá", "Hệ thống lọc (không bắt buộc)"),
            groupFields(SpeciesGroup.FISH).map { it.label })
        assertEquals(listOf("Nơi ở", "Tuổi hoặc ngày nhận nuôi", "Cân nặng (g/kg)", "Loại chuồng / chỗ ở (không bắt buộc)"),
            groupFields(SpeciesGroup.PET).map { it.label })
    }
}
