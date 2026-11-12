package com.example.vuonnha.domain

import com.example.vuonnha.data.CareTask

internal fun shouldSendReminder(lastNotifiedDay: Long, today: Long, force: Boolean): Boolean =
    force || lastNotifiedDay != today

internal fun dueTasksForReminder(tasks: List<CareTask>, existingItemIds: Set<Long>, now: Long): List<CareTask> =
    tasks.asSequence()
        .filter { it.itemId in existingItemIds && it.daysLeft(now) <= 0 }
        .distinctBy { it.id }
        .toList()
