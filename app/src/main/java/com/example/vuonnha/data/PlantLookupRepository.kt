package com.example.vuonnha.data

import com.example.vuonnha.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URI
import java.net.HttpURLConnection
import java.net.URLEncoder

data class PlantProfile(val name: String, val fields: Map<String,String>, val tasks: List<Pair<String,Int>>)
class PlantLookupRepository {
    suspend fun search(query: String): List<PlantProfile> = withContext(Dispatchers.IO) {
        require(query.trim().length in 2..120)
        check(BuildConfig.SCAN_ENDPOINT.isNotBlank()) { "Dịch vụ tra cứu chưa được cấu hình." }
        val url = URI(BuildConfig.SCAN_ENDPOINT).resolve("/plants?q=${URLEncoder.encode(query.trim(), "UTF-8")}").toURL()
        val conn=url.openConnection() as HttpURLConnection
        try {
            conn.connectTimeout=8000; conn.readTimeout=35000
            if (BuildConfig.SCAN_TOKEN.isNotBlank()) conn.setRequestProperty("Authorization", "Bearer ${BuildConfig.SCAN_TOKEN}")
            check(conn.responseCode==200) { when(conn.responseCode) {
                429 -> "API đang hết hạn mức hoặc bận. Hãy thử lại sau; bạn vẫn có thể tự nhập."
                503 -> "Backend chưa cấu hình dịch vụ tra cứu AI."
                401 -> "Ứng dụng chưa được backend cho phép tra cứu."
                else -> "Dịch vụ AI chưa trả được kết quả. Hãy thử lại sau."
            } }
            val bytes=conn.inputStream.use { it.readBackupBytes(64000) }
            val array=JSONObject(String(bytes, Charsets.UTF_8)).getJSONArray("plants")
            require(array.length()<=6)
            (0 until array.length()).map { i ->
                val p=array.getJSONObject(i); val tasks=p.getJSONArray("tasks")
                PlantProfile(p.getString("name"), listOf("scientificName","matureHeight","humidity","soil","light","source").associateWith { p.optString(it) },
                    (0 until tasks.length()).map { n -> tasks.getJSONObject(n).let { it.getString("name") to it.getInt("everyDays").also { d -> require(d in 1..365) } } })
            }
        } catch(e: java.io.IOException) { error("Không kết nối được danh mục cây. Bạn vẫn có thể tự nhập.") }
        finally { conn.disconnect() }
    }
}
