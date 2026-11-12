package com.example.vuonnha.data

import com.example.vuonnha.BuildConfig
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.io.IOException
import java.net.SocketTimeoutException

data class PlantCandidate(val name: String, val scientificName: String, val confidence: String,
    val features: List<String>, val careTips: String, val tasks: List<Pair<String, Int>>, val profile: Map<String, String> = emptyMap())
data class ScanPhoto(val bitmap: Bitmap, val base64: String)

object ScanSettings {
    private val _hidden = MutableStateFlow(false)
    val hiddenState = _hidden.asStateFlow()
    private fun prefs(ctx: Context) = ctx.getSharedPreferences("plant_scan", Context.MODE_PRIVATE)
    fun endpoint(ctx: Context) = BuildConfig.SCAN_ENDPOINT
    fun token(ctx: Context) = BuildConfig.SCAN_TOKEN
    fun hidden(ctx: Context) = prefs(ctx).getBoolean("hidden", false).also { _hidden.value = it }
    fun hide(ctx: Context, value: Boolean) { prefs(ctx).edit().putBoolean("hidden", value).apply(); _hidden.value = value }

}

class PlantScanRepository(private val ctx: Context) {
    suspend fun readPhoto(uri: Uri): ScanPhoto = withContext(Dispatchers.IO) {
        val bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(ctx.contentResolver, uri)) { decoder, info, _ ->
                val scale = maxOf(info.size.width, info.size.height) / 1024f
                if (scale > 1) decoder.setTargetSize((info.size.width / scale).toInt().coerceAtLeast(1), (info.size.height / scale).toInt().coerceAtLeast(1))
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1024) sample *= 2
            ctx.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: error("Không đọc được ảnh")
        }
        val bytes = ByteArrayOutputStream().use { out ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)); out.toByteArray()
        }
        require(bytes.size < 1_400_000) { "Ảnh quá lớn, hãy chọn ảnh khác." }
        ScanPhoto(bitmap, Base64.encodeToString(bytes, Base64.NO_WRAP))
    }

    suspend fun recognize(photo: ScanPhoto): List<PlantCandidate> = withContext(Dispatchers.IO) {
        val endpoint = ScanSettings.endpoint(ctx)
        check(endpoint.isNotBlank()) { "Dịch vụ nhận diện chưa sẵn sàng. Bạn vẫn có thể thêm cây thủ công." }
        val connection = URI(endpoint).toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10000
            connection.readTimeout = 55000
            connection.instanceFollowRedirects = false
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer ${ScanSettings.token(ctx)}")
            val body = JSONObject().put("imageBase64", photo.base64).toString().toByteArray(Charsets.UTF_8)
            connection.setFixedLengthStreamingMode(body.size)
            connection.outputStream.use { it.write(body) }
            check(connection.responseCode == 200) {
                when (connection.responseCode) {
                    401 -> "Dịch vụ nhận diện tạm thời không khả dụng."
                    429 -> "Dịch vụ đang bận. Hãy thử lại sau."
                    503 -> "Dịch vụ nhận diện chưa sẵn sàng."
                    504 -> "Dịch vụ nhận diện phản hồi chậm. Hãy thử lại sau hoặc tự nhập tên cây."
                    else -> "Không quét được ảnh. Kiểm tra kết nối và dịch vụ AI."
                }
            }
            val bytes = connection.inputStream.use { it.readBytesLimited(64000) }
            val candidates = JSONObject(String(bytes, Charsets.UTF_8)).getJSONArray("candidates")
            require(candidates.length() <= 3)
            (0 until candidates.length()).map { i ->
                val c = candidates.getJSONObject(i)
                val features = c.getJSONArray("features")
                val tasks = c.getJSONArray("tasks")
                require(features.length() <= 5 && tasks.length() <= 5)
                PlantCandidate(c.getString("name").also { require(it.isNotBlank() && it.length <= 120) },
                    c.getString("scientificName"), c.getString("confidence"),
                    (0 until features.length()).map { features.getString(it) }, c.getString("careTips"),
                    (0 until tasks.length()).map { n -> tasks.getJSONObject(n).let {
                        it.getString("name").also { value -> require(value.isNotBlank()) } to
                            it.getInt("everyDays").also { value -> require(value in 1..365) }
                    } }, listOf("light", "soil", "humidity", "matureHeight").associateWith { c.optJSONObject("profile")?.optString(it).orEmpty() })
            }
        } catch (e: SocketTimeoutException) {
            error("Nhận diện đang mất nhiều thời gian. Hãy thử lại sau.")
        } catch (e: IOException) {
            error("Không kết nối được dịch vụ nhận diện. Bạn có thể thử lại hoặc tự nhập tên cây.")
        } finally { connection.disconnect() }
    }
}

private fun java.io.InputStream.readBytesLimited(limit: Int): ByteArray {
    val out = ByteArrayOutputStream()
    val buffer = ByteArray(4096)
    while (true) {
        val read = read(buffer)
        if (read < 0) break
        require(out.size() + read <= limit)
        out.write(buffer, 0, read)
    }
    return out.toByteArray()
}
