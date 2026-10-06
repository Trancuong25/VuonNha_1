package com.example.vuonnha.ui.journal

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vuonnha.data.CareRepository
import com.example.vuonnha.data.DiaryEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// MEMBER 3
class JournalViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CareRepository.get(app)
    val items = repo.items.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val diary = repo.diary.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val logs = repo.logs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEntry(itemId: Long, note: String, uri: Uri?) = viewModelScope.launch { repo.addEntry(itemId, note, uri) }
    fun removeEntry(e: DiaryEntry) = viewModelScope.launch { repo.removeEntry(e) }
}