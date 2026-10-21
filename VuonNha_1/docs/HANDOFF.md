# HANDOFF.md — Trạng thái bàn giao VuonNha_1

Tài liệu bàn giao giữa Codex và Antigravity.
**Cập nhật: 21/10/2026 · Antigravity** (phân tích toàn bộ mã nguồn, không sửa code)

---

## 1. Trạng thái Git hiện tại

> [!CAUTION]
> **Toàn bộ code Codex đã làm chưa được commit.** Chỉ có 1 commit duy nhất (`775ab38 "First commit"`). Mọi thay đổi từ đó đến nay nằm ở working tree, chưa được Git bảo vệ.

| Loại | Số lượng |
|---|---|
| File modified (M) — đã tracked, chưa commit | **30 file** |
| File untracked (??) — mới hoàn toàn | **~50 file** |
| Commit trong lịch sử | **1** |

### File cần commit (an toàn)

**Tracked files (M) — cần stage & commit:**
```
VuonNha_1/.gitignore
VuonNha_1/app/build.gradle.kts
VuonNha_1/app/src/main/AndroidManifest.xml
VuonNha_1/app/src/main/java/com/example/vuonnha/data/AppDb.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/data/CareDao.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/data/CareRepository.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/data/Entities.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/data/SpeciesDb.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/domain/Schedule.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/notify/ReminderActionReceiver.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/MainActivity.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/common/Components.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/journal/JournalScreen.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/journal/JournalViewModel.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/nav/AppNav.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/plants/PlantsScreen.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/plants/PlantsViewModel.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/reminders/RemindersScreen.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/reminders/RemindersViewModel.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/settings/SettingsScreen.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/settings/SettingsViewModel.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/theme/Color.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/theme/Theme.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/theme/Type.kt
VuonNha_1/app/src/main/java/com/example/vuonnha/ui/weather/WeatherScreen.kt
VuonNha_1/app/src/main/res/values/strings.xml
VuonNha_1/gradle.properties
VuonNha_1/gradle/libs.versions.toml
```

**Untracked — cần thêm vào commit:**
```
VuonNha_1/README.md
VuonNha_1/AGENTS.md                          ← mới tạo hôm nay
VuonNha_1/docs/ARCHITECTURE.md               ← mới tạo hôm nay
VuonNha_1/docs/HANDOFF.md                    ← mới tạo hôm nay
VuonNha_1/backend/                           ← toàn bộ backend (trừ .env, data/)
VuonNha_1/app/src/main/java/.../data/AccountBackupRepository.kt
VuonNha_1/app/src/main/java/.../data/AccountRepository.kt
VuonNha_1/app/src/main/java/.../data/AssistantRepository.kt
VuonNha_1/app/src/main/java/.../data/BackupLimits.kt
VuonNha_1/app/src/main/java/.../data/PlantLookupRepository.kt
VuonNha_1/app/src/main/java/.../data/PlantScanRepository.kt
VuonNha_1/app/src/main/java/.../domain/CareCalendar.kt
VuonNha_1/app/src/main/java/.../ui/assistant/ (cả folder)
VuonNha_1/app/src/main/java/.../ui/common/CareDatePicker.kt
VuonNha_1/app/src/main/java/.../ui/plants/ItemDetailScreen.kt
VuonNha_1/app/src/main/java/.../ui/plants/LivePlantCamera.kt
VuonNha_1/app/src/main/java/.../ui/plants/PlantRecognitionResult.kt
VuonNha_1/app/src/main/java/.../ui/plants/PlantScanScreen.kt
VuonNha_1/app/src/main/java/.../ui/plants/PlantScanViewModel.kt
VuonNha_1/app/src/main/java/.../ui/reminders/CareCalendarScreen.kt
VuonNha_1/app/src/main/java/.../ui/settings/AccountScreen.kt
VuonNha_1/app/src/main/java/.../ui/settings/ScanSettingsCard.kt
VuonNha_1/app/src/main/res/drawable/nav_assistant.xml
VuonNha_1/app/src/main/res/drawable/nav_care.xml
VuonNha_1/app/src/main/res/drawable/nav_garden.xml
VuonNha_1/app/src/main/res/xml/scan_network_security.xml
VuonNha_1/app/src/debug/res/xml/scan_network_security.xml
VuonNha_1/app/src/test/java/.../data/ (BackupLimitsTest, CustomSpeciesTest)
VuonNha_1/app/src/test/java/.../domain/ (CareCalendarTest, ScheduleTest)
VuonNha_1/app/src/androidTest/java/.../AccountBackupTest.kt
VuonNha_1/app/src/androidTest/java/.../AccountFlowTest.kt
VuonNha_1/app/src/androidTest/java/.../BackupTransactionTest.kt
VuonNha_1/app/src/androidTest/java/.../CalendarDiaryTest.kt
VuonNha_1/app/src/androidTest/java/.../CameraToFormTest.kt
VuonNha_1/app/src/androidTest/java/.../EditablePlantProfileTest.kt
VuonNha_1/app/src/androidTest/java/.../GardenFlowAuditTest.kt
VuonNha_1/app/src/androidTest/java/.../GardenNavigationTest.kt
VuonNha_1/app/src/androidTest/java/.../LiveRecognitionAuditTest.kt
VuonNha_1/app/src/androidTest/java/.../NotificationDeliveryAuditTest.kt
VuonNha_1/app/src/androidTest/java/.../PlantScanFormTest.kt
VuonNha_1/app/src/androidTest/java/.../RecognitionFlowTest.kt
```

### File KHÔNG được commit (đã gitignore hoặc nhạy cảm)
```
backend/.env                 ← Gemini API key (đã gitignore ✅)
backend/data/accounts.sqlite ← dữ liệu người dùng (đã gitignore ✅)
backend/data/garden.sqlite   ← chat + snapshot (đã gitignore ✅)
local.properties             ← đã gitignore ✅
app/build/                   ← build output (đã gitignore ✅)
*.log                        ← log files (đã gitignore ✅)
.idea/                       ← IDE config (đã gitignore ✅)
.kotlin/                     ← đã gitignore ✅
anr-form-before.txt          ← ANR trace (nên gitignore)
anr-form-repeat.txt          ← ANR trace (nên gitignore)
```

---

## 2. Công việc Codex đã hoàn thành

Danh sách dựa trên đọc mã nguồn thực tế — không suy đoán.

### ✅ Android App
- Room DB v4: Item, Zone, CareTask, CareLog, DiaryEntry với foreign key CASCADE
- Luồng thêm cây 3 bước (AddSheet) với xem lại trước khi lưu
- Tra cứu loài: nút "Tra cứu" → backend `/plants` → điền profile
- Nhận diện ảnh: CameraX → scan → `PlantRecognitionResult` → AddSheet
- `editedFields`: theo dõi trường người dùng đã sửa, không ghi đè khi lookup
- `manualPlans` vs `suggestedPlans`: tách lịch thủ công và gợi ý
- Lịch chăm sóc: `CareCalendarScreen` với tuần/tháng, projected occurrences
- Nhật ký: ảnh nén JPEG 80% / 1280px max, ghi chú, diary entries
- 4 tab navigation với custom nav bar và XML drawables
- Trợ lý AI: `AssistantScreen` + `AssistantRepository`, snapshot vườn, chat 48h
- Tài khoản: đăng ký/đăng nhập/đăng xuất, AES-GCM session, tùy chọn
- Backup: exportZip/importBackup với transaction, SHA-256, giới hạn 200MB
- Auto-save: debounce 2.5s, conflict detection (409), retry 30s
- WorkManager notifications: `ReminderWorker`, `ReminderActionReceiver`
- Settings: theme, giờ nhắc, scan toggle, xuất/nhập ZIP

### ✅ Backend Node.js
- `server.mjs`: HTTP server, `/identify` (scan), `/plants` (lookup), `/health`
- `accounts.mjs`: scrypt+salt hash, phiên max 7 ngày, backup ZIP per account
- `assistant.mjs`: garden snapshot store, chat handler với Gemini
- `plant-catalog.mjs`: danh mục nội bộ
- `plant-lookup.mjs`: lookup Gemini khi ngoài danh mục, validate profiles
- `provider-fetch.mjs`, `gemini-config.mjs`: abstraction layer
- Docker support: `compose.yaml`, `Dockerfile`
- Test: 37 backend tests với `node --test`

### ✅ Tests
- JVM: 20+ unit tests (Schedule, CareCalendar, BackupLimits, CustomSpecies)
- Android instrumented: 13 test files (navigation, form, profile, recognition, account, backup)
- Backend: 37 tests (server, accounts, assistant, plant-lookup, plant-catalog)

---

## 3. Lỗi còn tồn tại (xác minh từ code)

### P0 — Đổi loài chưa làm mất hiệu lực gợi ý cũ
**File:** `PlantsScreen.kt` — AddSheet, dòng ~297–346

**Vấn đề:** `profile` và `manualPlans` không được reset khi người dùng thay đổi `speciesName` (gõ tên mới sau khi đã tra cứu). Chỉ `suggestedPlans` có `key=sp.id` (reset khi đổi loài từ danh sách). Nhánh tự nhập không có cơ chế tương đương.

```kotlin
// Vấn đề: profile không có key speciesName
var profile by rememberSaveable(stateSaver = ProfileSaver) { mutableStateOf(...) }
// Khi gõ speciesName mới:
OutlinedTextField(speciesName, {
    lookupJob?.cancel(); speciesName = it; matches = emptyList()
    // profile và manualPlans KHÔNG được reset
}, ...)
```

**Hướng xử lý (Codex quyết định):** Cần quyết định UX — tự động xóa profile khi đổi tên? hay hỏi người dùng? Phức tạp vì `editedFields` theo dõi trường đã sửa thủ công.

**Test cần viết:** Đổi speciesName sau khi tra cứu → verify profile/manualPlans được xử lý đúng.

---

### P1 — Validation `valid` kiểm tra trường sẽ không ghi
**File:** `PlantsScreen.kt` — AddSheet, dòng ~324–334

**Vấn đề:** `valid` kiểm tra `profile.values.all { it.length <= 1000 }` — bao gồm cả referenceKeys. Nhưng khi `keepExtra = false`, `submit()` loại bỏ referenceKeys. Người dùng bỏ chọn "Lưu kèm thông tin bổ sung" nhưng mô tả referenceKey dài vẫn chặn lưu.

```kotlin
// Hiện tại:
val valid = ... && profile.values.all { it.length <= 1000 } ...
// Cần sửa thành:
val savedProfile = if (keepExtra) profile else profile.filterKeys { it !in referenceKeys }
val valid = ... && savedProfile.values.all { it.length <= 1000 } ...
```

**Test cần viết:** `keepExtra=false` + referenceKey >1000 ký tự → `valid = true` (cho qua), trường đó không xuất hiện trong profile lưu.

**Antigravity có thể sửa: ✅** (thay đổi nhỏ, không ảnh hưởng kiến trúc)

---

### P1 — Error handling thiếu trong PlantsViewModel
**File:** `PlantsViewModel.kt` — dòng 53–61

**Vấn đề:** 9 hàm dùng `viewModelScope.launch {}` trần không có try/catch. Nếu Room ném exception (disk full, lock timeout, v.v.), app crash hoặc im lặng. Chỉ `addItem()` và `saveEdit()` có error state đầy đủ.

```kotlin
// Không có error handling:
fun removeItem(id: Long) = viewModelScope.launch { repo.removeItem(id) }
fun done(t: CareTask) = viewModelScope.launch { repo.done(t) }
fun snooze(t: CareTask) = viewModelScope.launch { repo.snooze(t) }
fun addTask(...) = viewModelScope.launch { repo.addTask(...) }
fun removeTask(t: CareTask) = viewModelScope.launch { repo.removeTask(t) }
fun editItem(...) = viewModelScope.launch { repo.editItem(...) }
fun addZone(...) = viewModelScope.launch { repo.addZone(...) }
fun removeZone(...) = viewModelScope.launch { repo.removeZone(it) }
fun updateCycle(...) = viewModelScope.launch { repo.updateCycle(...) }
```

**Hướng xử lý:** Thêm `var operationError: String? by mutableStateOf(null)` + try/catch cho mỗi hàm. UI đọc và hiển thị Snackbar/Toast.

**Antigravity có thể sửa: ✅** (không thay đổi signature hay schema)

---

### P1 — UI không đồng đều giữa hai cách chọn cây
**File:** `PlantsScreen.kt` — AddSheet, dòng ~394–471

**Vấn đề:** Nhánh `!custom` (chọn từ danh sách) không có nút "Tra cứu", ô thông tin bổ sung, hay nút "Điền gợi ý". Nhánh `custom` có đầy đủ.

**Hướng xử lý (Codex quyết định):** Cần design decision về UX — loài từ danh sách đã có `sp.tip` và `sp.profileJson`, có cần thêm lookup không? Thay đổi layout đáng kể.

**Antigravity không tự sửa: ⛔** (cần quyết định từ Codex)

---

## 4. Rủi ro cần xử lý trước khi tiếp tục

| Rủi ro | Mức độ | Hành động cần làm |
|---|---|---|
| **Toàn bộ code chưa commit** | 🔴 Cao | Commit ngay trước khi sửa bất cứ thứ gì |
| **ANR trace chưa trong .gitignore** | 🟡 Trung bình | Thêm `anr-*.txt` vào `.gitignore` root |
| **`.idea/caches/deviceStreaming.xml` bị tracked** | 🟡 Trung bình | Đã trong .gitignore nhưng có thể đã staged — kiểm tra lại |
| **`app/src/debug/` chưa commit** | 🟡 Trung bình | Thư mục này chứa `scan_network_security.xml` — cần commit |
| **Backend data/ nếu có file mới** | 🔴 Cao | Đã gitignore, nhưng xác nhận không bị stage nhầm |

---

## 5. Công việc tiếp theo

### Antigravity thực hiện (ưu tiên cao, an toàn)

| # | Việc | File | Test |
|---|---|---|---|
| **AG-1** | Commit baseline sau khi user xác nhận | — | — |
| **AG-2** | Sửa P1 validation `valid` (keepExtra) | `PlantsScreen.kt` | `EditablePlantProfileTest` hoặc test mới |
| **AG-3** | Sửa P1 error handling ViewModel | `PlantsViewModel.kt` + `PlantsScreen.kt` (hiển thị lỗi) | Unit test mới |
| **AG-4** | Thêm `anr-*.txt` vào `.gitignore` root | `.gitignore` | — |

### Codex thực hiện khi quay lại

| # | Việc | File | Ghi chú |
|---|---|---|---|
| **CX-1** | Sửa P0 speciesName reset logic | `PlantsScreen.kt` | Cần quyết định UX |
| **CX-2** | UI consistency nhánh `!custom` | `PlantsScreen.kt` | Cần design decision |
| **CX-3** | Demo end-to-end trên thiết bị thật | — | P0 trước báo cáo |
| **CX-4** | Backend HTTPS hosting | — | P1 trước phát hành |
| **CX-5** | Nhắc việc dài hạn / Doze mode | `ReminderWorker.kt` | P1 |

---

## 6. Lệnh nhanh

```powershell
# Chạy JVM unit tests (không cần thiết bị)
cd VuonNha_1
.\gradlew.bat testDebugUnitTest

# Chạy backend tests (không cần Gemini key)
cd backend
node --test --test-isolation=none

# Build debug APK
.\gradlew.bat assembleDebug

# Check git status
git status --short
git diff --stat HEAD

# Chạy instrumented tests (cần emulator)
& 'C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe' -s SERIAL shell am instrument -w `
  -e class com.example.vuonnha.GardenNavigationTest `
  com.example.vuonnha.test/androidx.test.runner.AndroidJUnitRunner
```

---

## 7. Lịch sử cập nhật

| Ngày | AI | Nội dung |
|---|---|---|
| 21/10/2026 | Antigravity | Tạo file ban đầu: phân tích toàn bộ project, không sửa code |
