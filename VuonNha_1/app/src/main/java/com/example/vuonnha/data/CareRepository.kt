package com.example.vuonnha.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.room.withTransaction
import com.example.vuonnha.domain.completed
import com.example.vuonnha.domain.snoozed
import com.example.vuonnha.domain.startOfDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class CareRepository private constructor(private val ctx: Context) {
    private val db = AppDb.get(ctx)
    private val dao = db.dao()

    val items = dao.items()
    val tasks = dao.tasks()
    val logs = dao.logs()
    val diary = dao.allEntries()
    val zones = dao.zones()

    // ---- Cây / vật nuôi ----
    suspend fun addItem(name: String, s: Species, cycles: List<Int>, zoneId: Long?) = db.withTransaction {
        val id = dao.insertItem(Item(name = name, speciesId = s.id, zoneId = zoneId))
        dao.insertTasks(s.tasks.mapIndexed { i, (n, d) ->
            CareTask(itemId = id, name = n, everyDays = cycles.getOrElse(i) { d }, lastDone = startOfDay(), type = TaskType.infer(n).name)
        })
    }
    suspend fun editItem(i: Item, name: String, zoneId: Long?) {
        if (name != i.name || zoneId != i.zoneId) dao.updateItem(i.copy(name = name, zoneId = zoneId))
    }

    // ---- Khu vực ----
    suspend fun addZone(name: String, outdoor: Boolean, covered: Boolean, emoji: String) {
        dao.insertZone(Zone(name = name, outdoor = outdoor, covered = covered, emoji = emoji))
    }
    suspend fun removeZone(id: Long) = db.withTransaction { dao.clearZoneOf(id); dao.deleteZone(id) }
    suspend fun removeItem(id: Long) {
        dao.entriesOf(id).forEach { deleteFile(it.photoUri) }
        dao.deleteItem(id) // khoá ngoại CASCADE tự xoá việc + nhật ký
    }
    suspend fun updateCycle(t: CareTask, days: Int) = dao.updateTask(t.copy(everyDays = days))
    suspend fun addTask(itemId: Long, name: String, days: Int, type: TaskType = TaskType.infer(name)) {
        dao.insertTask(CareTask(itemId = itemId, name = name, everyDays = days, lastDone = startOfDay(), type = type.name))
    }
    suspend fun removeTask(t: CareTask) = dao.deleteTask(t)

    // ---- Nhắc nhở ----
    suspend fun done(t: CareTask) = db.withTransaction {
        dao.updateTask(t.completed())
        dao.insertLog(CareLog(itemId = t.itemId, task = t.name, at = System.currentTimeMillis()))
    }
    suspend fun snooze(t: CareTask) = dao.updateTask(t.snoozed())

    // ---- Nhật ký (ảnh được thu nhỏ còn tối đa 1280px, nén JPEG 80% trước khi lưu) ----
    suspend fun addEntry(itemId: Long, note: String, uri: Uri?) = withContext(Dispatchers.IO) {
        val path = uri?.let { saveCompressed(it) } ?: ""
        dao.insertEntry(DiaryEntry(itemId = itemId, at = System.currentTimeMillis(), note = note, photoUri = path))
        uri?.let { deleteCameraTemp(it) }
    }

    /** Ảnh chụp từ camera được ghi tạm vào cache/camera; sau khi đã nén và lưu bản chính thì xoá bản tạm. */
    private fun deleteCameraTemp(uri: Uri) {
        if (uri.authority != "${ctx.packageName}.fileprovider") return
        uri.lastPathSegment?.let { File(File(ctx.cacheDir, "camera"), it).delete() }
    }
    suspend fun removeEntry(e: DiaryEntry) = withContext(Dispatchers.IO) {
        deleteFile(e.photoUri)
        dao.deleteEntry(e)
    }

    private fun saveCompressed(uri: Uri): String {
        val bmp = loadScaled(uri) ?: return ""
        val f = File(ctx.filesDir, "diary_${System.currentTimeMillis()}.jpg")
        f.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 80, it) }
        bmp.recycle()
        return f.absolutePath
    }

    private fun loadScaled(uri: Uri): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= 28) {
            // ImageDecoder tự xoay ảnh đúng chiều theo EXIF
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(ctx.contentResolver, uri)) { dec, info, _ ->
                val s = maxOf(info.size.width, info.size.height) / MAX_PX.toFloat()
                if (s > 1f) dec.setTargetSize((info.size.width / s).toInt(), (info.size.height / s).toInt())
                dec.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, o) }
            var s = 1
            while (maxOf(o.outWidth, o.outHeight) / s > MAX_PX) s *= 2
            ctx.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = s })
            }
        }
    } catch (e: Exception) { null }

    private fun deleteFile(path: String) { if (path.isNotEmpty()) File(path).delete() }

    // ---- Sao lưu ZIP: data.json + photos/*.jpg ----
    /** Ghi file ZIP (dữ liệu + ảnh) vào luồng đầu ra. Trả về số ảnh đã đóng gói. */
    suspend fun exportZip(out: OutputStream): Int = withContext(Dispatchers.IO) {
        val items = dao.itemsOnce(); val tasks = dao.tasksOnce(); val logs = dao.logsOnce(); val entries = dao.entriesOnce()
        val zonesList = dao.zonesOnce()
        val hasPhoto = entries.map { it.photoUri.isNotEmpty() && File(it.photoUri).exists() }
        val root = JSONObject().put("app", "VuonNha").put("version", 2)
        root.put("items", JSONArray().apply {
            items.forEach { i ->
                val o = JSONObject().put("id", i.id).put("name", i.name).put("speciesId", i.speciesId)
                i.zoneId?.let { o.put("zoneId", it) }
                put(o)
            }
        })
        root.put("zones", JSONArray().apply {
            zonesList.forEach { z ->
                put(JSONObject().put("id", z.id).put("name", z.name).put("outdoor", z.outdoor).put("covered", z.covered).put("emoji", z.emoji))
            }
        })
        root.put("tasks", JSONArray().apply {
            tasks.forEach { put(JSONObject().put("itemId", it.itemId).put("name", it.name).put("everyDays", it.everyDays)
                .put("lastDone", it.lastDone).put("snoozeUntil", it.snoozeUntil).put("type", it.type)) }
        })
        root.put("logs", JSONArray().apply {
            logs.forEach { put(JSONObject().put("itemId", it.itemId).put("task", it.task).put("at", it.at)) }
        })
        root.put("diary", JSONArray().apply {
            entries.forEachIndexed { i, e ->
                val o = JSONObject().put("itemId", e.itemId).put("at", e.at).put("note", e.note)
                if (hasPhoto[i]) o.put("photo", "photos/$i.jpg")
                put(o)
            }
        })
        var count = 0
        ZipOutputStream(out.buffered()).use { zip ->
            zip.putNextEntry(ZipEntry("data.json"))
            zip.write(root.toString(2).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            entries.forEachIndexed { i, e ->
                if (hasPhoto[i]) {
                    zip.putNextEntry(ZipEntry("photos/$i.jpg"))
                    File(e.photoUri).inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                    count++
                }
            }
        }
        count
    }

    /** Nhập file sao lưu: tự nhận ZIP (có ảnh) hoặc JSON cũ (không có ảnh). Trả về số cây / vật nuôi. */
    suspend fun importBackup(input: InputStream): Int = withContext(Dispatchers.IO) {
        val bin = BufferedInputStream(input)
        bin.mark(4)
        val b0 = bin.read(); val b1 = bin.read()
        bin.reset()
        if (b0 == 'P'.code && b1 == 'K'.code) importZip(bin)
        else importJson(bin.readBytes().toString(Charsets.UTF_8), null)
    }

    private suspend fun importZip(input: InputStream): Int {
        val tmp = File(ctx.cacheDir, "import_tmp").apply { deleteRecursively(); mkdirs() }
        try {
            var json: String? = null
            val photos = HashMap<String, File>()
            ZipInputStream(input).use { zip ->
                var n = 0
                var e = zip.nextEntry
                while (e != null) {
                    when {
                        e.isDirectory -> {}
                        e.name == "data.json" -> json = zip.readBytes().toString(Charsets.UTF_8)
                        e.name.startsWith("photos/") -> {
                            // đặt tên file tạm theo số thứ tự, không dùng tên trong ZIP (tránh path traversal)
                            val f = File(tmp, "p${n++}.jpg")
                            f.outputStream().use { zip.copyTo(it) }
                            photos[e.name] = f
                        }
                    }
                    e = zip.nextEntry
                }
            }
            val text = json ?: throw IllegalArgumentException("Thiếu data.json")
            return importJson(text, photos)
        } finally {
            tmp.deleteRecursively()
        }
    }

    /**
     * Thay toàn bộ dữ liệu hiện có bằng dữ liệu trong file. [photos] = null nghĩa là file JSON cũ không có ảnh
     * (giữ nguyên ảnh đang có trên máy); khác null là ZIP, ảnh cũ sẽ được dọn sau khi nhập thành công.
     */
    private suspend fun importJson(text: String, photos: Map<String, File>?): Int {
        val root = JSONObject(text)
        val items = root.getJSONArray("items")
        val oldPhotos = dao.entriesOnce().map { it.photoUri }
        db.withTransaction {
            dao.clearItems() // CASCADE xoá luôn việc, log, nhật ký
            dao.clearZones()
            val zmap = HashMap<Long, Long>()
            root.optJSONArray("zones")?.let { a ->
                for (i in 0 until a.length()) {
                    val j = a.getJSONObject(i)
                    zmap[j.getLong("id")] = dao.insertZone(Zone(name = j.getString("name"), outdoor = j.getBoolean("outdoor"),
                        covered = j.optBoolean("covered", false), emoji = j.optString("emoji", "🌿")))
                }
            }
            val map = HashMap<Long, Long>()
            for (i in 0 until items.length()) {
                val j = items.getJSONObject(i)
                map[j.getLong("id")] = dao.insertItem(Item(name = j.getString("name"), speciesId = j.getString("speciesId"),
                    zoneId = if (j.has("zoneId")) zmap[j.getLong("zoneId")] else null))
            }
            root.optJSONArray("tasks")?.let { a ->
                for (i in 0 until a.length()) {
                    val j = a.getJSONObject(i)
                    val id = map[j.getLong("itemId")] ?: continue
                    dao.insertTask(CareTask(itemId = id, name = j.getString("name"), everyDays = j.getInt("everyDays"),
                        lastDone = j.getLong("lastDone"), snoozeUntil = j.optLong("snoozeUntil", 0),
                        type = j.optString("type", "").ifEmpty { TaskType.infer(j.getString("name")).name }))
                }
            }
            root.optJSONArray("logs")?.let { a ->
                for (i in 0 until a.length()) {
                    val j = a.getJSONObject(i)
                    val id = map[j.getLong("itemId")] ?: continue
                    dao.insertLog(CareLog(itemId = id, task = j.getString("task"), at = j.getLong("at")))
                }
            }
            root.optJSONArray("diary")?.let { a ->
                for (i in 0 until a.length()) {
                    val j = a.getJSONObject(i)
                    val id = map[j.getLong("itemId")] ?: continue
                    val src = photos?.get(j.optString("photo", ""))
                    val path = if (src != null) {
                        val dst = File(ctx.filesDir, "diary_${System.nanoTime()}.jpg")
                        src.copyTo(dst, overwrite = true)
                        dst.absolutePath
                    } else ""
                    dao.insertEntry(DiaryEntry(itemId = id, at = j.getLong("at"), note = j.optString("note", ""), photoUri = path))
                }
            }
        }
        if (photos != null) oldPhotos.forEach { deleteFile(it) }
        return items.length()
    }

    companion object {
        private const val MAX_PX = 1280
        @Volatile private var inst: CareRepository? = null
        fun get(ctx: Context): CareRepository = inst ?: synchronized(this) {
            inst ?: CareRepository(ctx.applicationContext).also { inst = it }
        }
    }
}