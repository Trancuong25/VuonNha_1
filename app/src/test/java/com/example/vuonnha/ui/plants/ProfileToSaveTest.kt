package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup
import org.junit.Assert.*
import org.junit.Test

class ProfileToSaveTest {
    @Test fun fishAndPetsKeepOnlyTheirOwnFields() {
        val profile = mapOf("location" to "Bàn học", "actualLight" to "Gần cửa sổ", "pottingMix" to "Bể riêng", "soil" to "Đất ẩm")
        for (group in listOf(SpeciesGroup.FISH, SpeciesGroup.PET)) {
            for (keepExtra in listOf(true, false)) {
                val saved = profileToSave(profile, keepExtra, group)
                assertEquals(mapOf("location" to "Bàn học"), saved)
            }
        }
    }

    @Test fun plantKeepsReferencesOnlyWhenSelected() {
        val profile = mapOf("location" to "Ban công", "soil" to "Thoát nước")
        assertEquals(profile, profileToSave(profile, true, SpeciesGroup.PLANT))
        assertEquals(mapOf("location" to "Ban công"), profileToSave(profile, false, SpeciesGroup.PLANT))
    }
}
