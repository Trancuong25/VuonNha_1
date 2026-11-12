package com.example.vuonnha.ui.weather

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vuonnha.data.CareRepository
import com.example.vuonnha.data.Place
import com.example.vuonnha.data.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeatherViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CareRepository.get(app)
    val weather = WeatherRepository.state
    val place = WeatherRepository.place
    val items = repo.items.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val zones = repo.zones.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var loading by mutableStateOf(false)
        private set
    var searching by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var results by mutableStateOf<List<Place>>(emptyList())
        private set

    fun fail(msg: String) { error = msg }

    fun refresh(force: Boolean = false) = viewModelScope.launch {
        loading = true
        val ok = WeatherRepository.refresh(getApplication(), force)
        error = if (!ok && place.value != null) "Không có mạng, đang hiện dữ liệu đã lưu." else null
        loading = false
    }

    fun search(q: String) = viewModelScope.launch {
        if (q.isBlank()) return@launch
        error = null; searching = true
        results = WeatherRepository.searchCity(q.trim())
        if (results.isEmpty()) error = "Không tìm thấy hoặc không có mạng."
        searching = false
    }

    fun choose(p: Place) {
        WeatherRepository.setPlace(getApplication(), p)
        results = emptyList()
        refresh(true)
    }

    fun useCurrent(): Boolean {
        val p = WeatherRepository.currentLocation(getApplication())
        if (p == null) { error = "Chưa lấy được vị trí. Hãy tìm thành phố."; return false }
        choose(p)
        return true
    }

    // Đặt cuối cùng để các thuộc tính ở trên đã khởi tạo xong.
    init {
        WeatherRepository.init(app)
        refresh()
    }
}
