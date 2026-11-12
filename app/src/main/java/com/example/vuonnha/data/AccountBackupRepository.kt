package com.example.vuonnha.data

import android.content.Context
import com.example.vuonnha.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.io.File
import java.io.FilterOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.security.MessageDigest

data class AccountBackup(val revision: Long, val updatedAt: Long, val bytes: Long)
object AccountBackupState {
    val message = MutableStateFlow("")
    val latest = MutableStateFlow<Pair<String, AccountBackup>?>(null)
}

/** One complete ZIP per account. Restore is explicit; revisions prevent silent multi-device overwrites. */
class AccountBackupRepository(private val context: Context, private val endpoint: String = BuildConfig.SCAN_ENDPOINT,
    private val garden: CareRepository = CareRepository.get(context)) {
    companion object { private val transfer = Mutex(); const val LIMIT = 25_000_000 }
    private val prefs get() = context.getSharedPreferences("account_backup", Context.MODE_PRIVATE)
    private fun userId() = AccountSession.user.value?.id ?: error("Bạn cần đăng nhập trước.")
    private fun connection(path: String, method: String): HttpURLConnection {
        check(endpoint.isNotBlank()) { "Máy chủ sao lưu chưa được cấu hình." }
        val token = AccountSession.credential(context) ?: error("Phiên đăng nhập đã hết hạn.")
        return (URI(endpoint).resolve(path).toURL().openConnection() as HttpURLConnection).apply {
            requestMethod = method; connectTimeout = 8000; readTimeout = 60000; instanceFollowRedirects = false
            setRequestProperty("X-Account-Session", token)
        }
    }
    private fun checked(conn: HttpURLConnection) {
        if (conn.responseCode != 200) {
            if (conn.responseCode == 409) prefs.edit().remove("bound_account").apply()
            val text = conn.errorStream?.use { String(it.readBackupBytes(16000), Charsets.UTF_8) }
            error(runCatching { JSONObject(text.orEmpty()).getString("error") }.getOrDefault("Chưa kết nối được nơi lưu dữ liệu. Bản lưu trước vẫn giữ nguyên."))
        }
    }
    private fun metadata(json: JSONObject): AccountBackup? = json.optJSONObject("backup")?.let { AccountBackup(it.getLong("revision"), it.getLong("updatedAt"), it.getLong("bytes")) }
    suspend fun info(): AccountBackup? = withContext(Dispatchers.IO) {
        val id = userId(); val conn = connection("/auth/backup/meta", "GET")
        try { checked(conn); val result = metadata(JSONObject(conn.inputStream.use { String(it.readBackupBytes(16000), Charsets.UTF_8) })); check(userId() == id); result }
        finally { conn.disconnect() }
    }
    private fun bind(id: String, revision: Long) { check(userId() == id); prefs.edit().putString("bound_account", id).putLong("revision_$id", revision).apply() }
    suspend fun upload(expectedRevision: Long): AccountBackup = save(expectedRevision, enableAuto = true)
    private suspend fun save(expectedRevision: Long, enableAuto: Boolean): AccountBackup = transfer.withLock { withContext(Dispatchers.IO) {
        val id = userId(); val file = File.createTempFile("account_upload_", ".zip", context.cacheDir)
        AccountBackupState.message.value = "Đang lưu vườn vào tài khoản…"
        try {
            file.outputStream().use { raw ->
                val bounded = object : FilterOutputStream(raw) {
                    var size = 0L
                    override fun write(b: Int) { check(++size <= LIMIT) { "Vườn vượt 25 MB. Hãy xuất ZIP để giữ đủ ảnh." }; out.write(b) }
                    override fun write(b: ByteArray, off: Int, len: Int) { size += len; check(size <= LIMIT) { "Vườn vượt 25 MB. Hãy xuất ZIP để giữ đủ ảnh." }; out.write(b, off, len) }
                }
                garden.exportZip(bounded)
            }
            currentCoroutineContext().ensureActive(); check(userId() == id)
            val conn = connection("/auth/backup", "PUT")
            try {
                conn.doOutput = true; conn.setRequestProperty("Content-Type", "application/zip"); conn.setRequestProperty("X-Backup-Revision", expectedRevision.toString()); conn.setFixedLengthStreamingMode(file.length())
                conn.outputStream.use { output -> file.inputStream().use { it.copyTo(output) } }
                checked(conn)
                val result = requireNotNull(metadata(JSONObject(conn.inputStream.use { String(it.readBackupBytes(16000), Charsets.UTF_8) })))
                currentCoroutineContext().ensureActive(); check(userId() == id)
                if (enableAuto) bind(id, result.revision)
                else prefs.edit().putLong("revision_$id", result.revision).apply()
                AccountBackupState.message.value = if (!enableAuto && !enabled()) "Đã lưu xong lượt đang chạy; tự lưu đã dừng." else "Đã lưu vườn vào tài khoản."
                AccountBackupState.latest.value = id to result
                result
            } finally { conn.disconnect() }
        } catch (e: Exception) {
            if (AccountSession.user.value?.id == id) AccountBackupState.message.value = "Chưa lưu được. Vườn trên thiết bị vẫn được giữ."
            throw e
        } finally { file.delete() }
    } }
    suspend fun restore(): AccountBackup = transfer.withLock { withContext(Dispatchers.IO) {
        val id = userId(); val conn = connection("/auth/backup", "GET")
        try {
            checked(conn)
            val revision = conn.getHeaderField("X-Backup-Revision").toLong()
            val checksum = conn.getHeaderField("X-Backup-Checksum")
            val bytes = conn.inputStream.use { it.readBackupBytes(LIMIT.toLong()) }
            val actual = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
            check(actual == checksum) { "Bản tải về chưa đầy đủ. Vườn trên máy vẫn được giữ." }
            currentCoroutineContext().ensureActive(); check(userId() == id)
            // Preserve a local safety copy before the user-approved replacement.
            val safety = File(context.filesDir, "garden-before-account-restore.zip")
            val temp = File.createTempFile("safety_", ".zip", context.cacheDir)
            try { temp.outputStream().use { garden.exportZip(it) }; temp.copyTo(safety, overwrite = true) } finally { temp.delete() }
            prefs.edit().remove("bound_account").apply()
            currentCoroutineContext().ensureActive(); check(userId() == id)
            garden.importBackup(bytes.inputStream())
            bind(id, revision)
            AccountBackupState.message.value = "Đã khôi phục vườn từ tài khoản."
            AccountBackup(revision, System.currentTimeMillis(), bytes.size.toLong())
        } finally { conn.disconnect() }
    } }
    fun enabled() = AccountSession.user.value?.id?.let { prefs.getString("bound_account", null) == it } == true
    fun pause() { prefs.edit().remove("bound_account").apply(); AccountBackupState.message.value = "Đã tạm dừng tự lưu. Bản trên máy chủ vẫn được giữ." }
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    suspend fun observe() {
        AccountSession.user.collectLatest { account ->
            AccountBackupState.message.value = ""
            if (account != null) {
                val repo = garden
                combine(repo.items, repo.tasks, repo.logs, repo.diary, repo.zones) { a,b,c,d,e -> listOf(a,b,c,d,e) }
                    .debounce(2500).collect {
                        while (enabled()) {
                            try { save(prefs.getLong("revision_${account.id}", 0), enableAuto = false); break }
                            catch (e: CancellationException) { throw e }
                            catch (e: Exception) { AccountBackupState.message.value = "Chưa tự lưu được. Kiểm tra kết nối hoặc vào Tài khoản để lưu lại." }
                            if (enabled()) delay(30000) else break
                        }
                    }
            }
        }
    }
}
