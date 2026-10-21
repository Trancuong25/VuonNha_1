package com.example.vuonnha.ui.plants

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vuonnha.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class PlantScanState(val photo: ScanPhoto? = null, val loading: Boolean = false,
    val candidates: List<PlantCandidate> = emptyList(), val scanned: Boolean = false, val error: String? = null,
    val saving: Boolean = false, val saved: Boolean = false, val saveError: String? = null)

class PlantScanViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PlantScanRepository(app)
    private val _state = MutableStateFlow(PlantScanState())
    val state = _state.asStateFlow()
    private var job: Job? = null

    fun select(uri: Uri, identify: Boolean = false) {
        job?.cancel()
        _state.value = PlantScanState(loading = true)
        job = viewModelScope.launch {
            try {
                val photo = repo.readPhoto(uri)
                _state.value = PlantScanState(photo = photo, loading = identify)
                if (identify) recognize(photo)
            }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.value = PlantScanState(error = "Không đọc được ảnh. Hãy chọn ảnh khác.") }
            finally {
                if (uri.authority == "${getApplication<Application>().packageName}.fileprovider") {
                    uri.lastPathSegment?.takeIf { it.startsWith("scan_") && !it.contains('/') }?.let {
                        File(File(getApplication<Application>().cacheDir, "camera"), it).delete()
                    }
                }
                if (uri.scheme == "file") {
                    uri.path?.let { path ->
                        val file = File(path)
                        val folder = File(getApplication<Application>().cacheDir, "camera")
                        if (file.parentFile?.canonicalPath == folder.canonicalPath && file.name.startsWith("scan_")) file.delete()
                    }
                }
            }
        }
    }
    fun scan() {
        val photo = _state.value.photo ?: return
        if (_state.value.loading) return
        _state.value = PlantScanState(photo = photo, loading = true)
        job = viewModelScope.launch {
            try { recognize(photo) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.value = PlantScanState(photo, error = e.message ?: "Không kết nối được dịch vụ AI.") }
        }
    }
    private suspend fun recognize(photo: ScanPhoto) {
        try { _state.value = PlantScanState(photo, candidates = repo.recognize(photo), scanned = true) }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { _state.value = PlantScanState(photo, error = e.message ?: "Không kết nối được dịch vụ nhận diện.") }
    }

    fun savePlant(name: String, species: Species, cycles: List<Int>, zone: Long?, starts: List<Long>, candidate: PlantCandidate?) {
        if (_state.value.saving) return
        val photo = _state.value.photo
        _state.value = _state.value.copy(saving = true, saveError = null)
        viewModelScope.launch {
            try {
                val care = CareRepository.get(getApplication())
                if (photo != null) {
                    val note = candidate?.let { "Gợi ý nhận diện: ${it.name} (${it.scientificName}). ${it.careTips}" }
                        ?: "Ảnh đầu tiên khi thêm vào vườn."
                    care.addScannedItem(name, species, cycles, zone, starts, photo.bitmap, note)
                } else care.addItem(name, species, cycles, zone, starts)
                _state.value = _state.value.copy(saving = false, saved = true)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.value = _state.value.copy(saving = false,
                saveError = "Chưa lưu được cây. Thông tin vẫn được giữ; hãy thử lại.") }
        }
    }

    fun retake() { job?.cancel(); _state.value = PlantScanState() }
    fun dismissSuggestion() { _state.value = _state.value.copy(candidates = emptyList(), scanned = false, error = null) }
    fun cancel() { job?.cancel(); _state.value = _state.value.copy(loading = false) }
}
