package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.PlantCandidate
import org.junit.Assert.*
import org.junit.Test

class ScanAttributionTest {
    private val candidate = PlantCandidate("Kim tiền", "Zamioculcas zamiifolia", "medium", emptyList(), "Kiểm tra đất", emptyList())

    @Test fun discardedSuggestionHasNoCandidateAndNoAiDiaryAttribution() {
        val saved = candidateForSave(candidate, false, "Kim tiền")
        assertNull(saved)
        assertEquals("Ảnh đầu tiên khi thêm vào vườn.", recognitionDiaryNote(saved))
    }

    @Test fun changedSpeciesCannotKeepOldRecognition() {
        assertNull(candidateForSave(candidate, true, "Cây táo"))
    }

    @Test fun acceptedSuggestionKeepsAttributionAndManualHasNone() {
        assertEquals(candidate, candidateForSave(candidate, true, " kim TIỀN "))
        assertTrue(recognitionDiaryNote(candidate).contains("Zamioculcas zamiifolia"))
        assertNull(candidateForSave(null, false, "Cây táo"))
    }
}
