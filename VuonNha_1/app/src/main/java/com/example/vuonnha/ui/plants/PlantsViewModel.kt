package com.example.vuonnha.ui.plants

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vuonnha.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlantsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CareRepository.get(app)
    val items = repo.items.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tasks = repo.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val diary = repo.diary.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val zones = repo.zones.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addItem(name: String, s: Species, cycles: List<Int>, zoneId: Long?) = viewModelScope.launch { repo.addItem(name, s, cycles, zoneId) }
    fun editItem(i: Item, name: String, zoneId: Long?) = viewModelScope.launch { repo.editItem(i, name, zoneId) }
    fun removeItem(id: Long) = viewModelScope.launch { repo.removeItem(id) }
    fun updateCycle(t: CareTask, days: Int) = viewModelScope.launch { repo.updateCycle(t, days) }
    fun addTask(itemId: Long, name: String, days: Int, type: TaskType) = viewModelScope.launch { repo.addTask(itemId, name, days, type) }
    fun removeTask(t: CareTask) = viewModelScope.launch { repo.removeTask(t) }
    fun addZone(name: String, outdoor: Boolean, covered: Boolean, emoji: String) = viewModelScope.launch { repo.addZone(name, outdoor, covered, emoji) }
    fun removeZone(id: Long) = viewModelScope.launch { repo.removeZone(id) }
}
