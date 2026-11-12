package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.PlantCandidate

internal fun candidateForSave(candidate: PlantCandidate?, accepted: Boolean, speciesName: String): PlantCandidate? =
    candidate?.takeIf { accepted && it.name.trim().equals(speciesName.trim(), ignoreCase = true) }

internal fun recognitionDiaryNote(candidate: PlantCandidate?): String = candidate?.let {
    "Gợi ý nhận diện: ${it.name} (${it.scientificName}). ${it.careTips}"
} ?: "Ảnh đầu tiên khi thêm vào vườn."
