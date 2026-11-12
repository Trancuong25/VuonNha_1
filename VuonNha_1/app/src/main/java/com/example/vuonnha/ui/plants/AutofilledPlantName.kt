package com.example.vuonnha.ui.plants

internal data class AutofilledPlantName(
    val name: String,
    val autofilledName: String
)

internal fun updateAutofilledPlantName(
    currentName: String,
    autofilledName: String,
    newSpeciesName: String
): AutofilledPlantName {
    val normalizedName = currentName.trim()
    val normalizedAutofilledName = autofilledName.trim()
    val normalizedSpeciesName = newSpeciesName.trim()
    return if (normalizedName.isEmpty() || normalizedName == normalizedAutofilledName) {
        AutofilledPlantName(normalizedSpeciesName, normalizedSpeciesName)
    } else {
        AutofilledPlantName(normalizedName, normalizedAutofilledName)
    }
}

internal fun effectivePlantName(
    name: String,
    nameAutofilled: String,
    currentSpeciesName: String
): String {
    val normalizedName = name.trim()
    val normalizedAutofilledName = nameAutofilled.trim()
    val normalizedSpeciesName = currentSpeciesName.trim()
    return if (normalizedName.isEmpty() || normalizedName == normalizedAutofilledName) {
        normalizedSpeciesName
    } else {
        normalizedName
    }
}

internal fun plantNameSaveHint(name: String, nameAutofilled: String, currentSpeciesName: String): String? {
    val normalizedName = name.trim()
    val normalizedAutofilledName = nameAutofilled.trim()
    val followsSpecies = normalizedName.isEmpty() || normalizedName == normalizedAutofilledName
    val finalName = effectivePlantName(normalizedName, normalizedAutofilledName, currentSpeciesName)
    return if (followsSpecies && finalName != normalizedName) "Sẽ lưu là: $finalName" else null
}
