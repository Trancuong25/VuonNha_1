package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.Species

internal fun speciesWithReviewedTasks(species: Species, plans: List<Pair<String, Int>>): Species = species.copy(tasks = plans)
