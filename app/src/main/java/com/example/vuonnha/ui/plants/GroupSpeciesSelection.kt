package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SPECIES
import com.example.vuonnha.data.Species
import com.example.vuonnha.data.SpeciesGroup

internal fun speciesForGroup(group: SpeciesGroup): List<Species> = SPECIES.filter { it.group == group }

internal fun speciesAfterGroupChange(current: Species, group: SpeciesGroup): Species =
    current.takeIf { it.group == group } ?: speciesForGroup(group).first()
