package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.SpeciesGroup

internal val REFERENCE_KEYS = setOf("scientificName", "cultivar", "actualHeight", "matureHeight", "humidity", "soil", "light", "source")

internal fun profileToSave(profile: Map<String, String>, keepExtra: Boolean, group: SpeciesGroup): Map<String, String> =
    if (group == SpeciesGroup.PLANT) {
        if (keepExtra) profile else profile.filterKeys { it !in REFERENCE_KEYS }
    } else {
        val allowed = groupFields(group).mapTo(hashSetOf()) { it.key }
        profile.filterKeys { it in allowed }
    }

internal fun profileIsValidForSave(profile: Map<String, String>, keepExtra: Boolean, group: SpeciesGroup): Boolean =
    profileToSave(profile, keepExtra, group).values.all { it.length <= 1000 }

internal fun hasOversizedProfileValue(profile: Map<String, String>): Boolean =
    profile.values.any { it.length > 1000 }
