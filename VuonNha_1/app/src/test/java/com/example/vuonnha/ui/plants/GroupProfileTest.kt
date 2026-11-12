package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup
import org.junit.Assert.*
import org.junit.Test

class GroupProfileTest {
    @Test fun fishSaveKeepsTankFieldsAndDropsPlantFields() {
        val profile = mapOf("location" to "Phòng khách", "tankVolume" to "20", "fishCount" to "5",
            "filterType" to "Lọc treo", "actualLight" to "Nắng sáng", "scientificName" to "Cũ")
        assertEquals(mapOf("location" to "Phòng khách", "tankVolume" to "20", "fishCount" to "5", "filterType" to "Lọc treo"),
            profileToSave(profile, true, SpeciesGroup.FISH))
    }

    @Test fun petSaveKeepsAgeAndDropsFishFields() {
        val profile = mapOf("location" to "Phòng ngủ", "petAge" to "8 tháng", "petWeight" to "120 g",
            "housing" to "Chuồng", "tankVolume" to "20")
        assertEquals(mapOf("location" to "Phòng ngủ", "petAge" to "8 tháng", "petWeight" to "120 g", "housing" to "Chuồng"),
            profileToSave(profile, false, SpeciesGroup.PET))
    }

    @Test fun groupChangeKeepsLocationAndRemovesOldGroupFields() {
        val profile = mapOf("location" to "Kệ", "tankVolume" to "20", "fishCount" to "5", "actualLight" to "Sáng", "soil" to "Đất")
        val result = profileAfterGroupChange(profile, setOf("location", "fishCount", "soil"), SpeciesGroup.FISH, SpeciesGroup.PET)
        assertEquals(mapOf("location" to "Kệ"), result.profile)
        assertEquals(setOf("location"), result.editedFields)
    }

    @Test fun plantSaveKeepsExistingBehavior() {
        val profile = mapOf("location" to "Ban công", "actualHeight" to "35", "actualLight" to "Sáng", "soil" to "Thoát nước")
        assertEquals(profile, profileToSave(profile, true, SpeciesGroup.PLANT))
        assertEquals(profile - setOf("soil", "actualHeight"), profileToSave(profile, false, SpeciesGroup.PLANT))
    }
}
