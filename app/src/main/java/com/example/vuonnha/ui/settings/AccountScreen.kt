package com.example.vuonnha.ui.settings

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vuonnha.data.*
import com.example.vuonnha.ui.common.GardenWordmark
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class AccountViewModel(app: Application, repository: AccountRepository) : AndroidViewModel(app) {
    constructor(app: Application) : this(app, AccountRepository(app))
    private val repo = repository
    private val backups = AccountBackupRepository(app, repository.endpoint)
    var backup by mutableStateOf<AccountBackup?>(null); private set
    var loaded by mutableStateOf(false); private set
    var busy by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    init { run { repo.refresh(); if (AccountSession.user.value != null) { backup = backups.info(); loaded = true } } }
    private fun run(block: suspend () -> Unit) {
        if (busy) return
        busy = true; error = null
        viewModelScope.launch {
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { error = if (e is java.io.IOException) "Chưa kết nối được nơi lưu dữ liệu. Vườn trên máy vẫn được giữ." else e.message ?: "Chưa xử lý được. Bạn thử lại nhé." }
            finally { busy = false }
        }
    }
    fun submit(register: Boolean, name: String, username: String, password: String, onSuccess: () -> Unit) = run {
        repo.authenticate(register, name, username, password); onSuccess()
        loaded = false
        if (register) backup = backups.upload(0) else backup = backups.info()
        loaded = true
    }
    fun refreshBackup() = run { backup = backups.info(); loaded = true }
    fun saveGarden() = run { val latest = backups.info(); backup = backups.upload(latest?.revision ?: 0); loaded = true }
    fun restoreGarden() = run { backups.restore(); backup = backups.info(); loaded = true }
    fun pauseBackup() { backups.pause() }
    fun logout() = run { repo.logout(); backup = null; loaded = false }
}

@Composable fun AccountScreen(onBack: () -> Unit, vm: AccountViewModel = viewModel()) {
    val user by AccountSession.user.collectAsStateWithLifecycle()
    val backupMessage by AccountBackupState.message.collectAsStateWithLifecycle()
    val saved by AccountBackupState.latest.collectAsStateWithLifecycle()
    val latestBackup = saved?.takeIf { it.first == user?.id && (vm.backup == null || it.second.revision > vm.backup!!.revision) }?.second ?: vm.backup
    var register by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }
    var confirmTransfer by remember { mutableStateOf<String?>(null) }
    val usernameValid = Regex("[a-zA-Z0-9_.-]{3,40}").matches(username.trim())
    val passwordValid = password.length in 10..128
    val nameValid = name.trim().length in 1..80
    val valid = usernameValid && passwordValid && (!register || (nameValid && password == confirmation))
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onBack, enabled = !vm.busy) { Text("‹ Quay lại") }
        GardenWordmark()
        Text(if (user != null) "Tài khoản của bạn" else if (register) "Giữ lại những ngày cây lớn" else "Chào bạn trở lại", style = MaterialTheme.typography.headlineSmall)
        Text(if (user != null) "Quản lý bản lưu để mang vườn sang thiết bị khác."
            else "Bạn vẫn có thể chăm vườn mà không đăng nhập.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        user?.let { account ->
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Outlined.PersonOutline, null, Modifier.size(36.dp))
                    Column { Text(account.name, style = MaterialTheme.typography.titleLarge); Text("@${account.username}"); Text("Đã đăng nhập", style = MaterialTheme.typography.labelMedium) }
                }
            }
            OutlinedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.CloudDone, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Bản lưu của vườn", style = MaterialTheme.typography.titleMedium)
                    Text(latestBackup?.let { "Lần lưu: ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(it.updatedAt))} · ${it.bytes / 1024} KB" }
                        ?: if (vm.loaded) "Chưa có bản lưu trong tài khoản." else "Chưa kiểm tra được bản lưu.", style = MaterialTheme.typography.bodyMedium)
                    Text("Sau khi lưu hoặc khôi phục thành công, app tự lưu thay đổi khi đang mở và có kết nối. Tối đa 25 MB gồm ảnh. Đăng nhập một mình chưa bảo đảm dữ liệu đã được lưu.", style = MaterialTheme.typography.bodySmall)
                    if (backupMessage.isNotBlank()) Text(backupMessage, style = MaterialTheme.typography.bodySmall)
                    Button({ confirmTransfer = "save" }, Modifier.fillMaxWidth(), enabled = !vm.busy) { Text("Lưu vườn hiện tại") }
                    OutlinedButton({ confirmTransfer = "restore" }, Modifier.fillMaxWidth(), enabled = !vm.busy && latestBackup != null) { Text("Lấy lại vườn đã lưu") }
                    Row { TextButton({ vm.refreshBackup() }, enabled = !vm.busy) { Text("Kiểm tra lại") }; TextButton({ vm.pauseBackup() }, enabled = !vm.busy) { Text("Dừng tự lưu") } }
                }
            }
            Text("Vườn trên máy vẫn dùng chung khi đổi tài khoản. Chỉ khôi phục nếu bạn muốn thay vườn hiện tại bằng bản đã lưu.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton({ confirmLogout = true }, enabled = !vm.busy, modifier = Modifier.fillMaxWidth()) { Text("Đăng xuất") }
        } ?: run {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(!register, { register = false; password = ""; confirmation = ""; attempted = false }, label = { Text("Đăng nhập") }, enabled = !vm.busy)
                        FilterChip(register, { register = true; password = ""; confirmation = ""; attempted = false }, label = { Text("Tạo tài khoản") }, enabled = !vm.busy)
                    }
                    if (register) OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), label = { Text("Tên hiển thị") }, singleLine = true, enabled = !vm.busy,
                        isError = attempted && !nameValid, supportingText = { if (attempted && !nameValid) Text("Bạn điền tên muốn mọi người gọi nhé, tối đa 80 ký tự.") })
                    OutlinedTextField(username, { username = it }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), label = { Text("Tên đăng nhập") }, singleLine = true, enabled = !vm.busy,
                        isError = attempted && !usernameValid, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), supportingText = { Text("3–40 ký tự: chữ không dấu, số, dấu chấm, _ hoặc -") })
                    OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), label = { Text("Mật khẩu") }, singleLine = true, enabled = !vm.busy,
                        isError = attempted && !passwordValid, visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), supportingText = { Text("Từ 10 đến 128 ký tự.") },
                        trailingIcon = { TextButton({ visible = !visible }) { Text(if (visible) "Ẩn" else "Hiện") } })
                    if (register) OutlinedTextField(confirmation, { confirmation = it }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), label = { Text("Nhập lại mật khẩu") }, singleLine = true, enabled = !vm.busy,
                        visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = (attempted || confirmation.isNotEmpty()) && confirmation != password,
                        supportingText = { if ((attempted || confirmation.isNotEmpty()) && confirmation != password) Text("Hai mật khẩu chưa giống nhau.") })
                    Button({ attempted = true; if (valid) vm.submit(register, name, username, password) { password = ""; confirmation = "" } }, Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(14.dp), enabled = !vm.busy) {
                        Text(if (register) "Tạo tài khoản và đăng nhập" else "Đăng nhập vào Vườn Nhà")
                    }
                }
            }
            if (register) Text("Khi tạo tài khoản, app sẽ thử lưu vườn hiện tại gồm cây, lịch và ảnh. Hãy kiểm tra thông báo lưu thành công. Mật khẩu được băm trên máy chủ, không lưu dạng đọc được.", style = MaterialTheme.typography.bodySmall)
            TextButton(onBack, Modifier.fillMaxWidth(), enabled = !vm.busy) { Text("Tiếp tục chăm vườn, chưa đăng nhập") }
        }
        if (vm.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text("Chỉ dữ liệu đã báo lưu thành công mới có thể lấy lại. Bạn nên giữ thêm bản ZIP riêng. Chưa hỗ trợ quên mật khẩu.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (confirmLogout) AlertDialog(onDismissRequest = { confirmLogout = false }, title = { Text("Đăng xuất?") }, text = { Text("Vườn trên thiết bị và bản lưu tài khoản vẫn được giữ. Việc tự lưu tạm dừng khi đăng xuất.") },
        confirmButton = { TextButton({ confirmLogout = false; vm.logout() }) { Text("Đăng xuất") } }, dismissButton = { TextButton({ confirmLogout = false }) { Text("Giữ lại") } })
    confirmTransfer?.let { operation ->
        AlertDialog(onDismissRequest = { confirmTransfer = null }, title = { Text(if (operation == "save") "Lưu vườn vào tài khoản?" else "Lấy lại vườn đã lưu?") },
            text = { Text(if (operation == "save") "Toàn bộ cây, lịch, lịch sử và ảnh trên máy sẽ thay bản lưu hiện tại của tài khoản này."
                else "Vườn trên thiết bị sẽ được thay thế bằng bản của tài khoản. App giữ một bản ZIP dự phòng cục bộ trước khi thay; bạn nên xuất ZIP riêng nếu cần giữ vườn hiện tại.") },
            confirmButton = { TextButton({ confirmTransfer = null; if (operation == "save") vm.saveGarden() else vm.restoreGarden() }) { Text("Xác nhận") } },
            dismissButton = { TextButton({ confirmTransfer = null }) { Text("Hủy") } })
    }
}
