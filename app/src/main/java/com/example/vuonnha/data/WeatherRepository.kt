package com.example.vuonnha.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class Place(val name: String, val lat: Double, val lon: Double)
data class DayForecast(val date: String, val code: Int, val tMax: Double, val tMin: Double, val rainMm: Double, val rainProb: Int)
data class WeatherData(val place: Place, val fetchedAt: Long, val temp: Double, val humidity: Int, val code: Int, val days: List<DayForecast>)

// Ngưỡng gợi ý (chỉnh được): mưa đáng kể hoặc xác suất cao; nắng nóng từ 35°C.
fun DayForecast.isRainy() = rainMm >= 5.0 || (rainProb >= 70 && rainMm >= 1.0)
fun DayForecast.isHot() = tMax >= 35.0

fun weatherEmoji(code: Int) = when (code) {
    0 -> "☀️"; 1, 2 -> "🌤️"; 3 -> "☁️"; 45, 48 -> "🌫️"
    in 51..57 -> "🌦️"; in 61..67 -> "🌧️"; in 71..77, 85, 86 -> "❄️"; in 80..82 -> "🌧️"; in 95..99 -> "⛈️"
    else -> "🌡️"
}
fun weatherText(code: Int) = when (code) {
    0 -> "Trời quang"; 1, 2 -> "Ít mây"; 3 -> "Nhiều mây"; 45, 48 -> "Sương mù"
    in 51..57 -> "Mưa phùn"; in 61..67 -> "Mưa"; in 71..77, 85, 86 -> "Tuyết"; in 80..82 -> "Mưa rào"; in 95..99 -> "Dông"
    else -> "Không rõ"
}

/** Dự báo từ Open-Meteo (không cần API key). Kết quả được lưu tạm để xem khi mất mạng. */
object WeatherRepository {
    private const val STALE_MS = 2 * 3600_000L
    val place = MutableStateFlow<Place?>(null)
    val state = MutableStateFlow<WeatherData?>(null)
    private var loaded = false

    private fun prefs(ctx: Context) = ctx.getSharedPreferences("weather", Context.MODE_PRIVATE)

    fun init(ctx: Context) {
        if (loaded) return
        loaded = true
        val p = prefs(ctx)
        val name = p.getString("name", null) ?: return
        val pl = Place(name, java.lang.Double.longBitsToDouble(p.getLong("lat", 0)), java.lang.Double.longBitsToDouble(p.getLong("lon", 0)))
        place.value = pl
        state.value = p.getString("cache", null)?.let { runCatching { parse(pl, p.getLong("at", 0), JSONObject(it)) }.getOrNull() }
    }

    fun setPlace(ctx: Context, pl: Place) {
        prefs(ctx).edit().putString("name", pl.name)
            .putLong("lat", java.lang.Double.doubleToLongBits(pl.lat)).putLong("lon", java.lang.Double.doubleToLongBits(pl.lon))
            .remove("cache").apply()
        place.value = pl
        state.value = null
    }

    suspend fun refresh(ctx: Context, force: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        val pl = place.value ?: return@withContext false
        val cur = state.value
        if (!force && cur != null && cur.place == pl && System.currentTimeMillis() - cur.fetchedAt < STALE_MS) return@withContext true
        try {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=${pl.lat}&longitude=${pl.lon}" +
                "&current=temperature_2m,relative_humidity_2m,weather_code" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max" +
                "&timezone=auto&forecast_days=7"
            val text = httpGet(url)
            val now = System.currentTimeMillis()
            val data = parse(pl, now, JSONObject(text))
            prefs(ctx).edit().putString("cache", text).putLong("at", now).apply()
            state.value = data
            true
        } catch (e: Exception) { false }
    }

    suspend fun searchCity(query: String): List<Place> = withContext(Dispatchers.IO) {
        try {
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=${URLEncoder.encode(query, "UTF-8")}&count=6&language=vi&format=json"
            val arr = JSONObject(httpGet(url)).optJSONArray("results") ?: return@withContext emptyList()
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val label = listOf(o.optString("name"), o.optString("admin1"), o.optString("country")).filter { it.isNotBlank() }.joinToString(", ")
                Place(label, o.getDouble("latitude"), o.getDouble("longitude"))
            }
        } catch (e: Exception) { emptyList() }
    }

    /** Vị trí gần đúng gần nhất mà hệ thống đã biết (cần quyền ACCESS_COARSE_LOCATION). Null nếu chưa có. */
    @SuppressLint("MissingPermission")
    fun currentLocation(ctx: Context): Place? {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time } ?: return null
        return Place("Vị trí hiện tại", loc.latitude, loc.longitude)
    }

    private fun parse(pl: Place, at: Long, root: JSONObject): WeatherData {
        val cur = root.getJSONObject("current")
        val d = root.getJSONObject("daily")
        val time = d.getJSONArray("time"); val codes = d.getJSONArray("weather_code")
        val tmax = d.getJSONArray("temperature_2m_max"); val tmin = d.getJSONArray("temperature_2m_min")
        val rain = d.getJSONArray("precipitation_sum"); val prob = d.getJSONArray("precipitation_probability_max")
        val days = (0 until time.length()).map { i ->
            DayForecast(time.getString(i), codes.optInt(i, 0), tmax.optDouble(i, 0.0), tmin.optDouble(i, 0.0), rain.optDouble(i, 0.0), prob.optInt(i, 0))
        }
        return WeatherData(pl, at, cur.getDouble("temperature_2m"), cur.optInt("relative_humidity_2m", 0), cur.optInt("weather_code", 0), days)
    }

    private fun httpGet(u: String): String {
        val c = URL(u).openConnection() as HttpURLConnection
        c.connectTimeout = 8000; c.readTimeout = 8000
        try {
            if (c.responseCode != 200) throw IOException("HTTP ${c.responseCode}")
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally { c.disconnect() }
    }
}
