package com.example.vuonnha.ui.plants

internal fun cyclesForExistingTasks(cycles: Map<Long, Int>, taskIds: Set<Long>): Map<Long, Int> = cycles.filterKeys { it in taskIds }
