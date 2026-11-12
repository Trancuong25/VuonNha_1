package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SPECIES
import com.example.vuonnha.data.SpeciesGroup
import com.example.vuonnha.data.customSpecies
import com.example.vuonnha.data.findSpecies
import org.junit.Assert.*
import org.junit.Test

class GroupPresentationTest {
    @Test fun plantCopyMatchesExistingAddSheet() {
        val copy = groupCopy(SpeciesGroup.PLANT)
        assertEquals("Chọn cây", copy.stepOne)
        assertEquals("Góc trồng", copy.stepTwo)
        assertEquals("Bắt đầu với tên cây. Bạn có thể tra cứu hoặc tự nhập.", copy.stepOneDescription)
        assertEquals("Bạn đang trồng cây gì?", copy.stepOneTitle)
        assertEquals("Tên loài hoặc mô tả *", copy.speciesLabel)
        assertEquals("VD: Cây kim tiền, cây lá tím chưa rõ tên", copy.speciesPlaceholder)
        assertEquals("Cây sẽ ở đâu?", copy.stepTwoTitle)
        assertEquals("Đặt tên thân quen và chọn một góc cho cây. Các mục này đều tuỳ chọn.", copy.stepTwoDescription)
        assertEquals("Lịch chăm cây", copy.stepThreeTitle)
        assertEquals("Đặt lịch kiểm tra cây; chỉ tưới hoặc bón khi cây cần.", copy.stepThreeDescription)
        assertEquals("Sẽ lưu hồ sơ cây", copy.saveSummaryPrefix)
    }

    @Test fun groupFieldsHaveDistinctKeysAndOnlyPlantOwnsPlantFields() {
        for (group in SpeciesGroup.entries) {
            val keys = groupFields(group).map { it.key }
            assertEquals(keys.size, keys.toSet().size)
            assertTrue("location missing for $group", "location" in keys)
        }
        val plant = groupFields(SpeciesGroup.PLANT).map { it.key }.toSet()
        val fish = groupFields(SpeciesGroup.FISH).map { it.key }.toSet()
        val pet = groupFields(SpeciesGroup.PET).map { it.key }.toSet()
        assertEquals(setOf("actualHeight", "location", "actualLight", "pottingMix"), plant)
        assertEquals(setOf("location", "tankVolume", "fishCount", "filterType"), fish)
        assertEquals(setOf("location", "petAge", "petWeight", "housing"), pet)
        assertTrue((fish + pet).intersect(REFERENCE_KEYS - "actualHeight").isEmpty())
        assertTrue((plant - "location").intersect(fish + pet).isEmpty())
    }

    @Test fun builtInAndCustomSpeciesReportTheirGroup() {
        assertEquals(SpeciesGroup.FISH, SPECIES.first { it.id == "betta" }.group)
        assertEquals(SpeciesGroup.FISH, SPECIES.first { it.id == "vang" }.group)
        assertEquals(SpeciesGroup.FISH, SPECIES.first { it.id == "guppy" }.group)
        assertEquals(SpeciesGroup.PET, SPECIES.first { it.id == "hamster" }.group)
        assertEquals(SpeciesGroup.PLANT, SPECIES.first { it.id == "monstera" }.group)
        assertEquals(SpeciesGroup.PET, customSpecies("Mèo", SpeciesGroup.PET).group)
        assertEquals(SpeciesGroup.PET, findSpecies("custom:PET:Mèo")?.group)
    }
}
