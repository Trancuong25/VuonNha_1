package com.example.vuonnha.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.vuonnha.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class AccountUser(val id: String, val username: String, val name: String)

/** Only an encrypted session is persisted; passwords never enter preferences or Room. */
object AccountSession {
    private val mutable = MutableStateFlow<AccountUser?>(null)
    val user = mutable.asStateFlow()
    @Volatile private var loaded = false
    private var token: String? = null
    private var expiresAt = 0L
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return store.getKey("vuonnha.account", null) as? SecretKey ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder("vuonnha.account", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build())
        }.generateKey()
    }
    @Synchronized fun load(context: Context) {
        if (loaded) return
        loaded = true
        val prefs = context.getSharedPreferences("account_session", Context.MODE_PRIVATE)
        val saved = prefs.getString("encrypted", null) ?: return
        try {
            val parts = saved.split(':')
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)))
            val data = JSONObject(String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), Charsets.UTF_8))
            accept(data)
            if (expiresAt <= System.currentTimeMillis()) clear(context)
        } catch (_: Exception) { clear(context) }
    }
    private fun accept(data: JSONObject) {
        val u = data.getJSONObject("user")
        token = data.getString("token"); expiresAt = data.getLong("expiresAt")
        mutable.value = AccountUser(u.getString("id"), u.getString("username"), u.getString("name"))
    }
    @Synchronized fun save(context: Context, data: JSONObject) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encrypted = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(cipher.doFinal(data.toString().toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
        check(context.getSharedPreferences("account_session", Context.MODE_PRIVATE).edit().putString("encrypted", encrypted).commit())
        accept(data); loaded = true
    }
    @Synchronized fun credential(context: Context): String? {
        load(context)
        if (token != null && expiresAt <= System.currentTimeMillis()) clear(context)
        return token
    }
    @Synchronized fun clear(context: Context) {
        token = null; expiresAt = 0; mutable.value = null; loaded = true
        context.getSharedPreferences("account_session", Context.MODE_PRIVATE).edit().clear().commit()
    }
}

class AccountRepository(private val context: Context, val endpoint: String = BuildConfig.SCAN_ENDPOINT) {
    suspend fun authenticate(register: Boolean, name: String, username: String, password: String) = withContext(Dispatchers.IO) {
        val result = request(if (register) "/auth/register" else "/auth/login", "POST", JSONObject().put("name", name.trim()).put("username", username.trim()).put("password", password))
        AccountSession.save(context, result)
    }
    suspend fun refresh() = withContext(Dispatchers.IO) {
        AccountSession.load(context)
        if (AccountSession.credential(context) != null) request("/auth/me", "GET")
    }
    suspend fun logout() = withContext(Dispatchers.IO) {
        try { request("/auth/logout", "POST") }
        catch (e: Exception) { error("Đã đăng xuất trên thiết bị. Chưa kết thúc được phiên trên máy chủ; phiên đó tự hết hạn sau tối đa 7 ngày.") }
        finally { AccountSession.clear(context) }
    }
    private fun request(path: String, method: String, body: JSONObject? = null): JSONObject {
        check(endpoint.isNotBlank()) { "Máy chủ tài khoản chưa được cấu hình." }
        val conn = URI(endpoint).resolve(path).toURL().openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method; conn.connectTimeout = 8000; conn.readTimeout = 15000; conn.instanceFollowRedirects = false
            AccountSession.credential(context)?.let { conn.setRequestProperty("X-Account-Session", it) }
            if (body != null) {
                conn.doOutput = true; conn.setRequestProperty("Content-Type", "application/json")
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = conn.responseCode
            val stream = if (status == 200) conn.inputStream else conn.errorStream
            val response = stream?.use { JSONObject(String(it.readBackupBytes(16000), Charsets.UTF_8)) } ?: JSONObject()
            if (status != 200) {
                if (status == 401 && path == "/auth/me") AccountSession.clear(context)
                error(response.optString("error", "Chưa kết nối được tài khoản."))
            }
            return response
        } catch (_: java.io.IOException) { error("Không kết nối được máy chủ tài khoản. Vườn trên thiết bị vẫn dùng được.") }
        finally { conn.disconnect() }
    }
}
