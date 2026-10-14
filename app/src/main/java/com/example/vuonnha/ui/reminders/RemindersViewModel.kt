package com.example.vuonnha.ui.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vuonnha.data.CareRepository
import com.example.vuonnha.data.CareTask
import com.example.vuonnha.data.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RemindersViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CareRepository.get(app)
    val items = repo.items.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tasks = repo.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val zones = repo.zones.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val logs = repo.logs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val diary = repo.diary.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val weather = WeatherRepository.state

    fun done(t: CareTask) = viewModelScope.launch { repo.done(t) }
    fun snooze(t: CareTask) = viewModelScope.launch { repo.snooze(t) }

    // Đặt cuối cùng để các thuộc tính ở trên đã khởi tạo xong.
    init {
        WeatherRepository.init(app)
        viewModelScope.launch { WeatherRepository.refresh(app) }
    }
}
