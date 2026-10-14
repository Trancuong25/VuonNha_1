package com.example.vuonnha.ui.plants

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vuonnha.data.PlantCandidate
import com.example.vuonnha.data.findSpecies
import java.io.File
import kotlinx.coroutines.delay

@Composable
fun PlantScanScreen(onBack: () -> Unit, onOpenExisting: (Long) -> Unit = {},
    vm: PlantScanViewModel = viewModel(), plants: PlantsViewModel = viewModel()) {
    val ctx = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val state by vm.state.collectAsStateWithLifecycle()
    val zones by plants.zones.collectAsStateWithLifecycle()
    val garden by plants.items.collectAsStateWithLifecycle()
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var asked by rememberSaveable { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val draft = selectedIndex?.let { state.candidates.getOrNull(it) }
    var manual by rememberSaveable { mutableStateOf(false) }
    var enteringDetails by rememberSaveable { mutableStateOf(false) }
    var reviewed by rememberSaveable { mutableStateOf(false) }
    var capture by remember { mutableStateOf<ImageCapture?>(null) }
    var capturing by remember { mutableStateOf(false) }
    var stopCamera by remember { mutableStateOf<CameraStop?>(null) }
    var closingCamera by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var disposed by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { reviewed = false; cameraError = null; vm.select(uri) }
    }
    LaunchedEffect(Unit) { if (!granted && !asked) { asked = true; permission.launch(Manifest.permission.CAMERA) } }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) {
            granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        } }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { disposed = true; lifecycle.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(state.scanned, state.candidates) {
        if (state.scanned && state.candidates.isNotEmpty() && !reviewed) { selectedIndex = 0; reviewed = true }
    }
    LaunchedEffect(state.saved) { if (state.saved) onBack() }
    LaunchedEffect(closingCamera) {
        if (closingCamera) {
            delay(10000)
            closingCamera = false
            cameraError = "Máy ảnh chưa đóng xong. Hãy thử lại hoặc quay về vườn."
        }
    }
    BackHandler {
        if (!state.saving) {
            if (enteringDetails || manual) { enteringDetails = false; manual = false } else if (draft != null) { selectedIndex = null }
            else { vm.cancel(); onBack() }
        }
    }
    if (enteringDetails || manual) {
        AddSheet(zones, { enteringDetails = false; manual = false }, initialCandidate = if (manual) null else draft,
            saving = state.saving, saveError = state.saveError, fullScreen = true) { name, species, cycles, zone, starts ->
            vm.savePlant(name, species, cycles, zone, starts, if (manual) null else draft)
        }
        return
    }
    if (draft != null) {
        PlantRecognitionResult(state.candidates, requireNotNull(draft), state.photo?.bitmap,
            onSelect = { selectedIndex = state.candidates.indexOf(it) }, onNext = { enteringDetails = true },
            onRetake = { selectedIndex = null; reviewed = false; cameraError = null; vm.retake() },
            onBack = { selectedIndex = null },
            existing = garden.filter { findSpecies(it.speciesId)?.name?.equals(draft?.name, ignoreCase = true) == true }
                .take(3).map { it.id to it.name }, onOpenExisting = onOpenExisting)
        return
    }
    val busy = state.loading || state.saving || capturing || closingCamera
    val white = Color.White
    Column(Modifier.fillMaxSize().background(Color(0xFF101C17)).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton({ if (!state.saving) { vm.cancel(); onBack() } }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Quay lại vườn", tint = white) }
            Column {
                Text("Quét cây", color = white, style = MaterialTheme.typography.headlineSmall)
                Text(if (state.photo == null) "Chụp rõ lá hoặc hoa của một cây" else "Ảnh đã sẵn sàng để nhận diện", color = Color(0xFFC9D8D0), style = MaterialTheme.typography.bodySmall)
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF203129))) {
            val photo = state.photo
            when {
                photo != null -> Image(photo.bitmap.asImageBitmap(), "Ảnh cây để nhận diện", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                granted && !manual && draft == null -> LivePlantCamera(Modifier.fillMaxSize(), { capture = it }, { cameraError = it }, { stopCamera = it })
                granted -> Unit
                else -> Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.CameraAlt, null, Modifier.size(44.dp), tint = white)
                    Text("Cho phép dùng máy ảnh", color = white, style = MaterialTheme.typography.titleMedium)
                    Text("Để chụp cây ngay trong ứng dụng. Bạn cũng có thể chọn ảnh có sẵn.", color = Color(0xFFC9D8D0))
                    Button({ permission.launch(Manifest.permission.CAMERA) }) { Text("Cho phép máy ảnh") }
                    TextButton({ ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))) }) {
                        Text("Mở quyền trong cài đặt", color = white)
                    }
                }
            }
            if (photo == null && granted) {
                Box(Modifier.align(Alignment.Center).fillMaxWidth(0.78f).aspectRatio(0.85f)
                    .border(1.dp, white.copy(alpha = 0.7f), RoundedCornerShape(24.dp)))
                Surface(Modifier.align(Alignment.BottomCenter).padding(16.dp), color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(16.dp)) {
                    Text("Một cây trong khung · đủ sáng · rõ lá hoặc hoa", Modifier.padding(14.dp), color = white, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (state.loading || capturing || closingCamera) {
                Column(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f)),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = Color(0xFF9BD4B5))
                    Text(if (closingCamera) "Đang đóng máy ảnh…" else if (capturing) "Đang chụp…" else "Đang nhận diện cây…", Modifier.padding(16.dp), color = white)
                }
            }
        }
        Column(Modifier.padding(20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            (state.error ?: cameraError)?.let { Text(it, color = Color(0xFFFFCCC7), style = MaterialTheme.typography.bodySmall) }
            if (state.scanned && state.candidates.isEmpty()) Text("Chưa nhận diện được. Chụp rõ lá hoặc hoa, hoặc tự nhập tên cây.", color = white)
            if (state.scanned && state.candidates.isNotEmpty()) {
                Button({ selectedIndex = 0 }, Modifier.fillMaxWidth(), enabled = !busy) { Text("Xem thông tin cây") }
            } else {
                Button(onClick = {
                    if (state.photo != null) vm.scan()
                    else {
                        val camera = capture ?: return@Button
                        capturing = true; cameraError = null; reviewed = false
                        try {
                            val file = File.createTempFile("scan_", ".jpg", File(ctx.cacheDir, "camera").apply { mkdirs() })
                            camera.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(ctx),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        if (disposed) { file.delete(); return }
                                        capturing = false; vm.select(Uri.fromFile(file), identify = true)
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        file.delete()
                                        if (!disposed) { capturing = false; cameraError = "Chưa chụp được ảnh. Hãy thử lại hoặc chọn ảnh có sẵn." }
                                    }
                                })
                        } catch (e: Exception) { capturing = false; cameraError = "Không mở được máy ảnh. Hãy chọn ảnh có sẵn." }
                    }
                }, Modifier.fillMaxWidth().height(54.dp), enabled = !busy && (state.photo != null || (granted && capture != null)),
                    shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Outlined.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text(if (state.photo == null) "Chụp & nhận diện" else "Nhận diện cây")
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (state.photo != null) TextButton({ reviewed = false; selectedIndex = null; cameraError = null; vm.retake() }, enabled = !busy) { Text("Chụp lại", color = white) }
                else TextButton({ picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, enabled = !busy) {
                    Icon(Icons.Outlined.PhotoLibrary, null, tint = white); Spacer(Modifier.width(6.dp)); Text("Chọn ảnh", color = white)
                }
                TextButton({
                    val stop = stopCamera
                    if (stop == null) manual = true
                    else {
                        closingCamera = true
                        stop { if (!disposed) { closingCamera = false; manual = true } }
                    }
                }, enabled = !busy) { Text("Tự nhập tên cây", color = white) }
                if (state.loading) TextButton(vm::cancel) { Text("Huỷ", color = white) }
            }
            Text("Ảnh được gửi để nhận diện khi bạn bấm nút. Gợi ý có thể nhầm; bạn kiểm tra trước khi lưu.",
                color = Color(0xFFB5C6BB), style = MaterialTheme.typography.bodySmall)
        }
    }
}
