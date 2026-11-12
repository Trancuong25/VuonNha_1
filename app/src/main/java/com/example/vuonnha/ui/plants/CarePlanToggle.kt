package com.example.vuonnha.ui.plants

import java.time.LocalDate

internal data class CarePlanOption(val name: String, val days: Int, val date: LocalDate = LocalDate.now())

internal data class CarePlanToggleResult(
    val active: List<CarePlanOption>,
    val inactive: List<CarePlanOption>
)

internal fun toggleCarePlan(
    active: List<CarePlanOption>,
    inactive: List<CarePlanOption>,
    option: CarePlanOption,
    enabled: Boolean
): CarePlanToggleResult = if (enabled) {
    val restored = inactive.firstOrNull { it == option } ?: option
    CarePlanToggleResult(
        if (active.contains(restored)) active else active + restored,
        inactive.toMutableList().also { it.remove(restored) }
    )
} else {
    val selected = active.firstOrNull { it == option }
    CarePlanToggleResult(active.toMutableList().also { if (selected != null) it.remove(selected) },
        if (selected != null) inactive + selected else inactive)
}
