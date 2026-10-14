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

class CareRepository internal constructor(private val ctx: Context, private val db: AppDb = AppDb.get(ctx)) {
    private val dao = db.dao()

    val items = dao.items()
    val tasks = dao.tasks()
    val logs = dao.logs()
    val diary = dao.allEntries()
    val zones = dao.zones()

    // ---- Cây / vật nuôi ----
    suspend fun addItem(name: String, s: Species, cycles: List<Int>, zoneId: Long?, starts: List<Long> = emptyList()) = db.withTransaction {
        require(name.isNotBlank() && cycles.all { it > 0 })
        require(starts.isEmpty() || starts.size == s.tasks.size)
        val id = dao.insertItem(Item(name = name, speciesId = s.id, zoneId = zoneId, profileJson = s.profileJson))
        dao.insertTasks(s.tasks.mapIndexed { i, (n, d) ->
            CareTask(itemId = id, name = n, everyDays = cycles.getOrElse(i) { d }, lastDone = starts.getOrElse(i) { startOfDay() }, type = TaskType.infer(n).name)
        })
        id
    }

    /** Save the new plant and its first photo together; a failed save leaves no half-created plant. */
    suspend fun addScannedItem(name: String, s: Species, cycles: List<Int>, zoneId: Long?, starts: List<Long>,
        photo: Bitmap, note: String) = withContext(Dispatchers.IO) {
        val file = File.createTempFile("diary_", ".jpg", ctx.filesDir)
        try {
            file.outputStream().use { check(photo.compress(Bitmap.CompressFormat.JPEG, 80, it)) }
            db.withTransaction {
                val id = addItem(name, s, cycles, zoneId, starts)
                dao.insertEntry(DiaryEntry(itemId = id, at = System.currentTimeMillis(), note = note, photoUri = file.absolutePath))
                id
            }
        } catch (e: Exception) {
            file.delete()
            throw e
        }
    }
    suspend fun editItem(i: Item, name: String, zoneId: Long?, speciesId: String = i.speciesId, profileJson: String = i.profileJson) {
        require(name.isNotBlank())
        if (name != i.name || zoneId != i.zoneId || speciesId != i.speciesId || profileJson != i.profileJson)
            dao.updateItem(i.copy(name = name, zoneId = zoneId, speciesId = speciesId, profileJson = profileJson))
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
    suspend fun updateCycle(t: CareTask, days: Int) {
        require(days > 0)
        dao.updateCycle(t.id, days)
    }
    suspend fun saveItemEdit(itemId: Long, name: String, zoneId: Long?, speciesId: String, profileJson: String, cycles: Map<Long, Int>) = db.withTransaction {
        require(name.trim().length in 1..120) { "Tên gọi cần có từ 1 đến 120 ký tự." }
        require(cycles.values.all { it in 1..365 }) { "Chu kỳ cần từ 1 đến 365 ngày." }
        val current = dao.itemOnce(itemId) ?: error("Cây này không còn trong vườn.")
        cycles.forEach { (id, days) ->
            val task = dao.taskOnce(id) ?: error("Lịch chăm sóc đã thay đổi. Hãy mở lại biểu mẫu.")
            require(task.itemId == itemId)
            dao.updateCycle(id, days)
        }
        editItem(current, name.trim(), zoneId, speciesId, profileJson)
    }
    suspend fun addTask(itemId: Long, name: String, days: Int, type: TaskType = TaskType.infer(name)) {
        require(name.isNotBlank() && days > 0)
        dao.insertTask(CareTask(itemId = itemId, name = name, everyDays = days, lastDone = startOfDay(), type = type.name))
    }
    suspend fun removeTask(t: CareTask) = dao.deleteTask(t)

    // ---- Nhắc nhở ----
    suspend fun done(t: CareTask) = db.withTransaction {
        val current = dao.taskOnce(t.id) ?: return@withTransaction
        dao.updateTask(current.completed())
        dao.insertLog(CareLog(itemId = current.itemId, task = current.name, at = System.currentTimeMillis()))
    }
    suspend fun snooze(t: CareTask) = db.withTransaction {
        dao.taskOnce(t.id)?.let { dao.updateTask(it.snoozed()) }
    }

    // ---- Nhật ký (ảnh được thu nhỏ còn tối đa 1280px, nén JPEG 80% trước khi lưu) ----
    suspend fun addEntry(itemId: Long, note: String, uri: Uri?) = withContext(Dispatchers.IO) {
        val path = uri?.let { saveCompressed(it) } ?: ""
        try {
            dao.insertEntry(DiaryEntry(itemId = itemId, at = System.currentTimeMillis(), note = note, photoUri = path))
        } catch (e: Exception) {
            deleteFile(path)
            throw e
        }
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
        val bmp = loadScaled(uri) ?: throw IllegalArgumentException("Không đọc được ảnh. Hãy chọn lại ảnh.")
        val f = File.createTempFile("diary_", ".jpg", ctx.filesDir)
        try {
            f.outputStream().use { check(bmp.compress(Bitmap.CompressFormat.JPEG, 80, it)) }
            return f.absolutePath
        } catch (e: Exception) {
            f.delete()
            throw e
        } finally { bmp.recycle() }
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
        val snapshot = db.withTransaction { BackupSnapshot(dao.itemsOnce(), dao.tasksOnce(), dao.logsOnce(), dao.entriesOnce(), dao.zonesOnce()) }
        val items = snapshot.items; val tasks = snapshot.tasks; val logs = snapshot.logs; val entries = snapshot.entries
        val zonesList = snapshot.zones
        val hasPhoto = entries.map { it.photoUri.isNotEmpty() && File(it.photoUri).exists() }
        val root = JSONObject().put("app", "VuonNha").put("version", 2)
        root.put("items", JSONArray().apply {
            items.forEach { i ->
                val o = JSONObject().put("id", i.id).put("name", i.name).put("speciesId", i.speciesId).put("profileJson", i.profileJson)
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
        else importJson(bin.readBackupBytes(10_000_000).toString(Charsets.UTF_8), null)
    }

    private suspend fun importZip(input: InputStream): Int {
        val tmp = File.createTempFile("import_", ".tmp", ctx.cacheDir).apply {
            check(delete() && mkdir()) { "Không tạo được thư mục nhập sao lưu." }
        }
        try {
            var json: String? = null
            val photos = HashMap<String, File>()
            ZipInputStream(input).use { zip ->
                var n = 0
                var total = 0L
                var entryCount = 0
                var e = zip.nextEntry
                while (e != null) {
                    require(++entryCount <= 2000) { "Sao lưu chứa quá nhiều file." }
                    when {
                        e.isDirectory -> {}
                        e.name == "data.json" -> {
                            require(json == null) { "Sao lưu có data.json trùng lặp." }
                            val bytes = zip.readBackupBytes(10_000_000)
                            total += bytes.size
                            json = bytes.toString(Charsets.UTF_8)
                        }
                        e.name.startsWith("photos/") -> {
                            // đặt tên file tạm theo số thứ tự, không dùng tên trong ZIP (tránh path traversal)
                            val f = File(tmp, "p${n++}.jpg")
                            require(!photos.containsKey(e.name)) { "Sao lưu có ảnh trùng lặp." }
                            f.outputStream().use { total += zip.copyBackupTo(it, minOf(20_000_000L, 200_000_000L - total)) }
                            photos[e.name] = f
                        }
                        else -> total += zip.copyBackupTo(object : OutputStream() {
                            override fun write(b: Int) {}
                            override fun write(b: ByteArray, off: Int, len: Int) {}
                        }, 200_000_000L - total)
                    }
                    require(total <= 200_000_000) { "Sao lưu giải nén vượt 200 MB." }
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
     * (khôi phục không có ảnh); ảnh cũ được dọn chỉ sau khi giao dịch nhập thành công.
     */
    private suspend fun importJson(text: String, photos: Map<String, File>?): Int {
        val root = JSONObject(text)
        val items = root.getJSONArray("items")
        val oldPhotos = dao.entriesOnce().map { it.photoUri }
        val createdPhotos = mutableListOf<File>()
        try { db.withTransaction {
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
                    zoneId = if (j.has("zoneId")) zmap[j.getLong("zoneId")] else null, profileJson = j.optString("profileJson", "")))
            }
            root.optJSONArray("tasks")?.let { a ->
                for (i in 0 until a.length()) {
                    val j = a.getJSONObject(i)
                    val id = map[j.getLong("itemId")] ?: continue
                    require(j.getInt("everyDays") > 0) { "Chu kỳ chăm sóc phải lớn hơn 0 ngày" }
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
                        createdPhotos.add(dst)
                        src.copyTo(dst, overwrite = true)
                        dst.absolutePath
                    } else ""
                    dao.insertEntry(DiaryEntry(itemId = id, at = j.getLong("at"), note = j.optString("note", ""), photoUri = path))
                }
            }
        } } catch (e: Exception) {
            createdPhotos.forEach { it.delete() }
            throw e
        }
        oldPhotos.forEach { deleteFile(it) }
        return items.length()
    }

    companion object {
        private const val MAX_PX = 1280
        @Volatile private var inst: CareRepository? = null
        fun get(ctx: Context): CareRepository = inst ?: synchronized(this) {
            inst ?: CareRepository(ctx.applicationContext).also { inst = it }
        }
    }
    private data class BackupSnapshot(val items: List<Item>, val tasks: List<CareTask>, val logs: List<CareLog>, val entries: List<DiaryEntry>, val zones: List<Zone>)
}
