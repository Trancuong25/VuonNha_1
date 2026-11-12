package com.example.vuonnha.ui.plants

import android.util.Size
import android.util.Range

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.CameraState
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Observer

internal typealias CameraStop = (() -> Unit) -> Unit

@Composable
internal fun LivePlantCamera(modifier: Modifier, onReady: (ImageCapture?) -> Unit, onError: (String) -> Unit,
    onStopReady: (CameraStop?) -> Unit = {}) {
    val ctx = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(ctx).apply {
        implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        scaleType = PreviewView.ScaleType.FILL_CENTER
    } }
    val latestReady by rememberUpdatedState(onReady)
    val latestError by rememberUpdatedState(onError)
    val latestStop by rememberUpdatedState(onStopReady)
    AndroidView(factory = { previewView }, modifier = modifier)
    DisposableEffect(lifecycle, previewView) {
        val future = ProcessCameraProvider.getInstance(ctx)
        val previewResolution = ResolutionSelector.Builder().setResolutionStrategy(
            ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)).build()
        val photoResolution = ResolutionSelector.Builder().setResolutionStrategy(
            ResolutionStrategy(Size(1280, 960), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)).build()
        val preview = Preview.Builder().setResolutionSelector(previewResolution).setTargetFrameRate(Range(15, 15))
            .build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        val capture = ImageCapture.Builder().setResolutionSelector(photoResolution)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
        var provider: ProcessCameraProvider? = null
        var boundCamera: androidx.camera.core.Camera? = null
        var closeObserver: Observer<CameraState>? = null
        var disposed = false
        future.addListener({
            if (!disposed) try {
                provider = future.get()
                previewView.display?.rotation?.let { rotation ->
                    preview.targetRotation = rotation
                    capture.targetRotation = rotation
                }
                val camera = provider!!.bindToLifecycle(lifecycle, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                boundCamera = camera
                latestStop { onClosed ->
                    latestReady(null)
                    closeObserver?.let { camera.cameraInfo.cameraState.removeObserver(it) }
                    val observer = object : Observer<CameraState> {
                        override fun onChanged(value: CameraState) {
                            if (value.type == CameraState.Type.CLOSED) {
                                camera.cameraInfo.cameraState.removeObserver(this)
                                closeObserver = null
                                if (!disposed) onClosed()
                            }
                        }
                    }
                    closeObserver = observer
                    provider?.unbind(preview, capture)
                    camera.cameraInfo.cameraState.observe(lifecycle, observer)
                }
                latestReady(capture)
            } catch (e: Exception) { latestError("Máy ảnh chưa sẵn sàng. Bạn có thể chọn ảnh có sẵn.") }
        }, ContextCompat.getMainExecutor(ctx))
        onDispose {
            disposed = true; latestStop(null); latestReady(null)
            closeObserver?.let { observer -> boundCamera?.cameraInfo?.cameraState?.removeObserver(observer) }
            provider?.unbind(preview, capture)
        }
    }
}
