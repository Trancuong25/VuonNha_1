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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// MEMBER 3
class JournalViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CareRepository.get(app)
    val items = repo.items.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val diary = repo.diary.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val logs = repo.logs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saving = MutableStateFlow(false)
    val saving = _saving.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun addEntry(itemId: Long, note: String, uri: Uri?, onSaved: () -> Unit) {
        if (_saving.value || (note.isBlank() && uri == null)) return
        _saving.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                repo.addEntry(itemId, note, uri)
                onSaved()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = "Không lưu được nhật ký. Kiểm tra ảnh và dung lượng rồi thử lại."
            } finally { _saving.value = false }
        }
    }
    fun removeEntry(e: DiaryEntry) = viewModelScope.launch { repo.removeEntry(e) }
}
