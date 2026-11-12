package com.example.vuonnha.ui.plants

import org.junit.Assert.assertEquals
import org.junit.Test

class AutofilledPlantNameTest {
    @Test fun blankNameFollowsNewSpecies() {
        assertEquals(
            AutofilledPlantName("Trầu bà", "Trầu bà"),
            updateAutofilledPlantName("", "", "Trầu bà")
        )
    }

    @Test fun nameEqualToPreviousAutofillFollowsNewSpecies() {
        assertEquals(
            AutofilledPlantName("Kim tiền", "Kim tiền"),
            updateAutofilledPlantName("Trầu bà", "Trầu bà", "Kim tiền")
        )
    }

    @Test fun userEditedNameIsKept() {
        assertEquals(
            AutofilledPlantName("Góc cửa sổ", "Trầu bà"),
            updateAutofilledPlantName("Góc cửa sổ", "Trầu bà", "Kim tiền")
        )
    }

    @Test fun autofilledNameTracksMultipleSpeciesChanges() {
        val first = updateAutofilledPlantName("", "", "Trầu bà")
        val second = updateAutofilledPlantName(first.name, first.autofilledName, "Kim tiền")
        val third = updateAutofilledPlantName(second.name, second.autofilledName, "Lan hồ điệp")

        assertEquals(AutofilledPlantName("Lan hồ điệp", "Lan hồ điệp"), third)
    }

    @Test fun manuallyTypedSpeciesAfterLookupBecomesFinalName() {
        assertEquals("Kim tiền", effectivePlantName("Trầu bà", "Trầu bà", "Kim tiền"))
    }

    @Test fun editedSpeciesAfterPhotoScanBecomesFinalName() {
        assertEquals("Lan hồ điệp", effectivePlantName("Trầu bà", "Trầu bà", "Lan hồ điệp"))
    }

    @Test fun userChosenGardenNameIsKept() {
        assertEquals("Góc cửa sổ", effectivePlantName("Góc cửa sổ", "Trầu bà", "Kim tiền"))
    }

    @Test fun blankNameUsesCurrentSpecies() {
        assertEquals("Kim tiền", effectivePlantName("  ", "Trầu bà", "Kim tiền"))
    }

    @Test fun aiNameWithSurroundingSpacesFollowsSpecies() {
        assertEquals("Kim tiền", effectivePlantName("  Trầu bà ", "Trầu bà ", "Kim tiền"))
        assertEquals(
            AutofilledPlantName("Kim tiền", "Kim tiền"),
            updateAutofilledPlantName(" Trầu bà ", "Trầu bà  ", " Kim tiền ")
        )
    }

    @Test fun userChosenNameWithAiNameWhitespaceIsKept() {
        assertEquals("Góc cửa sổ", effectivePlantName("Góc cửa sổ", " Trầu bà ", "Kim tiền"))
        assertEquals(
            AutofilledPlantName("Góc cửa sổ", "Trầu bà"),
            updateAutofilledPlantName(" Góc cửa sổ ", " Trầu bà ", "Kim tiền")
        )
    }

    @Test fun nameSaveHintOnlyAppearsWhenDisplayedNameWouldDiffer() {
        assertEquals("Sẽ lưu là: Kim tiền", plantNameSaveHint(" Trầu bà ", "Trầu bà", "Kim tiền"))
        assertEquals("Sẽ lưu là: Kim tiền", plantNameSaveHint("", "", "Kim tiền"))
        assertEquals(null, plantNameSaveHint("Góc cửa sổ", "Trầu bà", "Kim tiền"))
        assertEquals(null, plantNameSaveHint("Kim tiền", "Kim tiền", "Kim tiền"))
    }
}
