package com.example.vuonnha.ui.plants

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.example.vuonnha.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class PlantsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CareRepository.get(app)
    val items = repo.items.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tasks = repo.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val diary = repo.diary.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val zones = repo.zones.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val logs = repo.logs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var adding by mutableStateOf(false)
        private set
    var addError by mutableStateOf<String?>(null)
        private set
    fun clearAddError() { addError = null }
    var editing by mutableStateOf(false)
        private set
    var editError by mutableStateOf<String?>(null)
        private set
    fun clearEditError() { editError = null }
    fun saveEdit(i: Item, name: String, zoneId: Long?, speciesId: String, profileJson: String, cycles: Map<Long, Int>, onSaved: () -> Unit) {
        if (editing) return
        editing = true; editError = null
        viewModelScope.launch {
            try { repo.saveItemEdit(i.id, name, zoneId, speciesId, profileJson, cycles); onSaved() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { editError = "Chưa lưu được thay đổi. Nội dung vẫn được giữ để bạn thử lại." }
            finally { editing = false }
        }
    }
    fun addItem(name: String, s: Species, cycles: List<Int>, zoneId: Long?, starts: List<Long>, onSaved: () -> Unit = {}) {
        if (adding) return
        adding = true; addError = null
        viewModelScope.launch {
            try { repo.addItem(name, s, cycles, zoneId, starts); onSaved() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { addError = "Chưa lưu được. Thông tin vẫn được giữ; hãy thử lại." }
            finally { adding = false }
        }
    }
    var operationError by mutableStateOf<String?>(null)
        private set
    fun clearOperationError() { operationError = null }
    private fun guarded(message: String, block: suspend () -> Unit) = viewModelScope.launch {
        guardedOperation(message, block) { operationError = it }
    }
    fun editItem(i: Item, name: String, zoneId: Long?, speciesId: String = i.speciesId, profileJson: String = i.profileJson) = guarded("Chưa sửa được thông tin. Hãy thử lại.") { repo.editItem(i, name, zoneId, speciesId, profileJson) }
    fun removeItem(id: Long) = guarded("Chưa xóa được. Hãy thử lại.") { repo.removeItem(id) }
    fun updateCycle(t: CareTask, days: Int) = guarded("Chưa đổi được chu kỳ. Hãy thử lại.") { repo.updateCycle(t, days) }
    fun addTask(itemId: Long, name: String, days: Int, type: TaskType) = guarded("Chưa thêm được việc. Hãy thử lại.") { repo.addTask(itemId, name, days, type) }
    fun removeTask(t: CareTask) = guarded("Chưa xóa được việc. Hãy thử lại.") { repo.removeTask(t) }
    fun done(t: CareTask) = guarded("Chưa ghi nhận được việc đã làm. Hãy thử lại.") { repo.done(t) }
    fun snooze(t: CareTask) = guarded("Chưa hoãn được việc. Hãy thử lại.") { repo.snooze(t) }
    fun addZone(name: String, outdoor: Boolean, covered: Boolean, emoji: String) = guarded("Chưa thêm được khu vực. Hãy thử lại.") { repo.addZone(name, outdoor, covered, emoji) }
    fun removeZone(id: Long) = guarded("Chưa xóa được khu vực. Hãy thử lại.") { repo.removeZone(id) }
}
