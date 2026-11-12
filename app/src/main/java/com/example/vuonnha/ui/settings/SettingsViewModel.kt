package com.example.vuonnha.ui.settings

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vuonnha.data.CareRepository
import com.example.vuonnha.notify.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CareRepository.get(app)
    var message by mutableStateOf("")
        private set
    var reminderMinutes by mutableIntStateOf(Notifications.getMinutes(app))
        private set

    fun shiftReminder(delta: Int) {
        reminderMinutes = (reminderMinutes + delta + 1440) % 1440
        Notifications.setMinutes(getApplication(), reminderMinutes)
    }
    fun setReminder(minutes: Int) {
        reminderMinutes = minutes.coerceIn(0, 1439)
        Notifications.setMinutes(getApplication(), reminderMinutes)
    }

    fun backup(uri: Uri) = viewModelScope.launch {
        message = try {
            val photos = withContext(Dispatchers.IO) {
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { repo.exportZip(it) }
            } ?: throw IllegalStateException("Không mở được file")
            "Đã xuất dữ liệu và $photos ảnh ra file ZIP."
        } catch (e: Exception) { "Xuất thất bại: ${e.message}" }
    }

    fun restore(uri: Uri) = viewModelScope.launch {
        message = try {
            val count = withContext(Dispatchers.IO) {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { repo.importBackup(it) }
            } ?: throw IllegalStateException("Không mở được file")
            "Đã nhập $count cây / vật nuôi."
        } catch (e: Exception) { "Nhập thất bại: file không hợp lệ." }
    }

    fun testNotification() = Notifications.test(getApplication())
}
