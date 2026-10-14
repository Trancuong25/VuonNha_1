package com.example.vuonnha.data

import android.content.Context
import com.example.vuonnha.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.util.UUID
import androidx.room.withTransaction

data class ChatMessage(val role: String, val text: String, val at: Long)
class AssistantRepository(private val context: Context) {
    private val fields = listOf("scientificName", "cultivar", "location", "actualLight", "pottingMix", "light", "soil", "humidity", "matureHeight", "actualHeight")
    private fun credential(): String {
        val prefs = context.getSharedPreferences("garden_assistant", Context.MODE_PRIVATE)
        return prefs.getString("credential", null) ?: (UUID.randomUUID().toString() + UUID.randomUUID().toString()).also {
            prefs.edit().putString("credential", it).commit()
        }
    }
    suspend fun messages(): List<ChatMessage> = request("GET")
    suspend fun clear(forgetGarden: Boolean = false) { request("DELETE", path = if (forgetGarden) "/garden" else "/assistant") }
    suspend fun ask(question: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        val dao = AppDb.get(context).dao()
        val garden = AppDb.get(context).withSnapshot(dao, fields)
        request("POST", JSONObject().put("question", question.trim()).put("garden", garden))
    }
    private suspend fun request(method: String, body: JSONObject? = null, path: String = "/assistant"): List<ChatMessage> = withContext(Dispatchers.IO) {
        check(BuildConfig.SCAN_ENDPOINT.isNotBlank()) { "Trợ lý chưa sẵn sàng. Bạn vẫn có thể dùng lịch và nhật ký." }
        val connection = URI(BuildConfig.SCAN_ENDPOINT).resolve(path).toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 8000; connection.readTimeout = 40000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.SCAN_TOKEN}")
            connection.setRequestProperty("X-Garden-Key", credential())
            if (body != null) {
                connection.doOutput = true; connection.setRequestProperty("Content-Type", "application/json")
                val bytes = body.toString().toByteArray(Charsets.UTF_8)
                require(bytes.size <= 512000) { "Vườn có quá nhiều dữ liệu để gửi trong một lần." }
                connection.setFixedLengthStreamingMode(bytes.size)
                connection.outputStream.use { it.write(bytes) }
            }
            check(connection.responseCode == 200) { when (connection.responseCode) {
                429 -> "Trợ lý đang bận hoặc hết hạn mức. Bạn thử lại sau nhé."
                503 -> "Trợ lý AI chưa sẵn sàng. Câu hỏi về việc đã làm vẫn dùng được khi backend kết nối."
                401 -> "Chưa kết nối được Trợ lý. Hãy thử lại sau."
                else -> "Trợ lý chưa trả được câu trả lời. Hãy thử lại sau."
            } }
            val data = JSONObject(String(connection.inputStream.use { it.readBackupBytes(800000) }, Charsets.UTF_8))
            val messages = data.optJSONArray("messages") ?: JSONArray()
            (0 until messages.length()).map { i -> messages.getJSONObject(i).let { ChatMessage(it.getString("role"), it.getString("text"), it.getLong("at")) } }
        } catch (e: java.io.IOException) { error("Không kết nối được Trợ lý. Vườn và lịch chăm sóc vẫn lưu trên máy.") }
        finally { connection.disconnect() }
    }
}
private suspend fun AppDb.withSnapshot(dao: CareDao, fields: List<String>): JSONObject = withTransaction {
    val items = dao.itemsOnce(); val tasks = dao.tasksOnce(); val zones = dao.zonesOnce().associateBy { it.id }; val logs = dao.logsOnce()
    require(items.size <= 200 && logs.size <= 5000) { "Vườn quá lớn để gửi đầy đủ cho Trợ lý ở bản này." }
    val plants = JSONArray()
    items.forEach { item ->
        val profile = runCatching { JSONObject(item.profileJson) }.getOrDefault(JSONObject())
        val selected = JSONObject(); fields.forEach { key -> if (profile.has(key)) selected.put(key, profile.optString(key).take(1000)) }
        val plans = JSONArray(); tasks.filter { it.itemId == item.id }.forEach { plans.put(JSONObject().put("name", it.name).put("everyDays", it.everyDays).put("lastDone", it.lastDone)) }
        plants.put(JSONObject().put("id", item.id).put("name", item.name).put("species", findSpecies(item.speciesId)?.name.orEmpty())
            .put("location", zones[item.zoneId]?.name.orEmpty()).put("profile", selected).put("tasks", plans))
    }
    val history = JSONArray(); logs.forEach { history.put(JSONObject().put("itemId", it.itemId).put("task", it.task).put("at", it.at)) }
    val zone = java.time.ZoneId.systemDefault().id
    val timeZone = if (zone.startsWith("GMT+") || zone.startsWith("GMT-") || zone.startsWith("UTC+") || zone.startsWith("UTC-")) zone.substring(3) else zone
    JSONObject().put("plants", plants).put("logs", history).put("timeZone", timeZone)
}
