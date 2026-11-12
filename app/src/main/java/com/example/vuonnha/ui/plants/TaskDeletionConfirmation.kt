package com.example.vuonnha.ui.plants

import com.example.vuonnha.data.CareTask

internal fun resolveTaskDeletion(
    task: CareTask?,
    confirmed: Boolean,
    removeTask: (CareTask) -> Unit
) {
    if (confirmed) task?.let(removeTask)
}
