package com.example.vuonnha.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vuonnha.ui.common.ScreenHeader
import com.example.vuonnha.ui.theme.ThemePrefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vuonnha.data.AccountSession
import androidx.compose.material.icons.outlined.PersonOutline

@Composable private fun SettingsAction(icon: ImageVector, title: String, detail: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
        ListItem(leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
            headlineContent = { Text(title) }, supportingContent = { Text(detail) },
            trailingContent = { Icon(Icons.Outlined.ChevronRight, null) })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun SettingsScreen(vm: SettingsViewModel = viewModel(), onAccount: () -> Unit = {}) {
    val ctx = LocalContext.current
    val account by AccountSession.user.collectAsStateWithLifecycle()
    var confirm by remember { mutableStateOf(false) }
    var choosingTheme by remember { mutableStateOf(false) }
    var privacy by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(NotificationManagerCompat.from(ctx).areNotificationsEnabled()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, ctx) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) notificationsEnabled = NotificationManagerCompat.from(ctx).areNotificationsEnabled()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsEnabled = NotificationManagerCompat.from(ctx).areNotificationsEnabled()
    }
    val exportL = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) vm.backup(uri)
    }
    val importL = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.restore(uri)
    }
    Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ScreenHeader("Cài đặt", "Giao diện, nhắc nhở và dữ liệu")
        SettingsAction(Icons.Outlined.PersonOutline, "Tài khoản", account?.let { "${it.name} · @${it.username}" } ?: "Đăng nhập hoặc tạo tài khoản", onAccount)
        ScanSettingsCard()

        Text("Tùy chỉnh", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SettingsAction(Icons.Outlined.Palette, "Giao diện", listOf("Theo hệ thống", "Sáng", "Tối").getOrElse(ThemePrefs.mode) { "Theo hệ thống" }, { choosingTheme = true })

        Text("Nhắc chăm sóc", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SettingsAction(Icons.Outlined.Schedule, "Giờ nhắc mỗi ngày", "%02d:%02d · Khi có việc đến hạn".format(vm.reminderMinutes / 60, vm.reminderMinutes % 60)) {
            android.app.TimePickerDialog(ctx, { _, hour, minute -> vm.setReminder(hour * 60 + minute) }, vm.reminderMinutes / 60, vm.reminderMinutes % 60, true).show()
        }
        SettingsAction(Icons.Outlined.Notifications, "Thông báo", if (notificationsEnabled) "Đã bật · Nhắc khi có việc đến hạn" else "Đang tắt · Bật để nhận nhắc chăm sóc") {
            if (android.os.Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED)
                notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            else ctx.startActivity(android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, ctx.packageName))
        }

        Text("Dữ liệu của bạn", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SettingsAction(Icons.Outlined.Backup, "Sao lưu", "Lưu cây, lịch và ảnh thành một file") { exportL.launch("vuonnha-" + SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()) + ".zip") }
        SettingsAction(Icons.Outlined.Restore, "Khôi phục", "Nhập lại từ bản sao lưu của bạn") { confirm = true }
        if (vm.message.isNotEmpty()) Text(vm.message, style = MaterialTheme.typography.bodyMedium)

        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton({ privacy = !privacy }) { Text(if (privacy) "Quyền riêng tư · Thu gọn" else "Quyền riêng tư · Xem chi tiết") }
            if (privacy)
            Text("Vườn và nhật ký lưu trên điện thoại. Khi tạo tài khoản hoặc chọn lưu vườn, cây, lịch, lịch sử, ghi chú và ảnh được gửi tới máy chủ làm bản sao lưu; tự lưu tiếp tục khi app đang mở và có kết nối. Khi dùng Trợ lý, chỉ tên cây, điều kiện trồng, lịch và lịch sử chăm sóc được gửi, không kèm ảnh/ghi chú nhật ký. Tin nhắn được xoá sau tối đa 48 giờ; bản sao lưu tài khoản không hết hạn cùng tin nhắn. Khi nhận diện, ảnh bạn chọn được gửi tới Gemini theo chính sách nhà cung cấp. Thời tiết dùng thành phố hoặc vị trí gần đúng khi bạn cho phép.", style = MaterialTheme.typography.bodySmall)
        } }
        Text("Vườn Nhà · Phiên bản 1.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (choosingTheme) AlertDialog(onDismissRequest = { choosingTheme = false }, title = { Text("Giao diện") },
        text = { Column { listOf(0 to "Theo hệ thống", 1 to "Sáng", 2 to "Tối").forEach { (mode, label) ->
            TextButton({ ThemePrefs.set(ctx, mode); choosingTheme = false }, Modifier.fillMaxWidth()) {
                RadioButton(ThemePrefs.mode == mode, null); Spacer(Modifier.width(12.dp)); Text(label, Modifier.weight(1f))
            }
        } } }, confirmButton = { TextButton({ choosingTheme = false }) { Text("Đóng") } })
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false }, title = { Text("Nhập dữ liệu?") },
        text = { Text("Toàn bộ dữ liệu hiện tại sẽ bị thay bằng dữ liệu trong file.") },
        confirmButton = { TextButton(onClick = { confirm = false; importL.launch(arrayOf("application/zip", "application/json", "*/*")) }) { Text("Chọn file") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Huỷ") } }
    )
}
