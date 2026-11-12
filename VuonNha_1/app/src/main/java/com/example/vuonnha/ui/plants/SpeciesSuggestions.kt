package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.Species
import com.example.vuonnha.data.SpeciesGroup
import com.example.vuonnha.data.SPECIES
import java.text.Normalizer
import java.util.Locale

private fun searchable(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "")
    .replace('đ', 'd').replace('Đ', 'D')
    .lowercase(Locale.ROOT).trim()

internal fun speciesSuggestions(group: SpeciesGroup, query: String, limit: Int = 4): List<Species> {
    if (limit <= 0) return emptyList()
    val choices = SPECIES.filter { it.group == group }
    val needle = searchable(query)
    if (needle.isEmpty()) return choices.take(limit)
    return choices.mapIndexedNotNull { index, species ->
        val name = searchable(species.name)
        if (needle !in name) null else Triple(species, if (name.startsWith(needle)) 0 else 1, index)
    }.sortedWith(compareBy<Triple<Species, Int, Int>> { it.second }.thenBy { it.third })
        .take(limit).map { it.first }
}

internal data class ResolvedSpeciesInput(val custom: Boolean, val name: String, val species: Species)

internal fun resolveSpeciesInput(current: Species, typedName: String, selected: Species?): ResolvedSpeciesInput =
    if (selected == null) ResolvedSpeciesInput(true, typedName, current)
    else ResolvedSpeciesInput(false, selected.name, selected)
