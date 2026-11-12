# HANDOFF.md — Trạng thái bàn giao VuonNha_1

Tài liệu bàn giao giữa Codex và Antigravity.
**Cập nhật: 02/11/2026 · Codex** (đóng G1–G4 và ghi nhận bước cài Note9 bị chặn)

---

## 1. Trạng thái Git hiện tại

Repo dự án nằm tại `D:\Hoc_Tap\VuonNha_1\VuonNha_1\.git`. HEAD khi bắt đầu lượt kiểm tra 02/11 là `12f5e66d558e04356365f7bcc60ae6c24ba919c2` (commit tài liệu mục 25); working tree sạch. Commit mã G4 là `a8634276c1327ef5eef02ef525df8fdd20c72691`. G1 `954ca18`, G2 `409c763`, G3 `d9ca332`, G4 `a863427` đã commit riêng: **đóng lô G1–G4 về mã và Gradle**. Lần chạy Gradle sau G4 có 113 JVM test đạt; instrumented đầy đủ vẫn chưa được xác nhận (mục 25). Bản cập nhật handoff này chỉ sửa tài liệu, không stage mã, cấu hình bí mật, dữ liệu người dùng hoặc build output.

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

## 3. Lỗi và giới hạn còn tồn tại — đối chiếu 28/10/2026

### Đã xử lý trong mã

- M1 chính tả Xoá/Huỷ: `9374426`; M2 tiêu đề tra cứu thiếu tên khoa học không hiện `null`: `9eb1b01` (`lookupResultTitle`).
- M3 cảnh báo bỏ nháp AddSheet: `e7bca59`, hoàn thiện ở `8474b1d`. M4 chỉ request tra cứu hiện hành được cập nhật trạng thái/kết quả: `130a960` (`LookupRequestState` và token trong `PlantsScreen.kt`). Lô nhỏ #1 ở mục 20–21: **Đóng, M1–M4 đã có commit**.
- Các lỗi dữ liệu AddSheet/EditSheet/PlantScan được ghi ở mục 14 đã sửa trong các commit riêng. Lịch tự sinh khi tra cứu loài mới và validation EditSheet được cập nhật ở `fbd38ac`; tên vườn theo loài hiện hành ở `1eaa128` và `530cfc9`. Bộ test JVM hiện có chưa thay thế kiểm thử tay trên thiết bị.

### Còn mở hoặc chưa xác minh

1. CameraToFormTest từng thất bại trên SM-N960F; camera đang hoãn theo yêu cầu. Chưa kết luận nguyên nhân.
2. A2 tra cứu ngoài danh mục từng timeout tại backend sau khoảng 25 giây; không lặp lời gọi AI trong lượt này.
3. Backend HTTPS chưa phát hành; đăng nhập và backup qua mạng thật chưa được xác minh.
4. ReminderWorker đã có test chính sách chọn việc (`a8ce397`), còn cần kiểm tra giao thông báo thật và tác động Doze.
5. Nhãn/ô cho cá, thú còn cần quyết định UX. Nhánh chọn từ danh sách vẫn khác nhánh tự nhập về lookup/profile. Chuẩn hóa tên loài NFC/NFD vẫn là backlog.
6. `PlantsViewModel` dùng một chuỗi lỗi chung; lỗi đồng thời có thể ghi đè nhau. Đây là giới hạn đã chấp nhận, chưa có correlation ID.
## 4. Trạng thái kiểm chứng và giới hạn

- Các số liệu ở mục này là lịch sử lượt kiểm chứng 25/10, khi HEAD còn `0b2eb75`; hiện trạng Git ngày 28/10 nằm ở mục 1 và 23.
- Lượt 25/10 có 48 JVM test pass, 0 fail/error/skip; assembleDebug và lintDebug thành công, lint 0 error và 40 warnings. Kết quả mới hơn ghi ở mục 22–23.
- Instrumented CameraToFormTest có 1 failure tại assertion camera release nêu ở mục 3; chưa kết luận nguyên nhân.
- Live lookup có 1 lần gọi AI, trả lỗi timeout 504 phía app; log provider-safe được ghi ở mục 3. Không lặp lại request.
- Không kiểm tra/đọc nội dung cấu hình bí mật hoặc backend/data; không đổi schema/API/dependency và không push.
- operationError là state dùng chung; một lỗi mới có thể thay thông báo đang chờ, theo yêu cầu chấp nhận được. Đây chưa phải xử lý theo từng thao tác.

---

## 5. Công việc tiếp theo

M1–M4 đã đóng (mục 3 và 20–21); xác nhận xóa việc trong EditSheet hiện có trong `PlantsScreen.kt`. Lượt 28/10 tập trung build và các lớp instrumented an toàn trên Note9, ghi kết quả thật ở mục 23. Các luồng còn lại cần quyết định hoặc môi trường kiểm chứng riêng.

### Việc còn mở

1. Camera: hoãn; chỉ điều tra CameraToFormTest khi người dùng yêu cầu tiếp.
2. A2 timeout tra cứu: cần bằng chứng mới trước khi thử lại AI.
3. Backend HTTPS: phát hành và kiểm tra vận hành riêng.
4. `ReminderWorker`/Doze: kiểm tra thông báo thật, giờ nhắc và độ trễ trên thiết bị.
5. Đăng nhập và backup qua mạng thật: dùng môi trường thử riêng, tránh dữ liệu tài khoản/vườn thật.
6. Đổi Gemini key và `SCAN_TOKEN`: người dùng tự thực hiện theo quy trình bảo mật.
7. Nhãn cá/thú và các ô chỉ phù hợp cây: cần quyết định UX trước khi sửa.

Không coi build/JVM pass là bằng chứng cho các luồng chưa thử tay. Không gọi AI trong lượt kiểm chứng này.
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
| 23/10/2026 | opencode | Bước 1 P0: tách hàm thuần `clearSpeciesDerivedData` + 7 unit test, nối vào `AddSheet`, làm sạch lúc review |

---

## 8. Cập nhật 23/10/2026 — Bước 1 P0 (đã nối UI, làm sạch lúc review)

**Đã làm:**
- Tạo `app/src/main/java/com/example/vuonnha/ui/plants/SpeciesChange.kt` chứa `SpeciesChangeResult` và hàm thuần `clearSpeciesDerivedData(profile, editedFields, referenceKeys, oldSpeciesName, newSpeciesName)`.
- Quy ước: nếu tên loài không đổi, trả nguyên `profile`/`editedFields`. So sánh tên loài bỏ khoảng trắng đầu/cuối và không phân biệt hoa/thường. Nếu đổi, xóa các key trong `referenceKeys` trừ key người dùng đã sửa tay (`editedFields`); giữ `location`, `actualLight`, `pottingMix` và mọi key ngoài `referenceKeys`. `editedFields` sau xử lý chỉ còn key có trong profile.
- `REFERENCE_KEYS` là hằng top-level `internal` tại `PlantsScreen.kt:55`: `scientificName`, `cultivar`, `actualHeight`, `matureHeight`, `humidity`, `soil`, `light`, `source`. `actualHeight` mô tả chính cây đang trồng; `cultivar` là trường hợp biên nhưng hiện xếp vào dữ liệu tham khảo. `location`, `actualLight`, `pottingMix` không nằm trong `REFERENCE_KEYS` nên luôn được giữ.
- Thêm `app/src/test/java/com/example/vuonnha/ui/plants/SpeciesChangeTest.kt` với 7 test: cùng loài, cùng loài khác hoa/thường và khoảng trắng, đổi loài sau khi trim vẫn xóa, xóa field tra cứu, giữ field môi trường, giữ field sửa tay, dọn `editedFields` mồ côi.
- `derivedFor` (`PlantsScreen.kt:304`) theo dõi loài đã sinh ra profile hiện tại. Helper `clearStaleSpeciesData` (`:316-318`) bỏ qua khi `derivedFor` trắng và luôn truyền `REFERENCE_KEYS - "actualHeight"` (`:315`).
- Làm sạch đúng một lần khi mở hộp thoại Xem lại (`:600-606`): tính theo `speciesName` nếu `custom`, ngược lại theo `sp.name`, gán lại `profile`, `editedFields`, `derivedFor` trước khi `reviewing = true`. `submit()` (`:333-334`) chỉ lưu profile hiện tại, không làm sạch lại.
- Chọn kết quả lookup (`:349-353`, `:454-459`) và chọn loài từ dropdown (`:410-413`) vẫn làm sạch ngay tại thời điểm chọn. Không làm sạch trong `onValueChange` của `speciesName`.
- Chip nhóm (`:423-431`) chỉ làm sạch `speciesDerivedKeys` khi nhóm thực sự đổi; giữ `location`, `actualLight`, `pottingMix`, `actualHeight`; không còn `profile = emptyMap()`.
- Không đổi `manualPlans`, `EditSheet`, lookup repository, Room schema, API, dependency hay test hiện có.

**Lệnh đã chạy (workdir `VuonNha_1`):** `.\gradlew.bat testDebugUnitTest assembleDebug lintDebug` — BUILD SUCCESSFUL; `SpeciesChangeTest` 7/7 đạt, 0 failure/error; APK debug tạo thành công; lint `0 errors, 40 warnings` (warning có sẵn).

**Chưa chắc chắn:** Giữ field tra cứu đã sửa tay khi đổi loài vẫn là giả định UX. Với bottom sheet không có bước Xem lại, việc làm sạch chỉ xảy ra khi chọn lookup/dropdown hoặc đổi nhóm, đúng theo yêu cầu "chỉ một lần lúc reviewing = true". Chưa có instrumented test tự động cho luồng đổi loài.

**Manual test trên thiết bị (bắt buộc trước khi chốt):**
1. Tạo cây mới → nhập tên loài → bấm tra cứu → chọn một kết quả; nhập `Vị trí`, `Ánh sáng nơi trồng`, `Chậu & giá thể`, sửa tay `Chiều cao hiện tại`; lưu. Mở lại cây, xác nhận các field tham khảo và field môi trường đều được lưu.
2. Tra cứu loài A (VD Kim tiền), chọn kết quả; sau đó tra cứu loài B (VD Cây táo) và chọn kết quả. Xác nhận field tham khảo của A (`scientificName`, `source`, `humidity`, `soil`, `light`, `matureHeight`…) không còn, còn `Vị trí`, `Ánh sáng nơi trồng`, `Chậu & giá thể` và `Chiều cao hiện tại` vẫn giữ.
3. Tra cứu A và chọn kết quả; sửa nhẹ tên loài sang B nhưng **không** bấm lookup; bấm Tiếp tục để mở hộp thoại Xem lại. Hộp thoại phải hiển thị profile đã làm sạch (không còn field tham khảo của A, vẫn có field môi trường và `actualHeight`). Xác nhận lưu rồi mở lại cây: dữ liệu đã lưu phải khớp đúng những gì hộp thoại Xem lại hiển thị.
4. Ở chế độ "Chọn từ danh sách", chọn loài A rồi chọn loài B. Xác nhận dữ liệu tham khảo của A bị xóa, field môi trường/`actualHeight` còn nguyên.
5. Đổi chip `Nhóm` sang nhóm khác rồi quay lại; chỉ dữ liệu tham khảo của loài bị xóa, `Vị trí`, `Ánh sáng nơi trồng`, `Chậu & giá thể`, `Chiều cao hiện tại` còn nguyên. Bấm lại chip đang chọn không được xóa gì.
6. Tra cứu A, sửa tay một field tham khảo (VD `light`), rồi đổi sang B. Xác nhận giá trị sửa tay vẫn còn và lookup mới không ghi đè field đó.
7. Tra cứu A rồi gõ thêm/bớt ký tự trong ô tên loài nhưng chưa mở Xem lại/chưa lưu. Xác nhận thông tin bổ sung không bị xóa ngay mỗi lần gõ.
8. Đặt một việc chăm tay (tên + chu kỳ + ngày), sau đó tra cứu/đổi loài. Xác nhận việc chăm tay không đổi; chỉ lịch gợi ý giữ hành vi cũ.
9. Tạo cây mới hoàn toàn (không lookup, `derivedFor` trắng), nhập field môi trường và lưu. Xác nhận không có field nào bị xóa bất thường.
10. Từ kết quả quét AI, đổi tên loài sang loài khác rồi mở Xem lại và lưu. Xác nhận field gợi ý cũ của AI bị xóa, field môi trường và `actualHeight` vẫn còn.

---

## 9. Backlog đang mở

- Nhánh built-in/custom=false chưa có lookup và editor profile tương tự nhánh tự nhập; cần quyết định UX.
- EditSheet thêm/xóa task chưa có xác nhận.
- Lookup có race cờ looking/kết quả cũ; cần test chậm có kiểm soát.
- AddSheet không cảnh báo hoặc giữ draft khi đóng.
- Một số UI có thể hiện chuỗi null khi thiếu scientificName.
- Chuẩn hóa tên loài NFC/NFD; lookup chưa injectable.
- manualPlans chưa lưu nguồn gốc riêng; tên gọi trong vườn có thể chưa theo tên loài sau lookup lại.
- CameraToFormTest fail ở assertion camera client release trên SM-N960F; cần điều tra.
- Live lookup đã timeout một lần ngày 25/10/2026; không lặp request khi chưa có bằng chứng mới.
## 10. Đối chiếu nhận xét bên ngoài — 23/10/2026 · Codex

Phạm vi: người dùng yêu cầu chỉ đọc và góp ý, chưa sửa ứng dụng. Không đổi mã nguồn, schema, API, dependency, test hoặc Git; chỉ bổ sung bàn giao này theo quy tắc workspace. Các thay đổi working tree đã có trong PlantsScreen.kt và HANDOFF.md được giữ nguyên.

Đã đọc nhận xét người dùng đính kèm, hướng dẫn workspace/project, README, backend/README, ARCHITECTURE và HANDOFF; đối chiếu AddSheet/EditSheet, CareRepository.addItem/saveItemEdit, PlantLookupRepository, PlantScanScreen/PlantScanViewModel, SpeciesChange và bảy test helper.

Đã đọc trực tiếp các source member được chỉ định trong VuonNha_1-main.7z bằng 7-Zip, không giải nén toàn bộ và không đọc cấu hình bí mật. PlantsScreen.kt trong archive khác working tree, chưa có helper đổi loài/derivedFor; CareRepository.kt và PlantScanScreen.kt trùng nội dung sau chuẩn hóa xuống dòng. Vì vậy không dùng nhận xét helper của bản mới để khẳng định archive đã được sửa.

Các điểm nên sửa trước, xác định từ đường xử lý trong mã, chưa chạy tái hiện trên thiết bị trong lượt này:

- Nhánh chọn danh sách: xóa hết suggestedPlans không xóa sp.tasks; submit vẫn gửi sp, repository tạo task từ sp.tasks. Cần thống nhất danh sách việc đã duyệt với Species.tasks được gửi lưu; không đổi mặc định của repository chỉ dựa trên cycles rỗng vì có caller đang dựa vào mặc định đó.
- EditSheet: cycles có thể giữ ID đã xóa, khiến saveItemEdit từ chối ở mọi lần thử sau. Cần kiểm thử đổi chu kỳ → xóa cùng việc → lưu các sửa còn lại.
- Nhánh cá/thú tự nhập: submit bỏ toàn bộ profileJson dù bước vị trí và xem lại vẫn hiển thị dữ liệu. Cần lưu các trường người dùng đã nhập phù hợp hoặc không trình bày chúng như sẽ lưu.
- Nhận diện: AddSheet bỏ gợi ý/đổi loài nhưng caller vẫn có thể truyền draft gốc vào savePlant để tạo ghi chú ảnh. Cần truyền trạng thái gợi ý còn hiệu lực, không chỉ làm sạch profile.
- Mở xem lại đang ghi đè profile/editedFields/derivedFor, nên Chỉnh lại không khôi phục được gợi ý đã dọn. Nên tạo bản dự kiến lưu riêng, không làm mất draft chỉ vì mở xem lại.
- keepExtra=false vẫn bị validation của toàn bộ profile chặn. Nên validation, review và submit cùng dùng một bản dữ liệu dự kiến ghi.

Cần chốt UX hoặc có bằng chứng bổ sung:

- Ô đã sửa/xóa trắng được giữ nguyên là hành vi đã thống nhất và có test, không tự gọi là lỗi. Có thể bổ sung thao tác Điền lại gợi ý rõ ràng; không tự khôi phục giá trị khi đổi focus.
- Hủy HttpURLConnection và cờ looking có rủi ro tranh chấp giữa các job; cần test lookup giả có độ trễ, kiểm tra request ID/job sở hữu trạng thái. Không suy từ đọc mã thành mọi kết quả cũ chắc chắn sẽ ghi đè hoặc disconnect sẽ hoàn lại quota đã tiêu thụ.
- Cần phân biệt nguồn của tên riêng/lịch tự nhập/lịch gợi ý khi đổi loài; giữ các sửa do người dùng chủ động thực hiện.
- Chuẩn hóa Unicode NFC và khoảng trắng là hợp lý, nhưng hai tên khác chuỗi không nhất thiết khác loài; không bỏ dấu tiếng Việt hoặc coi so sánh chuỗi là định danh phân loại đáng tin.
- fullScreen=false chưa được caller sản phẩm dùng không có nghĩa nên xóa ngay; kiểm tra các test/caller trước khi thống nhất hoặc loại nhánh.

Lệnh đọc/kiểm tra: Get-Content, rg, git -C VuonNha_1 status/diff/log và 7z l/e -so trên đúng source member. Không chạy Gradle/backend/AI, không cài APK, không commit/push. Git project thực tế có HEAD d377294 và commit trước 69ca200; mô tả chỉ có 775ab38 ở phần đầu HANDOFF không đại diện lịch sử của repo lồng hiện tại.

Đề xuất bước sau (chưa thực hiện): thêm test tái hiện sáu lỗi dữ liệu trên trước; sửa từng nhóm nhỏ, giữ kiến trúc/contract và test đã đạt. Sau đó xử lý nguồn gốc gợi ý và lookup bất đồng bộ; cuối cùng mới bổ sung chuẩn hóa và tinh gọn bố cục.

## 11. Bước 0 — chốt phiên bản và baseline, 23/10/2026 · Codex

Chưa sửa source/test, chưa commit/push, chưa trộn archive. Chỉ cập nhật bàn giao này sau kiểm tra. Working tree đầu lượt đã có PlantsScreen.kt (+25/-10 so với HEAD) và HANDOFF.md thay đổi; giữ nguyên các thay đổi đó. Repo thực tế: VuonNha_1/.git, HEAD d377294 (23/10 17:36:34 +07), trước đó 69ca200 (16:57:50), 1d026c9 (11:40:42). Yêu cầu mới của người dùng cho phép một commit riêng mỗi lỗi sau khi sửa/test; chưa thực hiện ở Bước 0.

Đối chiếu VuonNha_1-main.7z (44.553.601 byte, mtime 23/10 19:56:59) bằng 7z l và đọc đúng member, không giải nén đè. Không thấy .git trong archive. Phạm vi so sánh source/resource app/src, backend code/test/script, Gradle, hướng dẫn/tài liệu; loại cấu hình bí mật, build/cache và dữ liệu người dùng. Trong 112 file archive thuộc phạm vi: 90 giống (cho phép khác CRLF/LF), 14 khác, 8 chỉ có trong archive; working tree có thêm 7 file. Không đọc .env/local.properties; không in giá trị key/token.

- AppNav.kt chỉ khác thụt lề khi so dòng đã trim; không thấy thay đổi nội dung dòng.
- ItemDetailScreen.kt: archive đổi tiêu đề theo nhóm cá/thú; working tree luôn ghi Thông tin cây.
- PlantsScreen.kt: archive có câu chữ theo nhóm và lưu riêng location cho nhóm không phải cây; chưa có derivedFor/clearSpeciesDerivedData. Working tree có helper đổi loài và WIP làm sạch ngay khi mở xem lại. Archive file mtime 15:54:18, working file mtime 17:39:33 cùng ngày; không thể suy toàn bộ bản nào mới hơn từ giờ đóng gói.
- 11 file biểu tượng/background hiện có khác nhau; archive thêm 8 asset icon/adaptive icon, working tree có hai adaptive icon ở mipmap-anydpi thay vì mipmap-anydpi-v26.
- 7 file chỉ có ở working tree: SpeciesChange.kt, SpeciesChangeTest.kt, hai mipmap-anydpi XML, AGENTS.md, docs/ARCHITECTURE.md, docs/HANDOFF.md.
- Kết luận: hai nhánh nội dung có thay đổi độc lập; archive đã có đoạn liên quan lỗi 4 nhưng chưa chạy test cho archive. Không khẳng định lỗi 4 đã được xử lý đầy đủ ở archive. WIP hiện có chồng vào lỗi 5/6. Chờ người dùng xác nhận dùng nguyên working tree (HEAD + WIP) làm nền; không tự lấy sửa đổi từ archive.

Baseline trên working tree, trước sửa source:

| Kiểm tra | Kết quả |
|---|---|
| Backend: node --test --test-isolation=none | 37 pass, 0 fail, 0 skip |
| Gradle testDebugUnitTest --rerun-tasks --offline | 30 pass, 0 failure/error/skip; đã chạy thực sự, không chỉ UP-TO-DATE |
| assembleDebug, assembleDebugAndroidTest, lintDebug --offline | BUILD SUCCESSFUL; lint 0 error, 40 warning |
| Android instrumentation mặc định, Pixel_6_Audit / emulator-5554 | 32 được runner liệt kê: 16 pass, 7 fail, 9 opt-in skip; chưa đạt baseline giao diện sạch |

Android: khởi động AVD hiện có bằng -no-window -no-audio -no-snapshot-save, không wipe; lượt cài đầu khi chưa boot xong thất bại, sau sys.boot_completed=1 cài cả hai APK thành công. Chạy am instrument -w, không bật accountAudit/flowAudit/liveAudit/notificationAudit, không gọi AI thật và không dùng điện thoại. Runner báo FAILURES dù lệnh adb kết thúc exit 0; kết luận dựa trên kết quả runner.

7 test fail baseline:
- CameraToFormTest.liveCameraToManualFormAndKeyboardRemainsResponsive: không tìm thấy contentDescription Quét cây bằng máy ảnh (test dòng 15); entry hiện tại là DropdownMenuItem Quét cây ở PlantsScreen.kt:117. Chưa sửa test/caller.
- Cả 4 test EditablePlantProfileTest, PlantScanFormTest.invalidSpeciesExplainsWhySaveIsUnavailableAndCanBeCorrected, RecognitionFlowTest.repeatedKeyboardInputAndGroupSwitchesKeepFormResponsive: RootViewWithoutFocusException khi Espresso.closeSoftKeyboard. dumpsys window tại thời điểm chạy xác nhận mCurrentFocus là Application Not Responding: com.android.systemui, app test không có window focus. Bấm Back thử không loại được hộp thoại. Đây là bằng chứng môi trường chặn kiểm thử; chưa kết luận chức năng tương ứng của app đúng hay sai. Cần xử lý môi trường rồi chạy lại trước khi khẳng định không hồi quy.

| Lỗi | Trạng thái |
|---|---|
| 1. Đặt lịch sau vẫn tạo task danh sách | Chưa — chờ chốt phiên bản |
| 2. Đổi chu kỳ rồi xóa task không lưu được | Chưa — chờ chốt phiên bản |
| 3. Bỏ AI vẫn ghi nhận diện cũ vào nhật ký | Chưa — chờ chốt phiên bản |
| 4. Cá/thú tự nhập mất vị trí | Chưa — chờ chốt phiên bản |
| 5. Xem lại làm mất draft khi Chỉnh lại | Chưa — chờ chốt phiên bản; WIP đang chồng phạm vi |
| 6. Không lưu thông tin bổ sung vẫn bị validate | Chưa — chờ chốt phiên bản |

Đánh giá sơ bộ dữ liệu cũ: sáu lỗi chưa cho thấy cần đổi schema. Chưa kiểm tra dữ liệu thật; thông tin vị trí đã bỏ khi lưu thường không thể tái dựng, ghi chú AI cũ/lịch sinh dư không thể tự xác định ý định người dùng. Chưa đề xuất hay chạy migration/sửa dữ liệu. Sẽ đánh giá lại riêng từng lỗi sau tái hiện.

Bước tiếp theo: người dùng xác nhận working tree hiện tại là bản nền; sau đó ổn định môi trường test và xử lý lần lượt 1→6 theo FAIL trước/PASS sau, commit riêng từng lỗi. Không làm phần provenance/async/refill/Unicode trước khi hỏi lại sau sáu lỗi.

### Làm rõ nguồn tham khảo sau Bước 0

Người dùng xác nhận luồng chính là thư mục làm việc; .7z là bản bạn của người dùng sửa để tham khảo, cần đánh giá chọn lọc, logo thiết kế sau. Không còn chờ chọn giữa hai phiên bản; sáu lỗi vẫn chưa bắt đầu sửa. Không nhập cả file từ archive và không đưa asset logo vào phạm vi hiện tại.

Đã đối chiếu lại từng dòng khác của PlantsScreen.kt, AppNav.kt, ItemDetailScreen.kt với archive và đọc đường lưu profile tại CareRepository.addItem. Khuyến nghị:
- Nên lấy ý tưởng giữ location cho cá/thú vào bản sửa lỗi 4, nhưng viết sửa tối thiểu trên working tree, có test tái hiện trước. Repository đã ghi s.profileJson; điểm loại dữ liệu nằm ở AddSheet.submit (PlantsScreen.kt:335-336), không cần sửa schema để lưu vị trí mới. Chưa chạy test trên đoạn archive.
- Câu chữ/ẩn mục ánh sáng và giá thể theo nhóm có ích nếu tiếp tục hỗ trợ cá/thú; để đợt UX riêng sau sáu lỗi nhằm giữ phạm vi. Chưa hoàn chỉnh: archive đổi nhãn vị trí nhưng ví dụ vẫn là ban công/cửa sổ, EditSheet chỉ đổi tiêu đề mà các nhãn chuyên cây còn nguyên.
- AppNav.kt chỉ thay thụt lề, không cần đưa vào; không có bằng chứng bản nén sửa điều hướng.
- Không lấy nguyên PlantsScreen.kt vì sẽ bỏ derivedFor/helper làm sạch dữ liệu đổi loài của working tree; handler đổi nhóm trong archive vẫn profile = emptyMap(), có thể xóa vị trí/giá trị nhập tay ngay cả khi bấm lại nhóm đang chọn. Đây là đường xử lý đọc từ mã, chưa chạy tái hiện.
- Bản nén vẫn dùng sp.copy không cập nhật tasks theo plans, vẫn validation toàn bộ profile, vẫn caller lưu ghi chú nhận diện như cũ và cùng logic EditSheet cycles. Không coi đây là bản sửa toàn bộ sáu lỗi. Cách mở review ở archive không làm sạch draft chỉ vì chưa có cơ chế đổi loài của working tree; không thể chép lại để xử lý lỗi 5 mà bỏ luôn cơ chế hiện có.

Lượt làm rõ này chỉ đọc/đối chiếu và cập nhật HANDOFF; không sửa code/test, không chạy lại baseline khi chưa đổi code, không commit/push. Bước sau: tiếp tục sáu lỗi trên working tree đã chốt, theo thứ tự; chỉ tham khảo ý tưởng phù hợp từ archive.

## 12. Cài bản hiện tại lên Note9 — 24/10/2026 · Codex

Theo yêu cầu người dùng muốn xem lại ứng dụng: build từ working tree đã chốt, không nhập archive và không sửa source/test. Chạy assembleDebug --offline (JAVA_HOME Android Studio jbr, GRADLE_USER_HOME D:/Android/gradle): BUILD SUCCESSFUL trong 41 giây. Cài app-debug.apk lên Note9 SM-N960F bằng adb install -r: Success, không gỡ app/xóa dữ liệu. Mở com.example.vuonnha/.ui.MainActivity; lần chờ mở đầu kéo dài, lệnh mở lại xác nhận mResumedActivity đúng app và am start -W trả Status: ok.

Backend ban đầu không chạy; khởi động Node bằng --env-file=.env server.mjs với cửa sổ ẩn, không đọc/in cấu hình bí mật; /health trả HTTP 200. Thiết lập adb reverse tcp:8787 tcp:8787 riêng Note9, reverse --list xác nhận ánh xạ. Health chỉ xác nhận server phản hồi, chưa chứng minh API AI/key/quota hoạt động. Chưa gọi API AI, chưa test lại chức năng; sáu lỗi vẫn chưa sửa. Giữ backend chạy để người dùng thử qua USB. Chỉ cập nhật HANDOFF, không commit/push.

## 13. Kiểm thử trên Note9 — 24/10/2026 · Codex

Người dùng yêu cầu kiểm thử ngay trên điện thoại đã kết nối. Cài test APK hiện có bằng install -r, giữ app/data; không sửa source/test, không commit/push. Backend /health HTTP 200, adb reverse 8787 đã nối.

Chạy runner với danh sách class BackupTransactionTest, CalendarDiaryTest, EditablePlantProfileTest, ExampleInstrumentedTest, GardenNavigationTest, PlantScanFormTest, RecognitionFlowTest: OK (22 tests), 43,113 giây. Phạm vi: 4 giao dịch Room bằng DB in-memory; lịch/ngày với cây kiểm thử riêng và finally xóa đúng cây đó; nhập/chỉnh/xóa gợi ý, khôi phục trạng thái form, validation, kết quả nhận diện giả → form; ba bài điều hướng gồm tài khoản không đăng nhập, vòng Chăm sóc↔Vườn Nhà và các tab. Không gửi chat vào tài khoản người dùng. Các bài form dùng callback kiểm tra, không đồng nghĩa toàn bộ luồng đã lưu xuống DB. Test điều hướng có sinh năm ảnh kiểm thử ở externalFilesDir ứng dụng. Không gặp lỗi focus của máy ảo; dumpsys activity lastanr không ghi nhận ANR từ lúc boot. Không kết luận tuyệt đối không có treo máy từ kết quả này.

Chạy riêng LiveRecognitionAuditTest#nameOutsideCatalogCanBeLookedUpWithoutSavingGardenData với liveAudit=true: FAIL (1 test), 25,145 giây; tra cứu cây táo qua PlantLookupRepository báo Tra cứu đang mất nhiều thời gian. Đây là nhánh HTTP 504 tại PlantLookupRepository.kt:21. Backend plant-lookup.mjs:18 đặt AbortSignal.timeout(25000), provider-fetch.mjs:4 ánh xạ TimeoutError→504. Bằng chứng phù hợp timeout phía backend/provider; chưa phân biệt nguyên nhân mạng tới provider, độ trễ dịch vụ hay cấu hình model, không suy thành key sai/hết quota. Không chạy lại để tiêu quota khi chưa có thay đổi hoặc bằng chứng mới. Không lưu cây hay ảnh từ bài tra cứu này.

Chưa kiểm thử camera thật/độ chính xác nhận diện, đăng ký/đăng nhập/khôi phục tài khoản thật, trợ lý AI thật, thông báo theo giờ hoặc các tình huống sáu lỗi đã chốt. Không chạy CameraToFormTest vì selector nút đã lỗi ở baseline; không chạy audit cần vườn trống trên dữ liệu người dùng. Sáu lỗi vẫn chưa sửa, 22 test đạt không phủ kín chúng. Mở lại MainActivity cho người dùng sau kiểm thử.

Bước sau: sửa sáu lỗi theo thứ tự đã chốt; điều tra timeout tra cứu riêng, bắt đầu từ kết nối provider/cấu hình không bí mật và phản hồi chậm có kiểm soát. Không tự tăng timeout rồi coi là sửa xong.

## 14. Sửa sáu lỗi dữ liệu và ba việc phụ — 24/10/2026 · Codex

Theo yêu cầu mới: thứ tự 1,2,3,4,6,5 rồi a,b,c; mỗi mục một commit, không push. Giữ nguyên working tree làm nền; bản gốc trước lượt sửa lưu ở .audit/fix-baseline/PlantsScreen.kt để tách thay đổi đã có khỏi commit. Không thay schema/API/dependency; không đọc cấu hình bí mật hoặc backend/data.

| Mục | Trạng thái / bằng chứng |
|---|---|
| 1 | Xong: PlantsScreen.kt:337 gửi sp.tasks thay vì plans; tách ReviewedTasks.kt và dùng species.copy(tasks=plans). ReviewedTasksTest: 2 FAIL đúng assertion trước sửa, sau sửa toàn bộ JVM 32 pass. Repository giữ nguyên mặc định. Test JVM xác minh chính payload tạo task; kiểm thử DB/UI thực sẽ bổ sung cuối lượt. |
| 2 | Xong: EditSheet giữ ID đã xóa trong cycles (:661, :690, :710 cũ). Lọc payload bằng PendingCycles.kt theo tasks hiện có, giữ repository nghiêm ngặt. PendingCyclesTest: 2/3 FAIL trước, sau sửa toàn bộ JVM 35 pass. Nếu thao tác xóa còn đang chạy, repository vẫn có quyền từ chối do race; thử lại sau khi task Flow cập nhật sẽ lọc đúng ID, không giữ lỗi vĩnh viễn. Không đổi schema/dữ liệu cũ. |
| 3 | Xong: AddSheet bỏ usingAi nhưng PlantScanScreen:100 vẫn truyền draft gốc; callback onAiSuggestionDiscarded thông báo parent, candidateForSave loại draft bị bỏ/khác tên loài, reset derivedFor/editedFields khi bỏ. PlantScanViewModel dùng recognitionDiaryNote để ảnh thủ công chỉ có chú thích trung tính. ScanAttributionTest: 2/3 FAIL trước, sau sửa JVM 38 pass. Ảnh đầu tiên vẫn được lưu theo hành vi cũ, chỉ loại thông tin AI sai; chưa chạy camera thật. |
| 4 | Xong: nhánh custom group != PLANT ghi profileJson rỗng (PlantsScreen.kt:336 cũ). ProfileToSave.kt giữ các field môi trường/vị trí cho mọi nhóm, loại REFERENCE_KEYS khi cá/thú hoặc bỏ lưu tham khảo. ProfileToSaveTest: mất location FAIL đúng assertion trước sửa; cả hai test và toàn bộ JVM 40 pass sau sửa. Lượt đầu trích helper bị lỗi compile do gọi hàm speciesGroupOf không có trong bản chính; đã bỏ tham chiếu đó trước khi lấy FAIL hợp lệ. Built-in giữ chính sách profile trước đây; phạm vi lỗi là cá/thú tự nhập. Không cần schema migration; vị trí đã mất ở dữ liệu cũ không thể khôi phục tự động. |
| 6 | Xong: valid/canContinue và nút bước cuối kiểm tra toàn bộ profile (:330-332, :592 cũ) trong khi submit loại REFERENCE_KEYS. Dùng profileToSave/profileIsValidForSave chung cho validation, review và submit; review không hiện nội dung đã bỏ lưu. ProfileValidationTest: 2/3 FAIL trước, sau sửa toàn bộ JVM 43 pass; thử mọi REFERENCE_KEYS, ba ô môi trường, ranh giới 1000/1001 và mọi nhóm. Không migration. |
| 5 | Xong: WIP mở review ghi đè profile/editedFields/derivedFor (:601-604 cũ). PlantReview.kt tính projection riêng; mở/đóng chỉ đổi reviewing, submit lưu chính review.profile. PlantReviewTest: 1/3 FAIL trước đúng draft bị đổi, sau sửa toàn bộ JVM 46 pass. Bao phủ giữ ba state, loại gợi ý cũ khỏi payload, giữ môi trường và bỏ tham khảo. Không migration; draft đã mất ở phiên cũ không tự phục hồi. WIP chọn nhóm được giữ ngoài commit; WIP submit/review được thay bởi bản sửa lỗi này. |
| a | Xong, commit riêng `436495a`: test mở menu Tiện ích → Quét cây theo UI hiện tại. Chưa chạy instrumented test riêng trong lượt này. |
| b | Mã/test xong, commit riêng `90646a0`: log chẩn đoán an toàn, timeout 504 và lỗi provider 429/503 có test. Audit AI một lượt chưa chạy vì adb chỉ thấy thiết bị `unauthorized`, server 8787 không hoạt động; không gửi request. |
| c | Xong, commit riêng `07cd1d2`: `.gitignore` bỏ qua `anr-*.txt`; mục 1 handoff đồng bộ Git. |

Dữ liệu cũ lỗi 1: không đổi schema; lịch tạo dư chỉ có thể dọn sau người dùng xác nhận vì không có cờ phân biệt lịch họ muốn giữ. Không tự migration/sửa dữ liệu.

### Việc phụ a — CameraToFormTest

Đã cập nhật entry point của test từ content description cũ sang menu Tiện ích → Quét cây, khớp PlantsScreen hiện tại. Đối chiếu trực tiếp selector với UI; chưa chạy instrumented test trong bước này. Đã commit riêng 436495a.

### Việc phụ b — log lỗi tra cứu/nhận diện

Đã thêm một dòng JSON an toàn khi lỗi provider ở /plants và /identify: route, errorName, httpStatus, elapsedMs, model; không ghi thông điệp lỗi/request/header/ảnh/token/key. Bổ sung logImpl để test cô lập và test /plants giả lập TimeoutError → 504, provider 429/503 → đúng mã. 
node --test --test-isolation=none tại backend: 38 pass, 0 fail. Chưa chạy LiveRecognitionAuditTest: thiết bị adb đang unauthorized, không có server 8787; cần kết nối/ủy quyền lại thiết bị rồi chạy đúng một lượt AI. Không đổi timeout/model.

### Kết quả cuối lượt — 25/10/2026

- .\gradlew.bat testDebugUnitTest assembleDebug lintDebug: BUILD SUCCESSFUL (5m53s). Unit test report: 46 pass, 0 fail, 0 error, 0 skipped; task test up-to-date. APK debug build thành công. Lint: 0 errors, 40 warnings.
- node --test --test-isolation=none tại backend/: 38 pass, 0 fail, 0 skipped.
- LiveRecognitionAuditTest#nameOutsideCatalogCanBeLookedUpWithoutSavingGardenData chưa chạy: adb devices báo thiết bị 271c3b59c41c7ece là unauthorized, không có thiết bị/emulator đã ủy quyền; cổng 8787 cũng chưa có server. Không khởi chạy thêm AI call. Sau khi người dùng ủy quyền lại thiết bị, chạy test này đúng một lần và bổ sung kết quả vào đây.
- CameraToFormTest đã sửa selector nhưng chưa chạy instrumented.
- Hiện tại HEAD 07cd1d2, 16 commit; có hai file modified chưa commit gồm PlantsScreen.kt (WIP chọn nhóm từ trước nhiệm vụ) và HANDOFF.md (kết quả lượt này). Không đưa WIP PlantsScreen.kt vào commit.

## 15. Yêu cầu cho Codex — 25/10/2026 · Claude (chat)

Claude đã sửa trực tiếp `PlantsViewModel.kt` (chưa build, chưa test, chưa commit): thêm `operationError`, `clearOperationError()` và hàm bọc `guarded(message, block)`; 9 hàm `editItem`, `removeItem`, `updateCycle`, `addTask`, `removeTask`, `done`, `snooze`, `addZone`, `removeZone` giờ bắt lỗi Room (vẫn ném lại `CancellationException`). Chữ ký hàm và kiểu trả về `Job` không đổi. Claude không chạy được Gradle/adb nên cần Codex kiểm chứng và làm tiếp.

Quy tắc chung: không đổi schema/API/dependency; không đọc hoặc in cấu hình bí mật, `backend/data/`; không push; mỗi việc một commit riêng; FAIL trước, PASS sau khi viết test; giữ nguyên WIP chọn nhóm trong `PlantsScreen.kt` ngoài commit trừ khi việc 2 buộc phải đụng (xem dưới).

**Việc 1 — Kiểm chứng bản sửa ViewModel**
- Chạy `.\gradlew.bat testDebugUnitTest assembleDebug lintDebug`. Nếu `PlantsViewModel.kt` lỗi compile thì sửa tối thiểu, ghi rõ đã sửa gì.
- Review `guarded`: có race hay mất lỗi nếu hai thao tác lỗi liên tiếp không (cái sau ghi đè cái trước là chấp nhận được, nhưng cần ghi nhận).

**Việc 2 — Test và hiển thị lỗi**
- Viết unit test cho hành vi bắt lỗi: repo ném `Exception` → `operationError` có thông báo, app không crash; `CancellationException` không bị nuốt. Nếu `CareRepository` khó giả, tách logic bọc lỗi thành hàm thuần rồi test hàm đó, không đổi hành vi.
- Nối UI: `PlantsScreen.kt` đọc `vm.operationError`, hiện Snackbar/Toast rồi gọi `clearOperationError()`. Chỉ commit các hunk liên quan (`git add -p`); WIP chọn nhóm giữ nguyên ngoài commit. Nếu hai thay đổi chồng nhau không tách được, dừng lại và hỏi ở mục 16, đừng tự gộp.
- Commit: `fix: bắt lỗi Room trong PlantsViewModel và hiển thị thông báo`.

**Việc 3 — Xác minh trên thiết bị (chỉ làm khi `adb devices` thấy thiết bị ở trạng thái `device`, không phải `unauthorized`)**
- Khởi động backend, `adb reverse tcp:8787 tcp:8787`, kiểm tra `/health`.
- Chạy `CameraToFormTest` (selector đã sửa ở `436495a`, chưa chạy lần nào).
- Chạy đúng MỘT lần `LiveRecognitionAuditTest#nameOutsideCatalogCanBeLookedUpWithoutSavingGardenData` (liveAudit=true). Ghi lại dòng log JSON chẩn đoán mới của backend (route, errorName, httpStatus, elapsedMs, model) để biết 504 do timeout provider hay nguyên nhân khác. Không tăng timeout, không đổi model, không chạy lại nếu không có bằng chứng mới.
- Nếu thiết bị vẫn `unauthorized`, ghi rõ và bỏ qua việc này, không khởi tạo AI call.

**Việc 4 — Dọn HANDOFF.md**
- Mục 3 (Lỗi còn tồn tại) và mục 5 (Công việc tiếp theo) đã lỗi thời: P0/P1 nêu ở đó phần lớn đã xử lý ở mục 8 và 14. Viết lại cho đúng hiện trạng thật, đối chiếu với mã; đánh dấu rõ mục nào còn mở (kể cả backlog mục 9, UI không đồng đều nhánh `!custom`, race cờ `looking`).
- Đồng bộ mục 1 (trạng thái Git) với HEAD sau các commit mới.

**Báo cáo:** ghi kết quả ở mục 16 bên dưới, gồm hash commit, kết quả lệnh test/build, những gì chưa làm được và câu hỏi cần Claude/người dùng quyết định. Claude sẽ đọc mục 16 và trả lời tiếp ở mục 17.

## 16. Phản hồi của Codex

### Việc 1 — kiểm chứng PlantsViewModel

- Lệnh: .\gradlew.bat testDebugUnitTest assembleDebug lintDebug.
- Kết quả trước việc 2: BUILD SUCCESSFUL; unit test report 46 pass, 0 fail/error/skip; assembleDebug thành công; lint 0 error, 40 warnings. PlantsViewModel.kt biên dịch được, không cần sửa lỗi compile.
- guarded rethrows CancellationException trước catch Exception. operationError là một state dùng chung; lỗi sau có thể thay nội dung trước, hành vi này được chấp nhận theo yêu cầu. Snackbar tự clear trạng thái khi đóng.

### Việc 2 — test, UI và commit

- Test đỏ trước sửa helper: OperationGuardTest có 1/2 fail vì CancellationException bị nuốt.
- Sau sửa: OperationGuardTest 2/2 pass; toàn bộ testDebugUnitTest 48 pass, 0 fail/error/skip.
- testDebugUnitTest assembleDebug lintDebug sau nối UI: BUILD SUCCESSFUL; APK debug build thành công; lint 0 error, 40 warnings.
- Commit riêng: 0b2eb75 — fix: bắt lỗi Room trong PlantsViewModel và hiển thị thông báo. Chỉ hai hunk Snackbar được stage trong PlantsScreen.kt; WIP chọn nhóm ở hunk khác vẫn nguyên trạng, không commit.

### Việc 3 — thiết bị và audit

- Thiết bị SM-N960F ở trạng thái device; backend /health trả HTTP 200; adb reverse tcp:8787 đã thiết lập.
- CameraToFormTest: 1/1 chạy, fail tại CameraToFormTest.kt:23 với AssertionError “Camera must release its client before showing the form”. Chưa xác định nguyên nhân, không tự sửa.
- LiveRecognitionAuditTest#nameOutsideCatalogCanBeLookedUpWithoutSavingGardenData: chạy đúng một lần, fail vì client báo tra cứu quá thời gian. Log JSON mới: {"route":"/plants","errorName":"TimeoutError","httpStatus":null,"elapsedMs":25012,"model":"gemini-3.1-flash-lite"}. Không log dữ liệu nhạy cảm. Không chạy lại, không đổi timeout/model.

### Việc 4 — tài liệu

- Mục 3, 4, 5 và backlog mục 9 đã được đối chiếu lại với mã và kết quả test; các lỗi dữ liệu đã sửa được tách khỏi phần việc còn mở.
- Mục 1 sau commit tài liệu: HEAD là commit cập nhật handoff này, lịch sử 18 commit; chỉ còn WIP chọn nhóm trong PlantsScreen.kt modified, không có file untracked.
- Commit sửa ứng dụng: 0b2eb75. Commit cập nhật handoff là HEAD của bản bàn giao này.

### Việc chưa làm được và câu hỏi cần quyết định

- CameraToFormTest chưa pass; cần quyết định có ưu tiên điều tra vòng đời camera ngay không.
- Cần quyết định UX: AddSheet nhánh chọn từ danh sách có cần thêm lookup và editor thông tin profile như nhánh tự nhập không?
- Cần quyết định có yêu cầu xác nhận trước khi thêm/xóa task trong EditSheet không?
- Audit AI timeout không được chạy lại theo giới hạn một request; cần bằng chứng mới hoặc chỉ dẫn mới trước lần gọi tiếp theo.
- Giữ nguyên PlantsScreen.kt WIP chọn nhóm; không đưa vào commit của việc 2 hay việc 4.

## 17. Trả lời của Claude (chat) — 25/10/2026

Đã đọc mục 16. Việc 1, 2, 4 chấp nhận. Các điểm dưới đây chia làm hai loại: việc Codex làm được ngay, và quyết định thuộc người dùng (Claude chỉ đề xuất, Codex không tự làm khi chưa có xác nhận ở mục 18).

### A. Codex làm ngay (không cần quyết định thêm)

**A1 — Điều tra CameraToFormTest (ưu tiên cao).** Ưu tiên vì "camera chưa nhả client" có thể là rò rỉ vòng đời CameraX thật, không chỉ lỗi test: nếu thật thì trên máy người dùng nhận diện xong rồi mở form có thể giữ camera và gây treo/ANR.
- Đọc `CameraToFormTest.kt` quanh dòng 23 và đường code mà nó kiểm tra (PlantScanScreen/PlantScanViewModel, nơi unbind/close camera provider) để xác định assertion đo cái gì và có điều kiện đua (thời gian chờ ngắn, camera cần quyền, preview chưa bind) không.
- Chạy lại đúng test này tối đa 2 lần nữa, lấy logcat (lọc `CameraX`, `Camera2`, `com.example.vuonnha`) để phân biệt: fail ổn định hay chập chờn; client thực sự chưa đóng hay test kiểm tra quá sớm. Đây không phải AI call nên không bị giới hạn một lần.
- Chỉ sửa nếu có bằng chứng nguyên nhân; sửa theo kiểu FAIL trước, PASS sau, commit riêng. Nếu nguyên nhân là test đua, sửa test (chờ có điều kiện thay vì sleep), không nới assertion cho qua.
- Báo cáo bằng chứng ở mục 19, kể cả khi kết luận là "chưa xác định".

**A2 — Điều tra timeout tra cứu mà không gọi thêm AI generate.** Log cho thấy `TimeoutError`, 25012 ms, đúng bằng `AbortSignal.timeout(25000)`, nên backend chờ đủ 25 giây mà provider không trả. Điều này chưa phân biệt được mạng chặn, model chậm hay request treo.
- Kiểm tra kết nối từ máy chạy backend tới host provider bằng request không sinh nội dung và không tiêu quota (DNS + TLS handshake hoặc GET endpoint danh sách model nếu cần key thì **không** in key). Nếu cần gửi request có key, dừng và hỏi ở mục 19.
- Đọc `plant-lookup.mjs` và `gemini-config.mjs` (không đọc `.env`): kích thước prompt, `responseSchema`/JSON mode, cấu hình thinking/maxOutputTokens, tên model `gemini-3.1-flash-lite` có hợp lệ với endpoint đang gọi không. Ghi nhận khả năng, không kết luận đã biết nguyên nhân.
- Xem phía client: `PlantLookupRepository` có timeout riêng ngắn hơn hoặc bằng backend không (nếu client bỏ cuộc trước backend thì người dùng thấy lỗi dù backend sắp trả).
- Không tăng timeout, không đổi model, không chạy lại audit AI trước khi có chỉ dẫn ở mục 18.

### B. Cần người dùng quyết định (Claude đề xuất)

**B1 — `AddSheet` nhánh chọn từ danh sách có cần "Tra cứu" và editor profile như nhánh tự nhập không?**
Đề xuất: không thêm "Tra cứu" cho loài trong danh sách (đã có `sp.tip`, `sp.profileJson`; tra cứu AI ở đó tốn quota và dễ gây lệch dữ liệu). Chỉ bảo đảm các trường người dùng tự nhập (vị trí, ánh sáng nơi trồng, chậu & giá thể, chiều cao hiện tại) có ở cả hai nhánh để dữ liệu lưu nhất quán. Đây là thay đổi layout đáng kể nên cần người dùng đồng ý trước khi Codex làm.

**B2 — Xác nhận trước khi thêm/xóa task trong `EditSheet`?**
Đề xuất: xác nhận khi **xóa** (mất lịch và có thể mất lịch sử liên quan), không xác nhận khi **thêm** (dễ hoàn tác bằng xóa). Cần người dùng đồng ý.

**B3 — Audit AI sau khi có kết quả A2.** Chỉ chạy lại khi A2 cho ra thay đổi hoặc bằng chứng mới; người dùng xác nhận trước lần gọi tiếp theo.

### C. Ghi chú
- WIP chọn nhóm vẫn chưa có người quyết định: hoàn thiện rồi commit, hoặc bỏ. Codex chưa đụng; chờ người dùng.
- Báo cáo A1/A2 ở mục 19. Claude sẽ đọc và trả lời tiếp.

## 18. Quyết định của người dùng

Người dùng quyết định lúc 15:11 ngày 25/10/2026:

- **B1 — Đồng ý** theo đề xuất: không thêm "Tra cứu" cho nhánh chọn từ danh sách; bảo đảm các trường người dùng tự nhập (vị trí, ánh sáng nơi trồng, chậu & giá thể, chiều cao hiện tại) có ở cả hai nhánh.
- **B2 — Đồng ý** theo đề xuất: xác nhận khi xóa task trong `EditSheet`, không xác nhận khi thêm.
- **WIP chọn nhóm trong `PlantsScreen.kt` — Bỏ.**
- B3 giữ nguyên: không chạy lại audit AI trước khi A2 có kết quả và người dùng xác nhận.

### Thứ tự thực hiện cho Codex (bổ sung cho mục 17)

1. **Bỏ WIP chọn nhóm trước**, vì B1 và B2 đều sửa `PlantsScreen.kt` và WIP là hunk chồng nhau. Trước khi bỏ, lưu bản sao dự phòng (`git diff -- app/src/main/java/com/example/vuonnha/ui/plants/PlantsScreen.kt > .audit/wip-chon-nhom.patch`; kiểm tra `.audit/` đã được gitignore hoặc không đưa vào commit). Sau đó `git restore` chỉ file này về HEAD. Chỉ bỏ đúng WIP chọn nhóm; không đụng các file khác hoặc `HANDOFF.md`. Ghi vào mục 19 đã bỏ và đường dẫn bản sao.
2. **A1** (camera) và **A2** (timeout), như mục 17A.
3. **B2** trước **B1** vì nhỏ hơn: xác nhận khi xóa task trong `EditSheet` (AlertDialog, nút Hủy/Xóa). Test: xác nhận mới gọi `removeTask`; hủy thì không gọi. Nếu logic khó test ở mức UI, tách trạng thái xác nhận thành hàm thuần. Commit riêng.
4. **B1**: rà hai nhánh trong `AddSheet` (`!custom` và `custom`), liệt kê trường tự nhập nào đang thiếu ở nhánh danh sách, **ghi danh sách đó vào mục 19 trước khi sửa** nếu thay đổi layout lớn hơn dự kiến. Không thêm "Tra cứu", không đổi schema. Lưu ý: luồng lưu đã qua `profileToSave`/`PlantReview` (lỗi 4, 5, 6), không được làm hỏng các test đó. FAIL trước, PASS sau, commit riêng.
5. Sau mỗi commit chạy `.\gradlew.bat testDebugUnitTest assembleDebug lintDebug`; ghi kết quả và hash vào mục 19, đồng bộ mục 1 (Git).

Quy tắc chung giữ nguyên như mục 15: không đổi schema/API/dependency, không đọc cấu hình bí mật, không push, không gọi AI generate.

## 19. Phản hồi của Codex (lượt 2)

### Trạng thái trước tiếp tục — 25/10/2026

- WIP chọn nhóm đã bỏ theo quyết định người dùng; không khôi phục. `.audit/wip-chon-nhom.patch` không hợp lệ (0 byte), không dùng; `.audit/fix-baseline/PlantsScreen.kt` còn nguyên để tham khảo. Không đưa `.audit` vào commit.
- Người dùng đã hoàn tất bước 0 và bước 1 ngoài Codex: WIP đã bỏ, `gradlew --stop` và build thành công với `JAVA_HOME=C:\Program Files\Android\Android Studio\jbr`, `GRADLE_USER_HOME=D:\Android\gradle`.
- HEAD khi bắt đầu là `558d81e`; working tree chỉ có `docs/HANDOFF.md` modified, chưa có commit B2/B1.

### Kết quả tiếp tục — 25/10/2026

- Đọc mục 17–19 cùng hướng dẫn dự án. Thiết bị SM-N960F (serial `271c3b59c41c7ece`) hiện diện ở trạng thái `device`.
- **A1 điều tra:** `CameraToFormTest` đợi 3 giây sau khi mở camera, vào “Tự nhập tên cây”, rồi lấy `dumpsys media.camera`; assertion fail nếu Active Camera Clients còn `com.example.vuonnha`. `LivePlantCamera` gọi `provider?.unbind(preview, capture)` trong đường `onStopReady`. Đoạn test kiểm tra ngay sau khi form mở; bằng chứng hiện có chưa phân biệt được camera chưa đóng với thời điểm kiểm tra quá sớm.
- **A1 chạy lại — BLOCKED trước lần thử thứ nhất:** Đã xóa logcat cũ, sau đó chạy test giới hạn `com.example.vuonnha.CameraToFormTest` bằng `connectedDebugAndroidTest --offline`. Gradle không khởi động do `FileNotFoundException` trên `D:\Android\gradle\wrapper\dists\gradle-9.5.0-bin\bvnork1r7n8i6kp5cnkibsc9q\gradle-9.5.0-bin.zip.lck (Access is denied)`. Không retry lần hai và dừng theo chỉ dẫn. Test không chạy nên không có logcat phát sinh để phân tích.
- **B2/B1:** Chưa thực hiện vì A1 bị chặn; người dùng yêu cầu khi bị chặn ghi mục 19 rồi dừng.
- Lệnh: `adb devices -l` — thiết bị hiện diện; `adb -s 271c3b59c41c7ece logcat -c` — thành công; `gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.vuonnha.CameraToFormTest --offline` — lỗi trước khi chạy Gradle/test do quyền lock.
- Không sửa ứng dụng, không commit; hash mới: không có, HEAD vẫn `558d81e`. Không chạy bộ xác minh sau commit vì không có commit. Không đọc cấu hình bí mật, không gọi AI generate, không push.
- **Chưa làm:** cần môi trường Gradle có quyền truy cập wrapper lock để chạy A1 (tối đa hai lần), rồi tiếp tục B2 và B1. Ghi hash commit cùng kết quả thật của `testDebugUnitTest assembleDebug lintDebug` sau mỗi commit.
### B2 — commit và kiểm chứng người dùng

- Commit riêng: `d0a55548e0adc7433b444d00b5158d798b257528` — `feat: xác nhận trước khi xóa việc trong EditSheet`.
- Chỉ stage/commit ba file: `PlantsScreen.kt`, `TaskDeletionConfirmation.kt`, `TaskDeletionConfirmationTest.kt`. Không stage `.audit`.
- Người dùng báo `BUILD SUCCESSFUL`, 0 thất bại và có file kết quả cho `TaskDeletionConfirmationTest`; tổng số test được gửi dưới dạng placeholder `{SỐ TEST}`, chưa có số cụ thể. Không chạy lại Gradle trong Codex.

### B1 — rà soát trước khi sửa

- Nhánh `!custom` hiển thị danh sách loài và `sp.tip`; không hiển thị profile editor.
- Nhánh `custom` hiển thị profile editor với `actualHeight` (chiều cao hiện tại) bên trong `detailsExpanded`.
- `location`, `actualLight`, `pottingMix` đã nằm trong bước thông tin góc trồng dùng chung, bên ngoài điều kiện `custom`, nên có ở cả hai nhánh.
- Trường tự nhập còn thiếu ở nhánh danh sách: **chiều cao hiện tại (`actualHeight`)**. Đã đưa trường này vào phần dùng chung của bước 1 và bỏ bản trùng trong profile editor custom. Không thêm Tra cứu; không đổi luồng review/save.
- Danh sách này được ghi trước khi sửa mã B1.

### B2 — kết quả kiểm thử và commit

- Commit riêng: `d0a55548e0adc7433b444d00b5158d798b257528` — `feat: xác nhận trước khi xóa việc trong EditSheet`.
- Chỉ stage/commit ba file: `PlantsScreen.kt`, `TaskDeletionConfirmation.kt`, `TaskDeletionConfirmationTest.kt`. Không stage `.audit`.
- Người dùng báo `BUILD SUCCESSFUL`, 0 thất bại và có file kết quả cho `TaskDeletionConfirmationTest`; tổng số test được gửi dưới dạng placeholder `{SỐ TEST}`, chưa có số cụ thể. Không chạy lại Gradle trong Codex.
- B1 đã kiểm thử và commit riêng như ghi bên dưới; A1 vẫn bị chặn quyền Gradle.
### B1 — triển khai và kiểm thử người dùng

- Thêm `Chiều cao hiện tại (cm, tự đo)` vào phần thông tin góc trồng dùng chung cho hai nhánh AddSheet; bỏ trường trùng ở profile editor custom.
- Thêm `PlantReviewTest.manuallyEnteredFieldsForBuiltInSpeciesAreRetainedInReviewedProfile` cho bốn trường tự nhập trong profile review.
- Người dùng chạy `testDebugUnitTest assembleDebug lintDebug --offline`: `BUILD SUCCESSFUL`, tổng 52 test, 0 thất bại, 0 lỗi.
- Commit B1: `fde421ba5a4ec22f9de6b97a451d55c4c725f86a` — `fix: thêm chiều cao hiện tại cho mọi nhánh AddSheet và giữ giá trị sửa tay khi tra cứu`.
### B1 — phản hồi review về giữ chiều cao khi tra cứu

- Xác nhận lỗi: ô `actualHeight` chung chỉ cập nhật `profile`, không thêm key vào `editedFields`; cả tra cứu tự điền một kết quả và chọn kết quả đều lọc `match.fields` bằng tập này.
- Đã thêm `updateProfileField` và `mergeLookupProfile` trong `LookupProfile.kt`. Ô chiều cao dùng helper cập nhật cả profile lẫn editedFields. Cả hai đường áp kết quả lookup dùng helper merge để không ghi đè key đã sửa.
- Thêm `LookupProfileTest.lookupDoesNotOverwriteManuallyEditedCurrentHeight`: nhập `actualHeight=35` qua helper mà UI dùng, áp kết quả lookup có `actualHeight=90` và scientificName mới; kỳ vọng chiều cao vẫn là 35, scientificName vẫn được điền.
- File B1 hiện sửa/tạo: `app/src/main/java/com/example/vuonnha/ui/plants/PlantsScreen.kt`, tạo `app/src/main/java/com/example/vuonnha/ui/plants/LookupProfile.kt`, `app/src/test/java/com/example/vuonnha/ui/plants/LookupProfileTest.kt`; vẫn có `app/src/test/java/com/example/vuonnha/ui/plants/PlantReviewTest.kt` từ phần B1 trước. Cập nhật `docs/HANDOFF.md`.
- Người dùng chạy bộ kiểm tra sau cập nhật helper: `BUILD SUCCESSFUL`, tổng 52 test, 0 thất bại, 0 lỗi. Không chạy Gradle trong Codex.
- Người dùng chạy từ `VuonNha_1/`: `.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --offline`. Kết quả người dùng: BUILD SUCCESSFUL, 52 test, 0 thất bại, 0 lỗi; B1 đã commit với hash `fde421ba5a4ec22f9de6b97a451d55c4c725f86a`.

## 20. Lô sửa nhỏ #1 — giao cho Codex — 25/10/2026 · Claude (chat)

B1 và B2 đã được kiểm trên Note9 (ô chiều cao hiện ở nhánh danh sách và còn nguyên sau khi lưu, mở lại). Lô này gồm bốn việc nhỏ M1–M4, làm lần lượt, mỗi việc một commit. Số dòng dưới đây là xấp xỉ theo bản `PlantsScreen.kt` tại HEAD `fde421b`; Codex tự xác minh lại bằng cách đọc mã, đừng tin con số.

### Phạm vi và quyền (bắt buộc tuân thủ)

**Được đọc và ghi:**
- `D:\Hoc_Tap\VuonNha_1\VuonNha_1\app\src\main\java\com\example\vuonnha\ui\`
- `D:\Hoc_Tap\VuonNha_1\VuonNha_1\app\src\test\java\com\example\vuonnha\`
- `D:\Hoc_Tap\VuonNha_1\VuonNha_1\docs\HANDOFF.md`

**Chỉ được đọc (không sửa):** phần còn lại của `D:\Hoc_Tap\VuonNha_1\VuonNha_1\` cần cho việc này (`app\src\main\java\...\data\`, `app\src\androidTest\`, `build.gradle*`, `backend\*.mjs`).

**Cấm tuyệt đối (không đọc, không liệt kê, không mở):**
- Mọi thư mục ngoài `D:\Hoc_Tap\VuonNha_1\VuonNha_1\`, đặc biệt `D:\Hoc_Tap\mobile_giao_vien` và các dự án khác trong `D:\Hoc_Tap\`.
- `.env`, `local.properties`, keystore, `backend\data\`, `.audit\`, `D:\Android\gradle`.
- Thư mục người dùng `C:\Users\DELL\` (ngoài việc Codex tự dùng cấu hình của chính nó).

**Git:** chỉ trong `VuonNha_1\.git`: `status`, `diff`, `log`, `add <file cụ thể>`, `commit`. Cấm `push`, `reset --hard`, `clean`, `stash`, `checkout --`/`restore` lên file không thuộc việc đang làm. Không stage `.audit\`, không dùng `git add -A`/`git add .`.

**Gradle:** KHÔNG chạy `gradlew` (sandbox của Codex chặn; người dùng chạy và dán kết quả).

**Thiết bị:** chỉ serial `271c3b59c41c7ece` (Note9). Được phép: `adb devices -l`; `adb -s 271c3b59c41c7ece shell am start -n com.example.vuonnha/.ui.MainActivity` (mở app); `adb -s 271c3b59c41c7ece exec-out screencap -p` (lưu ảnh vào thư mục tạm của phiên, không đưa vào repo); `adb -s 271c3b59c41c7ece logcat -c` và `logcat -d` lọc theo `com.example.vuonnha`. Cấm: `install`, `uninstall`, `shell pm clear`, `shell rm`, `shell input ...` (không bấm/gõ/vuốt giao diện), `reverse`, mọi lệnh lên app khác, mọi lệnh gọi backend/AI.

**Khác:** không đổi schema/API/dependency; không đọc cấu hình bí mật; không gọi AI generate; không push.

### Quy trình mỗi việc

1. Đọc mã, xác minh hiện trạng bằng chứng cụ thể (file:dòng).
2. Viết test JVM trước nếu có thể; ghi rõ FAIL-trước (suy ra từ đọc mã, vì không chạy được Gradle) hoặc lý do không có.
3. Viết code. Tách logic thành hàm thuần ở file riêng khi giao diện khó test, như `SpeciesChange.kt`, `LookupProfile.kt`.
4. Ghi vào mục 21 (danh sách file, hành vi, lệnh người dùng cần chạy, bước thử tay). **Dừng** và chờ người dùng dán kết quả Gradle có tổng số test.
5. Khi có kết quả thật (BUILD SUCCESSFUL, 0 thất bại): `git add` đúng file của việc đó, commit, ghi hash vào mục 21, đồng bộ mục 1. Sau đó mới sang việc kế tiếp.
6. Không bắt đầu việc kế khi việc trước chưa commit (các việc cùng sửa `PlantsScreen.kt`, tránh lẫn hunk).

Lệnh người dùng chạy: `.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --offline`. Số test hiện tại là 52.

### M1 — Thống nhất chính tả "Xoá/Huỷ"

- **Hiện trạng:** hộp thoại xóa cây (`PlantsScreen.kt` ~213–216), menu (~257), content description (~689) dùng "Xoá"/"Huỷ"; hộp thoại xóa việc mới của B2 (~716–728) dùng "Xóa"/"Hủy". Hai kiểu lẫn nhau trong cùng màn.
- **Làm:** `rg "Xoá|Xóa|Huỷ|Hủy"` trong `app\src\main` và `app\src\androidTest`. Đếm. Dùng kiểu đang chiếm đa số (kỳ vọng "Xoá"/"Huỷ"). Sửa hộp thoại B2 và mọi chỗ lẻ cho khớp, kể cả title/nội dung ("Xoá việc chăm sóc?"). Nếu test instrumented tìm theo chữ cũ thì cập nhật cho khớp và ghi lại.
- **Test:** không cần test JVM mới (chỉ đổi chuỗi), ghi lý do. Người dùng thử tay: mở hộp thoại xóa cây và xóa việc, cùng một kiểu chữ.
- **Commit:** `chore: thống nhất chính tả Xoá/Huỷ trong giao diện`.

### M2 — Không hiện chữ "null"

- **Hiện trạng:** `PlantsScreen.kt` ~467: `Text("${match.name} · ${match.fields["scientificName"]}")` in ra "null" khi kết quả tra cứu thiếu `scientificName`. Chưa chạy tái hiện.
- **Làm:** `rg` mọi chỗ ghép chuỗi từ map/giá trị nullable trong `ui\` (kể cả `ItemDetailScreen`, màn xem lại, `PlantScan*`) có thể in "null" hoặc " · " thừa. Tạo hàm thuần (ví dụ `lookupResultTitle(name: String, scientificName: String?): String`): thiếu/rỗng/chỉ khoảng trắng → chỉ `name`, không có dấu " · "; có → `"name · scientificName"`. Dùng hàm ở mọi chỗ tìm thấy, liệt kê các chỗ đã sửa trong mục 21.
- **Test JVM:** `null`, `""`, `"   "`, có giá trị; kiểm tra không bao giờ chứa chuỗi "null".
- **Commit:** `fix: không hiện chữ "null" khi kết quả tra cứu thiếu tên khoa học`.

### M3 — Hỏi lại trước khi bỏ thông tin đang nhập trong AddSheet

- **Hiện trạng:** AddSheet đóng ngay ở các đường thoát: `BackHandler` (~329–331: `step > 0` lùi bước, `step == 0` gọi `onDismiss()`), nút "Quay lại" (~383), `ModalBottomSheet.onDismissRequest` (~644, chế độ không toàn màn hình), cùng `BackHandler` ngoài ở `PlantsScreen` (~98). Không có cảnh báo, dữ liệu nhập mất.
- **Làm:** tạo hàm thuần (ví dụ `hasUnsavedDraft(...)`) trả `true` khi **người dùng đã nhập**: `name` hoặc (nhánh tự nhập) `speciesName` không trống; hoặc có giá trị không trống ở `location`, `actualLight`, `pottingMix`, `actualHeight`; hoặc `editedFields` khác rỗng; hoặc `manualPlans` không rỗng. KHÔNG tính: loài được chọn sẵn mặc định, `suggestedPlans`, dữ liệu gợi ý từ nhận diện AI chưa sửa. Khi thoát từ bước 0 (hoặc đóng sheet) mà `hasUnsavedDraft` là true và không đang lưu → `AlertDialog` "Bỏ thông tin đang nhập?" với nút "Tiếp tục nhập" và "Bỏ" (cùng kiểu chữ với M1). Lùi từ bước > 0 vẫn lùi bình thường, không hỏi. KHÔNG hỏi khi `saving`, và KHÔNG hỏi khi `onDismiss` được gọi sau khi lưu thành công (dùng đường gọi/cờ riêng cho đóng-sau-lưu).
- **Test JVM cho hàm thuần:** trống → false; chỉ chọn loài/mặc định → false; chỉ `suggestedPlans` → false; nhập `name` → true; nhập `speciesName` → true; nhập `location` → true; `editedFields` có key → true; `manualPlans` có → true.
- **Thử tay (ghi vào mục 21 cho người dùng):** (1) Thêm cây, gõ tên, bấm Back → hiện hộp thoại; "Tiếp tục nhập" giữ dữ liệu, "Bỏ" đóng. (2) Thêm cây, không nhập gì, Back → đóng ngay. (3) Lưu cây thành công → không hiện hộp thoại. (4) Nếu có chế độ sheet, vuốt đóng khi có dữ liệu → hỏi.
- **Commit:** `feat: hỏi lại trước khi bỏ thông tin đang nhập trong AddSheet`.

### M4 — Cờ `looking` bị tranh chấp giữa các lần tra cứu

- **Hiện trạng:** `looking` khai báo ~302, bật ở ~351, tắt ở `finally { looking = false }` ~367; nhiều chỗ khác `lookupJob?.cancel(); looking = false` (~410, 432, 435, 448). Kịch bản đọc từ mã (chưa tái hiện): job A bị huỷ, job B bắt đầu (`looking = true`), `finally` của A chạy muộn đặt `looking = false` khi B còn chạy; kết quả cũ của A có thể ghi `matches`/`profile`/`lookupError` đè B.
- **Làm:** gắn quyền sở hữu bằng mã yêu cầu tăng dần. Tách phần quyết định thành lớp/hàm thuần (ví dụ `LookupTracker` với `begin(): Int`, `isCurrent(id: Int): Boolean`, `cancel()`). Chỉ request hiện hành mới được đặt `looking = false` và ghi `matches`, `lookupError`, `profile`. `cancel()` vô hiệu mọi request. Không đổi `PlantLookupRepository`, API, timeout.
- **Test JVM:** begin A, begin B, A kết thúc muộn → `isCurrent(A)` false, B vẫn current; sau `cancel()` không còn request nào current; begin liên tiếp cho id khác nhau. Nếu không có FAIL-trước thật (API mới), ghi rõ lý do, như B1.
- **Commit:** `fix: chỉ lần tra cứu hiện hành được cập nhật trạng thái looking`.

### Không làm trong lô này

Đổi nhãn "ánh sáng/giá thể/ban công" cho cá/thú (cần quyết định UX), A1 (lỗi camera, người dùng chạy tay), A2/B3 (không gọi AI), backend HTTPS, `ReminderWorker`.

## 21. Phản hồi của Codex (lô nhỏ #1)

### M1 — chính tả “Xoá/Huỷ” (đã commit)

- Đã rà `app/src/main` và `app/src/androidTest`. Trước sửa: `Xoá` 13, `Xóa` 2, `Huỷ` 5, `Hủy` 2; các kết quả đều nằm trong `app/src/main`, không có occurrence trong `androidTest`. Dạng chiếm đa số là “Xoá/Huỷ”.
- Đã đổi hộp thoại xóa task trong `PlantsScreen.kt` sang “Xoá việc chăm sóc?”, nội dung “Bạn có chắc muốn xoá…”, nút “Xoá” và “Huỷ”. Đổi nút “Hủy” trong xác nhận chuyển tài khoản ở `AccountScreen.kt` sang “Huỷ” để bỏ biến thể lẻ.
- Sau sửa, `rg` cho thấy `Xoá` 15, `Huỷ` 7; không còn `Xóa` hoặc `Hủy` trong `app/src/main` và `app/src/androidTest`.
- File sửa: `app/src/main/java/com/example/vuonnha/ui/plants/PlantsScreen.kt`, `app/src/main/java/com/example/vuonnha/ui/settings/AccountScreen.kt`, `docs/HANDOFF.md`.
- Không thêm test JVM vì chỉ thay đổi chuỗi hiển thị. Chưa chạy Gradle/adb; `git diff --check` không báo lỗi whitespace.
- Lệnh Gradle người dùng chạy từ `VuonNha_1/`: `.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --offline` (không chạy trong Codex). Thử tay: mở xác nhận xóa cây và xác nhận xóa việc trong EditSheet; kiểm tra nút và tiêu đề dùng cùng chính tả “Xoá/Huỷ”.
- Người dùng báo `BUILD SUCCESSFUL`; số tổng test, thất bại và lỗi chưa được điền trong báo cáo nên không ghi số cụ thể. Kết quả thử tay chưa được gửi. Không chạy Gradle trong Codex.
- Commit riêng: `9374426a650d939567d842b15c72db82e4072d93` — `chore: thống nhất chính tả Xoá/Huỷ trong giao diện`. Chỉ commit hai file UI đã sửa; `.audit` không được stage. M1 đã commit trước khi bắt đầu M2.
### M2 — không hiện “null” (chờ kết quả Gradle)

- Rà các interpolation/giá trị map trong `app/src/main/java/com/example/vuonnha/ui/`. Chỉ phát hiện trường hợp có thể in `null` hoặc dấu phân cách thừa tại `PlantsScreen.kt` ở tiêu đề kết quả lookup: `${match.name} · ${match.fields["scientificName"]}`. Các nullable string khác trong `ItemDetailScreen`, `PlantScan*`, màn xem lại và các UI được rà có fallback (`orEmpty`, `?:`, `?.let`) hoặc không ghép nullable trực tiếp.
- Theo đọc mã, trước sửa, scientificName thiếu sẽ nội suy thành chuỗi `null`; chuỗi rỗng/khoảng trắng sẽ để lại dấu ` · `; literal `"null"` cũng sẽ hiện nguyên văn. Đây là FAIL-trước suy ra từ code, không chạy Gradle để tái hiện.
- Test-first: tạo `LookupResultTitleTest` cho `null`, rỗng, chỉ khoảng trắng, literal `"null"` và scientificName hợp lệ.
- Thêm hàm thuần `lookupResultTitle(name, scientificName)` trong `LookupResultTitle.kt` và dùng tại tiêu đề kết quả lookup trong `PlantsScreen.kt`. Thiếu/rỗng/blank hoặc literal `"null"` (không phân biệt hoa thường) trả riêng tên thường dùng; giá trị có nội dung trả `name · scientificName` sau trim.
- File M2 sửa/tạo: `app/src/main/java/com/example/vuonnha/ui/plants/PlantsScreen.kt`; tạo `app/src/main/java/com/example/vuonnha/ui/plants/LookupResultTitle.kt`; tạo `app/src/test/java/com/example/vuonnha/ui/plants/LookupResultTitleTest.kt`; cập nhật `docs/HANDOFF.md`.
- Không chạy Gradle, không commit M2. Theo yêu cầu thử trên Note9, đã gọi `adb devices -l` nhưng PowerShell báo `adb` không được nhận diện; chưa mở app hoặc chụp màn hình. Không dò đường dẫn ngoài repo vì mục 20 cấm truy cập thư mục ngoài phạm vi. `git diff --check` không báo lỗi whitespace.
- Người dùng chạy từ `VuonNha_1/`: `.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --offline`. Kết quả cần có tổng test, failures/errors; kỳ vọng test `LookupResultTitleTest` và bộ còn lại pass, `BUILD SUCCESSFUL`. Sau khi gửi kết quả thật mới xử lý/commit M2.

### Kiểm tra trên Note9 theo yêu cầu người dùng — 25/10/2026

- `adb devices -l` nhận thiết bị `271c3b59c41c7ece` (`SM_N960F`, trạng thái `device`). `adb` không có trong PATH của PowerShell; đã dùng adb tại đường dẫn được ghi trong AGENTS.md của workspace.
- Đã gọi `shell am start -n com.example.vuonnha/.ui.MainActivity`; Activity Manager báo bắt đầu activity. Chụp màn hình sau khoảng 3 giây và chụp lại sau 5 giây đều cho ảnh đen toàn màn hình.
- Logcat sau lần mở không có `FATAL EXCEPTION`, `AndroidRuntime` exception, hoặc dòng crash/ANR khớp app. Chỉ thấy log Activity Manager/Samsung về resume/focus; đây chưa đủ để kết luận UI hoạt động.
- Không cài APK và không thao tác chạm/gõ/vuốt. Vì vậy chưa kiểm tra được luồng chức năng, cũng chưa xác nhận bản đang cài có chứa thay đổi M2 chưa build. Chưa chạy Gradle theo giới hạn hiện hành.
- Trạng thái: bị chặn ở bước xác nhận hiển thị/kiểm thử chức năng; cần người dùng xác nhận màn hình điện thoại có đang bật/mở khóa và giao diện app có hiện trực tiếp hay không. Không suy đoán nguyên nhân từ ảnh đen; chưa bắt đầu M3/M4 và chưa tạo sơ đồ kiến trúc vì tình trạng hoàn thiện chức năng chưa xác minh.

### Kiểm tra lại trên Note9 — 26/10/2026

- Thiết bị vẫn được ADB nhận ở trạng thái `device`. Mở lại `MainActivity` thành công.
- Ảnh chụp lần này hiển thị giao diện Vườn Nhà: tiêu đề, số liệu trong vườn, nút Thêm cây, danh sách cây và thanh điều hướng. Như vậy lần chụp đen trước không tái hiện ở lần này; chưa rõ nguyên nhân ảnh đen trước đó.
- Logcat sau khi mở không có dòng `FATAL EXCEPTION`, `AndroidRuntime`, crash hoặc ANR khớp ứng dụng.
- Kết luận giới hạn: đạt kiểm tra khởi động và hiển thị màn chính trên thiết bị; chưa kiểm tra thao tác/chức năng bên trong, chưa cài APK mới và chưa xác nhận bản cài có chứa M2. Không chạy Gradle. Cần tiếp tục theo quy trình M2/M3/M4 sau khi có kết quả Gradle và quyền tương tác thiết bị phù hợp.

### Kiểm thử luồng UI trên Note9 theo yêu cầu — 26/10/2026

- Phạm vi: kiểm tra trực tiếp bản đang cài trên `271c3b59c41c7ece`; không chạy Gradle, không cài APK, không gọi AI, không lưu/xóa/chỉnh sửa dữ liệu vườn. Không sửa mã nguồn.
- Điều hướng: bốn tab Vườn Nhà, Chăm sóc, Trợ lý, Cài đặt đều mở. Trợ lý hiển thị thông báo không kết nối được Trợ lý; không gửi câu hỏi. Đây là trạng thái kết nối hiện tại, chưa xác định là lỗi logic của app.
- AddSheet nhánh tự nhập: nhập thử `KiemTra` (đã xác nhận hiện trong ô), nhấn Back để đóng bàn phím rồi Back lần nữa. Sheet đóng thẳng về Vườn Nhà, không hỏi xác nhận bỏ nháp; dữ liệu nháp mất. **Không đạt, tái hiện một lần có ảnh trước/sau; phù hợp backlog M3.** Không lưu cây.
- AddSheet nhánh danh sách: vào bước 2, thấy trường `Chiều cao hiện tại` và `Vị trí đặt cây`; tiếp tục bước 3 và mở hộp thoại Xem lại. Nội dung loài và các việc gợi ý hiển thị. Chọn `Chỉnh lại` rồi thoát bằng Back, không xác nhận lưu. Các bước điều hướng và xem lại đạt; không kiểm tra việc lưu vào Room.
- Chi tiết cây và EditSheet mở được, sau đó thoát mà không lưu. Không kiểm tra cập nhật/xóa.
- Lịch tháng: chọn 28/10/2026 thấy `Kiểm tra độ ẩm đất`, cây `cây bàng`, còn 2 ngày. Chế độ Tuần chuyển được; lần quan sát tuần giữ ngày chọn 12/10/2026 nên chưa xác nhận ánh xạ việc sang tuần khác. Không bấm Hoãn/Hoàn thành.
- Không quan sát thấy app tự đóng/crash trong các màn đã đi qua. Logcat chưa cung cấp bằng chứng đủ sạch để kết luận không có lỗi nền; không ghi là kiểm tra crash toàn diện.
- Chưa kiểm tra camera/CameraToForm, Tra cứu, tài khoản/backup, gửi Trợ lý, hoàn thành/hoãn việc, lưu/sửa/xóa cây hoặc task, thông báo nền. Không gọi backend/AI và không làm thay đổi dữ liệu người dùng.
- Kết luận: phát hiện 1 lỗi UI có thể tái hiện là AddSheet bỏ nháp không cảnh báo; các luồng điều hướng, mở form/chi tiết, đi qua bước chọn loài → góc trồng → lịch chăm → Xem lại đều mở được. Không sửa lỗi theo yêu cầu “không sửa lặp đi lặp lại”. M2 vẫn chưa được xác nhận bằng Gradle; M3/M4 chưa làm. Chưa tạo sơ đồ hệ thống vì phần kiểm tra chưa hoàn tất.

### M3 — Xác nhận trước khi bỏ bản nhập AddSheet — 26/10/2026

- Đường thoát đã bảo vệ: Back ở bước 0, nút `‹ Vườn Nhà`/nút quay lại ở bước 0, và `onDismissRequest` của `ModalBottomSheet` (kéo/đóng sheet không toàn màn hình) đều gọi chung kiểm tra nháp. Có nháp thì hiện `Bỏ bản nhập?` với `Tiếp tục nhập` và `Bỏ`; không có nháp thì đóng thẳng. Back ở bước lớn hơn 0 vẫn lùi đúng một bước. Khi `saving`, handler tiếp tục bị vô hiệu hóa như trước và các thao tác đóng bị chặn.
- Nhánh quét ảnh được so với trạng thái ban đầu của `initialCandidate`: giá trị AI điền sẵn chưa sửa không được tính là nháp; thay đổi form, nhóm, lịch nhắc hoặc kế hoạch chăm sóc thì được tính. Trạng thái hộp thoại dùng `rememberSaveable`.
- Đã thêm hàm thuần `hasUnsavedDraft` và 7 kiểm thử JVM: trống/không đổi, nhập chữ, AI chưa sửa, AI đã sửa, chỉ đổi nhóm, chỉ đổi lịch nhắc, chỉ đổi kế hoạch. Đã thêm 7 instrumented Compose test cho Back + bàn phím, tiếp tục nhập, bỏ, form trống, nút quay lại, lùi bước và khôi phục hộp thoại/nháp. Test instrumented chỉ được biên dịch, chưa chạy trên thiết bị.
- File M3: sửa `app/src/main/java/com/example/vuonnha/ui/plants/PlantsScreen.kt`; tạo `app/src/main/java/com/example/vuonnha/ui/plants/DraftGuard.kt`, `app/src/test/java/com/example/vuonnha/ui/plants/DraftGuardTest.kt`, `app/src/androidTest/java/com/example/vuonnha/AddSheetDraftDiscardTest.kt`. M2 ở cùng `PlantsScreen.kt` và các file `LookupResultTitle*` không thuộc M3.
- Chạy `testDebugUnitTest assembleDebug lintDebug --offline --console=plain`: `BUILD SUCCESSFUL`; 64 JVM test, 0 thất bại, 0 lỗi. Chạy thêm `compileDebugAndroidTestKotlin --offline --console=plain`: `BUILD SUCCESSFUL`, test Compose biên dịch được. Kết quả nằm trong `docs/build-log.txt`. Lần chạy Gradle đầu bị từ chối truy cập cache; chạy lại với quyền cho cache được và thành công. Không chạy instrumented test, không cài APK, không thao tác thiết bị.
- `git diff --check` sạch. Chưa commit. Cần thử tay trên Note9 sau khi cài APK do người dùng build: nhập chữ → Back đóng bàn phím → Back mở hộp thoại; `Tiếp tục nhập` giữ chữ, `Bỏ` thoát; form trống thoát thẳng; xoay màn hình khi hộp thoại mở.

### Vá lỗ hổng M3 sau commit `e7bca59` — 26/10/2026

- Chọn ẩn thanh điều hướng dưới khi AddSheet đang mở ở route `plants`; không đổi `openTab`. AppNav nhận trạng thái từ PlantsScreen; thanh dưới trở lại khi đóng AddSheet. Có test JVM cho điều kiện hiển thị. Các luồng `scan` và `account` vẫn ẩn thanh dưới như trước.
- BackHandler của AddSheet toàn màn hình giờ vẫn bật khi `saving`; handler tiêu thụ Back nhưng không đổi bước/đóng form. Đã thêm Compose test kiểm tra form vẫn mở khi Back lúc đang lưu.
- Chip nhóm chỉ dọn khóa tham khảo khi `SpeciesGroup` thực sự đổi. Giữ `location`, `actualLight`, `pottingMix`, `actualHeight`, dọn cờ `editedFields` cho khóa đã loại. Test JVM kiểm tra bấm lại cùng nhóm không xóa dữ liệu và đổi nhóm giữ đúng các trường trên.
- Nhánh `!fullScreen` còn được các test/call site nội bộ dùng nên không xóa. Thêm `confirmValueChange`: khi nhắm tới `Hidden`, trạng thái saving từ chối; nháp khác ban đầu mở hộp thoại và từ chối ẩn; form không có nháp vẫn cho phép đóng. Thêm Compose test Back trên sheet thường khi có nháp.
- Gỡ `docs/build-log.txt` khỏi index bằng `git rm --cached`; file còn nguyên trên đĩa. Thêm `docs/build-log.txt` vào `.gitignore`. Log mới của lần chạy này: `docs/build-log-e7bca59-followup.txt`.
- Chạy `testDebugUnitTest assembleDebug lintDebug --offline --console=plain`: `BUILD SUCCESSFUL`, 69 test, 0 thất bại, 0 lỗi (đếm từ XML kết quả). Chạy `compileDebugAndroidTestKotlin --offline --console=plain`: `BUILD SUCCESSFUL`. **Test Compose chưa chạy** trên thiết bị/emulator; chỉ compile test Compose. Lần gọi Gradle đầu bị sandbox chặn cache; lần chạy lại với quyền đọc cache thành công. `git diff --check` sạch.
- File thay đổi cho vá: `.gitignore`, `app/src/main/java/com/example/vuonnha/ui/nav/AppNav.kt`, `app/src/main/java/com/example/vuonnha/ui/plants/PlantsScreen.kt`, `app/src/main/java/com/example/vuonnha/ui/plants/SpeciesChange.kt`, `app/src/test/java/com/example/vuonnha/ui/nav/BottomBarVisibilityTest.kt`, `app/src/test/java/com/example/vuonnha/ui/plants/SpeciesChangeTest.kt`, `app/src/androidTest/java/com/example/vuonnha/AddSheetDraftDiscardTest.kt`, và log mới. M2 (`LookupResultTitle*`) giữ nguyên, không thuộc commit vá này.

### M2 và các sửa lỗi AddSheet B1–B3 — 26/10/2026

- M2: thêm `lookupResultTitle` để tên khoa học null/rỗng/blank/literal `null` không tạo chữ hoặc dấu phân cách thừa; test JVM 5 trường hợp. Commit riêng `9eb1b017e27e444ff3ecb71d942d82c9c3df05e8`.
- A2: cập nhật `.gitignore` thành `docs/build-log*.txt` và ghi trạng thái xác minh HEAD vào tài liệu. Commit riêng `e4b7f915f817e3319592a5410e57ee07e20aa6df`.
- B1: `LookupRequestState`/token tăng khi bắt đầu hoặc hủy; hủy job trước request mới; coroutine cũ chỉ được ghi kết quả/lỗi/trạng thái nếu token còn hiện hành. Test cổng token gồm completion cũ sau khi bắt đầu request mới và hủy rồi bắt đầu lại. Commit `130a960482e90c10ba0bc2a9bb6ca08abfdbd7ac`.
- B2: hàm thuần bỏ gợi ý xóa khóa tham khảo và `source`, giữ `actualHeight`/các trường môi trường, dọn cờ chỉnh sửa đã xóa, chỉ bỏ lịch khi còn bằng lịch AI ban đầu; thao tác hủy lookup đang chạy. Ba test JVM. Commit `2e0ce9e04ee20cdc8ff5896e16642a80014fe8bb`.
- B3: lưu tên tự điền bằng `rememberSaveable`; cùng một hàm quyết định tên được dùng khi chọn loài trong danh sách, chọn kết quả lookup kể cả single-result, và bỏ gợi ý AI. Bốn test JVM cho tên trống, tên tự điền, tên người dùng sửa, và đổi liên tiếp nhiều loài. Commit `bf9552391ae4d2b53b7d1108c403b746b8ff3968`.
- Chạy `testDebugUnitTest assembleDebug lintDebug --offline --console=plain` trên checkout chính và chạy lại cùng bộ lệnh trên worktree sạch tại HEAD cuối `bf9552391ae4d2b53b7d1108c403b746b8ff3968`: cả hai `BUILD SUCCESSFUL`. Đếm các XML trong `app/build/test-results/testDebugUnitTest`: 78 test, 0 thất bại, 0 lỗi. Log hai lượt: `docs/build-log-b-fixes.txt` (bị ignore theo pattern mới).
- Vì B1–B3 chạm `PlantsScreen.kt`, đã build trên worktree sạch; chép `local.properties` mà không đọc, sau đó gỡ worktree. Git gỡ metadata nhưng báo giới hạn tên đường dẫn Windows khi xóa báo cáo Gradle; dừng daemon và xóa phần checkout còn lại trong đúng thư mục worktree. `git worktree list` chỉ còn checkout chính. Không dùng `git clean`/`git reset --hard`.
- **Test Compose/instrumented chưa chạy**; lượt C1 chỉ chạy unit test, assemble và lint. Không push. HEAD cuối là commit B3 nêu trên trước commit tài liệu này.

### Xác minh HEAD và nhánh AddSheet thường — 26/10/2026

- Đổi quy tắc ignore từ `docs/build-log.txt` thành `docs/build-log*.txt`. Kết quả `git ls-files docs/build-log.txt`: không có dòng nào (đường dẫn không còn được Git track). File log cũ vẫn còn trên đĩa và bị ignore.
- Tạo worktree sạch detached tại HEAD `8474b1d75e6618cf49641d85657cfb6a9ff74db0`, chép `local.properties` mà không mở/đọc nội dung, rồi chạy `testDebugUnitTest assembleDebug --offline --console=plain`. Kết quả: `BUILD SUCCESSFUL`, 64 test, 0 thất bại, 0 lỗi; APK debug build thành công. Log: `docs/build-log-head-check-8474b1d.txt`.
- HEAD tự build được; không thiếu `LookupResultTitle`. Vì vậy không commit M2. Các file M2 đang ở working tree chính vẫn chưa commit.
- Rà toàn bộ call site production: `PlantsScreen.kt` và `PlantScanScreen.kt` đều gọi AddSheet với `fullScreen = true`. Không có file production nào gọi `fullScreen = false`; nhánh mặc định `!fullScreen` chỉ còn được dùng bởi các test. Không cần thêm `rememberUpdatedState` cho production call site.
- Test Compose vẫn **chưa chạy**; lần xác minh này chỉ chạy JVM test và assembleDebug. Worktree được gỡ sau khi kiểm tra; không dùng `git clean` hoặc `git reset --hard`.

### B3 bổ sung — tên gọi vườn theo loài hiện tại — 26/10/2026

- Thêm hàm thuần `effectivePlantName(name, nameAutofilled, currentSpeciesName)`: tên trống hoặc còn bằng giá trị hệ thống tự điền thì theo tên loài hiện tại; tên gọi người dùng đã sửa được giữ nguyên.
- Dùng cùng kết quả cho dòng “Tên trong vườn” ở hộp xem lại, thẻ tóm tắt bước 3 và `onSave`. Hàm chỉ tính giá trị dẫn xuất, không cập nhật state `name` khi người dùng gõ.
- Thêm 4 test JVM: đổi tên loài bằng cách gõ sau tra cứu, sửa tên loài sau quét ảnh, giữ tên gọi riêng, và dùng tên loài khi tên gọi để trống.
- File sửa: `app/src/main/java/com/example/vuonnha/ui/plants/AutofilledPlantName.kt`, `app/src/main/java/com/example/vuonnha/ui/plants/PlantsScreen.kt`, `app/src/test/java/com/example/vuonnha/ui/plants/AutofilledPlantNameTest.kt`.
- Chạy `./gradlew.bat testDebugUnitTest assembleDebug lintDebug --offline --console=plain`: `BUILD SUCCESSFUL`. Cộng các XML trong `app/build/test-results/testDebugUnitTest`: 82 test, 0 thất bại, 0 lỗi. Log: `docs/build-log-b3-name-final.txt` (được ignore). Test Compose/instrumented không nằm trong lệnh này và vẫn chưa chạy.
- Commit code/test riêng: `1eaa128a5865befc2b06e791b816480aaf66f47b` — `fix: tên vườn theo tên loài hiện tại`. Không push, không sửa `PlantLookupRepository`.

### B3 các chi tiết còn lại và rà logo — 26/10/2026

- Phần 1: thẻ tóm tắt bước 3, dòng “Tên trong vườn” ở hộp xem lại và `submit()`/`onSave` đều dùng `finalPlantName` từ `effectivePlantName`; không còn chỗ nào trong ba điểm này dùng `name` thô. Ô nhập và validation độ dài vẫn đọc state `name`.
- Phần 2 (commit `530cfc9e0ee3c6192cdbc238c82c1fd2ee104c96`): thêm hàm thuần cho gợi ý “Sẽ lưu là: …”; dùng profile đã chiếu thành `savedProfile` cho validation và thông báo độ dài; chuẩn hoá tên tự điền và tên loài bằng `trim()` trong helper, đồng thời chuẩn hoá `nameAutofilled` lúc khởi tạo từ ảnh. Thêm test JVM cho tên AI có khoảng trắng, tên riêng do người dùng đặt, gợi ý tên, tham chiếu quá dài không được lưu và trường thực sự lưu quá dài.
- Phần 3 logo chưa thực hiện: đường dẫn nguồn vẫn là placeholder `<ĐIỀN ĐƯỜNG DẪN THƯ MỤC app\src\main CỦA BẢN LOGO>`, không có thư mục nguồn xác định để kiểm tra/chép. Không thay đổi asset, XML icon hay manifest; chưa thể xác minh logo trên điện thoại.
- Thử tay các thay đổi B3 trên Note9: chưa chạy. Test Compose/instrumented: chưa chạy. Cần người dùng cung cấp đường dẫn nguồn logo và sau đó thử thao tác nhập tên/đổi loài/xem trước/lưu trên Note9 nếu cần xác nhận giao diện.
- Chạy `.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --offline --console=plain` trên checkout chính: `BUILD SUCCESSFUL`; tổng cộng từ XML `app/build/test-results/testDebugUnitTest`: 87 test, 0 thất bại, 0 lỗi. Log `docs/build-log-final-tasks.txt` (được ignore). `assembleDebug` và `lintDebug` thành công cho mã hiện có; do không có nguồn/không thay đổi asset, đây không phải xác minh logo.
- Worktree sạch tại HEAD `ee2174d21d7d75d18088200691ccbbb42b7bd8a3`: `BUILD SUCCESSFUL`; cộng XML tại worktree: 87 test, 0 thất bại, 0 lỗi. Log `docs/build-log-clean-head-final.txt` (được ignore). Worktree đã gỡ; `git worktree remove` gặp lỗi xóa báo cáo Windows `Invalid argument`, dừng Gradle daemon rồi xóa đúng thư mục còn sót. Không dùng `git clean` hoặc `git reset --hard`.

### Logo mới và sao lưu Git — 26/10/2026

- Người dùng cung cấp `C:\Users\DELL\Downloads\vuonnha-logo.zip`. Trước khi chép, checkout lồng không có thay đổi logo; archive có đúng 18 mục được yêu cầu. Đã chép đúng 18 file vào `app/src/main`, không giải nén file khác.
- Kiểm tra sau chép: đúng 18 đường dẫn thay đổi/thêm; cả hai adaptive XML có background `@drawable/ic_launcher_background`, foreground `@mipmap/ic_launcher_foreground`, không có `<monochrome>`; không có `mipmap-anydpi-v26`; drawable foreground/background hiện có không đổi; manifest vẫn trỏ `@mipmap/ic_launcher` và `@mipmap/ic_launcher_round`; `git check-ignore` không chặn file nào.
- Commit logo riêng: `1f527fb4e12ad47183e9cb041acab12a4ae925e7` — `feat: thêm logo Vườn Nhà` (18 file).
- Chạy `.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --offline --console=plain`: `BUILD SUCCESSFUL`; cộng XML `app/build/test-results/testDebugUnitTest`: 87 test, 0 thất bại, 0 lỗi. Log `docs/build-log-logo.txt` (được ignore). Lint có một cảnh báo `ApplySharedPref` trong `data/AccountRepository.kt`; không có cảnh báo icon/launcher/mipmap/mật độ/kích thước ảnh.
- Đã tạo và xác minh bundle đầy đủ tại `D:\Backup\VuonNha_1-2026-10-26.bundle`; `git bundle verify` xác nhận complete history với 5 refs. Bundle chỉ lưu lịch sử Git.
- Agent hiện thực hiện là Codex; `AGENTS.md` đã ghi đúng trạng thái nên không cần sửa. Các dòng lịch sử có tác giả `opencode` trong mục 8 của HANDOFF được giữ nguyên.
- Chưa xem logo trên điện thoại thật; test Compose/instrumented chưa chạy. Việc còn lại phía người dùng: tự thay Gemini key và `SCAN_TOKEN` theo quy trình của mình, cài/chạy APK trên Note9 để kiểm tra icon thực tế và các luồng UI.

### Mở ứng dụng trên Note9 — 26/10/2026

- ADB nhận Note9 `271c3b59c41c7ece` (`SM_N960F`, `device`). `am start -W` mở `com.example.vuonnha/.ui.MainActivity` với `Status: ok`; ảnh chụp hiển thị màn Vườn Nhà.
- Logcat sau khi mở không có `FATAL EXCEPTION` hoặc ANR ứng dụng. Có một dòng thông tin `AndroidRuntime: VM exiting with result code 0`, không kèm crash.
- Theo giới hạn hiện có, không cài APK và không chạm/gõ/vuốt UI. Kết quả chỉ xác nhận app đang cài khởi động và hiển thị; **không xác minh logo mới** vì chưa cài APK vừa build. Test Compose/instrumented và các luồng tương tác vẫn chưa chạy.

### Chạy test trên Note9 theo yêu cầu — 26/10/2026

- Chạy `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`: `BUILD SUCCESSFUL`. Tổng JVM test cộng XML `app/build/test-results/testDebugUnitTest`: 87 test, 0 thất bại, 0 lỗi. Instrumented APK biên dịch thành công.
- Đã cập nhật APK debug và test APK trên Note9 bằng cài đặt thay thế, không gỡ app/không xóa dữ liệu. Chạy 6 lớp instrumented dùng dữ liệu giả hoặc Room trong bộ nhớ: `BackupTransactionTest`, `AddSheetDraftDiscardTest`, `EditablePlantProfileTest`, `PlantScanFormTest`, `RecognitionFlowTest`, `ExampleInstrumentedTest`: **27 test đạt**.
- Chạy riêng `CameraToFormTest`: **1 test thất bại** tại bước tìm `Tự nhập tên cây` sau khi mở `Quét cây`, với `IllegalStateException: No compose hierarchies found in the app` (dòng 17 test). Log không ghi nhận `FATAL EXCEPTION`/ANR của ứng dụng; chưa xác định nguyên nhân. Mở lại `MainActivity` sau đó thành công.
- Lint hoàn tất: 0 lỗi, 35 cảnh báo. Hai cảnh báo `MonochromeLauncherIcon` ở adaptive icon XML do không có `<monochrome>`; thẻ này được bỏ theo yêu cầu logo vì foreground là ảnh đặc. Các cảnh báo còn lại thuộc các vấn đề hiện có, không sửa trong lượt kiểm tra.
- Không chạy `CalendarDiaryTest` vì bài này ghi/xóa trên database thật của app; không chạy `GardenNavigationTest` vì ghi ảnh chụp cố định vào external app storage; không chạy `AccountFlowTest`/`AccountBackupTest` vì cần backend cô lập cổng 8788; không chạy audit AI/backend/notification.
- Tổng đã thực thi ở lượt này: 87 JVM test đạt và 28 instrumented test, trong đó 27 đạt, 1 thất bại. Không chạy hết mọi instrumented test; các luồng Tra cứu, tài khoản/backup, lịch ghi chú thật, thông báo và các luồng AI/backend chưa được xác minh. App hiện đã được mở lại ở màn chính. Không sửa mã nguồn, không commit, không push.

### Review UI/UX Pro Max trên Note9 — 26/10/2026

- Áp dụng lần review đầu theo hướng dẫn Jetpack Compose và UX của UI UX Pro Max; không cài CLI, không thêm dependency. Ảnh kiểm tra tạm nằm trong `app/build/ui-review-*.png` (thư mục build bị ignore).
- Quan sát màn Vườn Nhà ở dark theme: nhận diện xanh lá đồng bộ, tiêu đề/summary/CTA/phím tắt tách bạch; vùng nội dung đầu màn chiếm nhiều chiều cao nên mới thấy trọn một thẻ cây và một phần thẻ kế tiếp trên Note9. Đây là điểm cân nhắc nếu ưu tiên lướt danh sách nhanh, chưa đủ căn cứ để đổi bố cục.
- AddSheet: nhãn trường, trạng thái nhóm và bước hiện tại dễ nhận ra; khi bàn phím mở, nút “Tiếp tục” vẫn còn trong vùng nhìn thấy. Back hai lần hiện đúng “Bỏ bản nhập?”; “Tiếp tục nhập” giữ nguyên `KiemTra`, sau đó bản thử được bỏ và app trở về màn chính. Không lưu hay sửa dữ liệu vườn.
- Điểm nên đo tiếp: tương phản theo WCAG ở cả sáng/tối; cách chip/nhãn dài xuống dòng với cỡ chữ lớn; vùng chạm thực tế của nút hoàn thành việc có kích thước hiển thị 40dp (Material có thể tự mở rộng vùng chạm nên chưa kết luận lỗi). Không thay đổi UI; chưa chạy test thị giác/accessibility tự động.

### Rà soát test sâu theo yêu cầu — 26/10/2026

- Chạy lại toàn bộ `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain --rerun-tasks`: `BUILD SUCCESSFUL` sau 4m26s; 88 task thực thi. Cộng XML `app/build/test-results/testDebugUnitTest`: 87 test, 0 thất bại, 0 lỗi. Log `docs/build-log-deep-audit.txt` (được ignore).
- Lint: 0 lỗi, 35 cảnh báo; có 2 `MonochromeLauncherIcon` theo quyết định logo trước đó. Kotlin compiler cũng cảnh báo một điều kiện luôn true ở `PlantScanScreen.kt:129` và constructor `Locale(String,String)` deprecated ở màn nhắc/thời tiết; chưa sửa trong lượt test.
- Cài cập nhật APK/test APK bằng `install -r` giữ dữ liệu; 6 lớp instrumented an toàn đạt 27/27 lần nữa: backup trong Room in-memory, AddSheet bỏ nháp, hồ sơ, form scan giả lập, recognition giả lập và context.
- Điều tra `CameraToFormTest`: quyền `android.permission.CAMERA` hiện `granted=false`. Test không cấp/xử lý quyền trước khi truy vấn cây Compose sau khi mở màn quét; lần chạy trước báo `No compose hierarchies found` ở dòng 17. Đây có thể là hộp thoại quyền hệ thống làm test mất cây Compose, chưa chứng minh lỗi luồng camera của app. Không tự cấp quyền; cần người dùng cấp quyền Camera trên Note9 rồi chạy lại bài này để xác nhận.
- Không chạy test có ghi/xóa vào DB thật (`CalendarDiaryTest`), test tạo ảnh trong external app storage (`GardenNavigationTest`), test account cần backend cô lập, hoặc audit AI/backend/thông báo. Vì vậy chưa xác minh hết mọi luồng tích hợp; không gọi backend/AI và không sửa dữ liệu vườn.
- Mở `MainActivity` cuối lượt trả `Status: ok`; logcat hiện tại không có `FATAL EXCEPTION`/ANR của app. Không sửa mã nguồn, không commit/push; chỉ cập nhật HANDOFF. Bước tiếp theo an toàn: người dùng cấp quyền camera nếu muốn kiểm camera, sau đó chạy lại riêng `CameraToFormTest`; các luồng lưu/xóa trên DB thật và backend cần môi trường dữ liệu thử riêng.

- Theo yêu cầu mới: camera hoãn, để sau. Không chạy CameraToFormTest trong lượt kiểm chứng tiếp theo; mọi lần chạy instrumented sẽ loại lớp này bằng đối số notClass.

### Luồng repository vườn — Room in-memory — 27/10/2026

- Thêm hai ca instrumented vào BackupTransactionTest: thêm cây theo loài có sẵn và tự nhập (lịch rỗng dùng chu kỳ mặc định), sửa cây, thêm/xóa việc, hoàn thành tạo log, xóa cây cascade việc/log/nhật ký, và saveItemEdit từ form có task ID đã bị xóa.
- Các bài dùng Room in-memory; không truy cập DB vườn thật. Biên dịch instrumented test Kotlin thành công. Không chạy được trên thiết bị: Gradle báo No connected devices; do đó chưa có kết quả runtime cho các bài repository mới. Không chạy CalendarDiaryTest hoặc bất kỳ bài ghi DB thật nào.
- Không thêm biến thể .audit: chưa có sự đồng ý, và các bài này dùng DB trong bộ nhớ. Camera hoãn, để sau.
- Account backup hiện có round-trip Room in-memory, rollback khi nhập lỗi giữa chừng và kiểm thử giới hạn decompression; sẽ bổ sung bài ZIP hỏng riêng trong phần sao lưu.
- Bước tiếp theo: bổ sung phần test backup còn thiếu; khi thiết bị kết nối lại, chạy lớp BackupTransactionTest với CameraToFormTest bị loại.

### Backup limits và phục hồi ZIP — 27/10/2026

- Thêm bài instrumented với Room in-memory: ZIP thiếu data.json bị từ chối và cây đang có được giữ nguyên. Các bài sẵn có bao gồm round-trip export/import, rollback transaction khi lỗi giữa chừng; BackupLimitsTest kiểm giới hạn giải nén.
- Chạy 	estDebugUnitTest --tests com.example.vuonnha.data.BackupLimitsTest --offline --console=plain: BUILD SUCCESSFUL. Bài instrumented mới chỉ biên dịch được, chưa chạy vì hiện không có thiết bị kết nối. Không chạy account/backend thật.
- Đọc AccountRepository: session được mã hóa; commit() đồng bộ là có chủ đích để chỉ cập nhật AccountSession khi dữ liệu đã ghi thành công. Giữ nguyên thay vì đổi sang pply(), vì pply() không đảm bảo ghi xong trước khi trả về và có thể làm thay đổi hành vi.
- Không đổi API hay dùng key/token. Việc đăng nhập/sao lưu tài khoản qua mạng chưa được kiểm chứng.

### Hợp đồng tra cứu backend với phản hồi giả — 27/10/2026

- Bổ sung kiểm thử lookup nhận danh sách rỗng; từ chối field thiếu; báo lỗi JSON hỏng và TimeoutError. Tất cả dùng fetchImpl giả; test server hiện có xác nhận token sai trả 401 và luồng hợp lệ dùng lookupImpl giả.
- Chạy 
ode --test --test-isolation=none plant-lookup.test.mjs: 3 test đạt, 0 thất bại. Không đọc .env, không mở backend/data, không gọi AI/Gemini hoặc mạng provider thật; không cần khởi động backend riêng.
- Thử 
ode --test plant-lookup.test.mjs không chạy được do Windows trả spawn EPERM; chạy lại với isolation none đã thành công.

### Chính sách chọn việc nhắc — 27/10/2026

- Tách hàm thuần chọn việc đến hạn theo thời điểm/múi giờ và tập cây còn tồn tại; loại trùng task ID; tách điều kiện chặn thông báo trùng ngày (ngoại lệ khi force). ReminderWorker dùng cùng logic này.
- Thêm ReminderPolicyTest: ngày địa phương qua DST, bỏ việc của cây đã xóa, khử trùng lặp, chặn nhắc hai lần cùng ngày, và lịch mới sau khi hoàn thành.
- Chạy 	estDebugUnitTest --tests com.example.vuonnha.domain.ReminderPolicyTest --offline --console=plain: BUILD SUCCESSFUL. Chưa thử phát thông báo thật trên Note9; người dùng tự thử quyền thông báo, giờ nhắc và thao tác Xong/Hoãn.


### Bản vá AddSheet/EditSheet và cảnh báo lint — 27/10/2026

- AddSheet: ghi nguồn lịch tự nhập do gợi ý ban đầu/tra cứu và cờ người dùng đã chỉnh. Khi kết quả loài mới được chọn, lịch tự sinh chưa chỉnh được thay bằng lịch gợi ý mới (kể cả danh sách mới rỗng); lịch do người dùng chỉnh được giữ. Có test JVM cho cả hai trường hợp.
- EditSheet: kiểm tra mọi trường profile theo giới hạn 1000 ký tự, chặn Lưu nếu vượt; khi đổi loài tự nhập, giữ nguyên profile và hiện nhắc rà thông tin tham khảo. Test thuần xác nhận giới hạn và điều kiện nhắc.
- Dọn cảnh báo điều kiện !manual đã thừa trong nhánh camera sau các return; thay constructor Locale deprecated bằng Locale.forLanguageTag("vi-VN"). Test JVM xác nhận language/country.
- Các test JVM mục này đạt với BUILD SUCCESSFUL. Chưa kiểm thử UI EditSheet hay instrumented trên thiết bị do Gradle không tìm thấy thiết bị kết nối. Chưa phát thông báo thật. Camera hoãn, để sau.

### Kiểm chứng các phần còn lại — 27/10/2026

- Chạy `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`: BUILD SUCCESSFUL; 88 task (18 thực thi, 70 up-to-date). `assembleDebugAndroidTest` chỉ biên dịch APK test; không thực thi instrumented.
- Cộng XML `app/build/test-results/testDebugUnitTest`: 97 test, 0 thất bại, 0 lỗi, 0 bỏ qua. Log: `docs/build-log-complete.txt` (ignored). Lint: 0 lỗi, 35 cảnh báo. Còn ApplySharedPref (giữ commit đồng bộ vì cần ghi xong mới cập nhật session), 2 cảnh báo thiếu monochrome launcher theo thiết kế ảnh đặc, và cảnh báo lint có sẵn khác. Cảnh báo Locale deprecated và điều kiện `!manual` đã được dọn.
- Backend fake: `node --test --test-isolation=none plant-lookup.test.mjs server.test.mjs`: 15 test đạt, 0 thất bại. Lookup dùng fetch giả; server test chỉ dùng handler/fetch giả và localhost; không gọi Gemini/provider thật, không dùng `.env`/`backend/data`.
- Room test additions use in-memory DB/temp folder. Instrumented class compiles, but `connectedDebugAndroidTest` could not execute because Gradle reported `No connected devices`. No test against the installed garden database was run. Không chạy `clearPackageData`.
- Không thêm applicationIdSuffix `.audit`: chưa có đồng ý; test được viết với Room in-memory và dữ liệu/tệp tạm. Không đổi schema Room, API công khai hoặc dependency.
- Reminder worker pure-selection tests pass. Chưa thử phát thông báo thật trên Note9. Người dùng tự thử: luồng thêm/sửa/xóa cây và việc trên app, tra cứu rồi đổi loài để kiểm lịch, EditSheet đổi tên loài và rà tham khảo, quyền/giờ/thao tác thông báo. Camera hoãn, để sau.
- Git bundle lịch sử được tạo và xác minh tại `D:\Backup\VuonNha_1-2026-10-27.bundle`; sẽ tạo lại sau commit tổng kết này để bao gồm HEAD cuối. Bundle chỉ chứa lịch sử Git. Không push.
- Việc còn chưa kiểm chứng: instrumented runtime do thiết bị không kết nối; các thao tác UI EditSheet và lịch lookup chưa thử tay trong lượt này; notification delivery thật; camera; account/network integration. Không kết luận mọi lỗi đã hết.
- Build trên worktree sạch ở HEAD trước thất bại ngay khi cấu hình: SDK location not found (local.properties vắng trong worktree và ANDROID_HOME không có). Không mở/sao chép local.properties. Đã gỡ worktree tạm.
- Bundle cuối tại `D:\Backup\VuonNha_1-2026-10-27.bundle` được tạo lại và `git bundle verify` thành công sau commit tổng kết cuối.
- Bổ sung assertion `Bearer wrong-token` cho lookup; chạy lại `node --test --test-isolation=none plant-lookup.test.mjs server.test.mjs`: 15 đạt, 0 thất bại. Phản hồi provider vẫn là dữ liệu giả.

## 22. Giao Codex: dọn tài liệu và chạy test trên Note9 — 28/10/2026 · Claude (chat)

Claude đã đọc đến hết mục 19 và các mục bổ sung. Việc lần này: (A) dọn tài liệu cho đúng hiện trạng; (B) cài bản mới và chạy test trên Note9 theo danh sách an toàn; (C) báo cáo ở mục 23. Người dùng cho phép Codex tự chạy Gradle và adb trong phạm vi dưới đây (nới quyền so với mục 20).

### Quyền (bắt buộc)

**Được đọc/ghi:** `D:\Hoc_Tap\VuonNha_1\VuonNha_1\` (mã, test, docs). **Chỉ đọc:** `local.properties` KHÔNG mở; `build.gradle*`, `backend\*.mjs` được đọc.

**Cấm tuyệt đối:** mọi thư mục ngoài `D:\Hoc_Tap\VuonNha_1\VuonNha_1\` (đặc biệt `D:\Hoc_Tap\mobile_giao_vien`), `.env`, keystore, `backend\data\`, `.audit\`, `C:\Users\DELL\` (trừ file tạm của chính Codex). Gradle được dùng `GRADLE_USER_HOME=D:\Android\gradle` và `D:\Backup\` (chỉ ghi file bundle). Không đọc/in Gemini key, `SCAN_TOKEN`, token.

**Git:** `status`, `diff`, `log`, `add <file cụ thể>`, `commit`, `bundle create/verify`. Cấm `push`, `reset --hard`, `clean`, `stash`, `git add -A`/`git add .`; không stage `.audit\`, log build, `local.properties`.

**Gradle:** được chạy `.\gradlew.bat ... --offline --console=plain`. Nếu bị `Access is denied` ở khóa wrapper: ghi mục 23 và dừng ở phần B (phần A vẫn làm); đừng tìm cách vòng.

**Thiết bị:** chỉ serial `271c3b59c41c7ece`, luôn dùng `-s 271c3b59c41c7ece`. Được: `adb devices -l`; `adb install -r` APK debug và test APK (giữ dữ liệu); `am start`; `am instrument -w`; `screencap`; `logcat -c/-d` lọc `com.example.vuonnha`. **Cấm:** `uninstall`, `pm clear`, `pm revoke/grant`, `shell rm`, `shell input ...`, `reverse`, **`connectedDebugAndroidTest` / `connectedAndroidTest`** (Gradle task này thường gỡ app và test APK sau khi chạy, có thể xóa dữ liệu vườn thật trên máy; dùng `install -r` + `am instrument` như lần 26/10), mọi lệnh gọi backend/AI.

**Khác:** không đổi schema/API/dependency; không sửa mã ứng dụng trong lượt này trừ khi test fail và nguyên nhân rõ (khi đó dừng, ghi mục 23, hỏi); không gọi AI; không push.

### A. Dọn tài liệu (không cần thiết bị)

1. `git status`, `git log --oneline -25`, ghi HEAD thật và working tree thật.
2. Viết lại **mục 1** (trạng thái Git) đúng HEAD hiện tại, số commit, file chưa commit (nếu có, nêu tên và lý do). Cập nhật dòng "Cập nhật" ở đầu file (ngày 28/10/2026, Codex).
3. Viết lại **mục 3** (Lỗi còn tồn tại) và **mục 5** (Công việc tiếp theo) theo hiện trạng mã, đối chiếu mã thật; dấu đánh dấu rõ mục nào đã xong (kèm hash), mục nào còn mở. Bỏ các mục P0/P1 đã xử lý.
4. Ghi "Đóng" lô nhỏ #1 (mục 20–21): M1–M4 xong hay chưa, hash tương ứng.
5. Thêm mục ngắn "Việc còn mở" (danh sách số đánh số): camera (hoãn), A2 timeout tra cứu, backend HTTPS, `ReminderWorker`/Doze, đăng nhập & backup qua mạng thật, đổi Gemini key + `SCAN_TOKEN` (người dùng tự làm), nhãn cá/thú (cần quyết định UX).
6. Commit tài liệu riêng: `docs: cập nhật HANDOFF theo hiện trạng 28/10`. Chỉ stage `docs/HANDOFF.md`.

### B. Cài bản mới và chạy test trên Note9 (an toàn dữ liệu)

1. `adb devices -l`: nếu serial không ở trạng thái `device`, ghi mục 23 và dừng phần B.
2. Chạy `.\gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain` (log vào `docs/build-log-*.txt`, đã ignore). Cộng XML `app\build\test-results\testDebugUnitTest` lấy tổng test thật (kỳ vọng 97).
3. `adb -s 271c3b59c41c7ece install -r` APK debug (`app\build\outputs\apk\debug\app-debug.apk`) và test APK (`app\build\outputs\apk\androidTest\debug\*.apk`). Không gỡ app.
4. `adb -s 271c3b59c41c7ece logcat -c`, rồi chạy **chỉ các lớp an toàn** (Room in-memory hoặc dữ liệu giả): `BackupTransactionTest`, `AddSheetDraftDiscardTest`, `EditablePlantProfileTest`, `PlantScanFormTest`, `RecognitionFlowTest`, `ExampleInstrumentedTest`, bằng `am instrument -w -e class <danh sách cách nhau dấu phẩy> com.example.vuonnha.test/androidx.test.runner.AndroidJUnitRunner`. Trước khi chạy, đọc từng lớp trong `app\src\androidTest` để xác nhận danh sách còn đúng (nếu có lớp mới, đọc mã: chỉ thêm vào nếu dùng Room in-memory/dữ liệu giả và không ghi DB/thư mục thật).
5. **KHÔNG chạy:** `CameraToFormTest` (hoãn), `CalendarDiaryTest` (ghi/xóa DB thật), `GardenNavigationTest` (ghi ảnh external storage), `AccountFlowTest`, `AccountBackupTest` (cần backend 8788), các audit AI/backend/notification (`liveAudit`, `accountAudit`, `flowAudit`, `notificationAudit`).
6. Ghi số test đạt/thất bại từng lớp từ output của `am instrument` (lỗi `OK (n tests)` hoặc `FAILURES!!!`; runner có thể exit 0 dù fail, dựa vào output). Nếu có fail: lấy logcat lọc theo `com.example.vuonnha` quanh thời điểm fail, phân biệt lỗi app với lỗi môi trường; chỉ chạy lại tối đa 1 lần.
7. `am start -W -n com.example.vuonnha/.ui.MainActivity`, `screencap` vào thư mục tạm của phiên (không đưa vào repo), `logcat -d` tìm `FATAL EXCEPTION`/ANR của app.
8. Tạo lại `D:\Backup\VuonNha_1-2026-10-28.bundle` bằng `git bundle create ... --all` và `git bundle verify`.

### C. Người dùng tự thử tay (Codex chép danh sách này vào mục 23, không tự làm vì bị cấm `input`)

1. Xem logo mới trên màn hình chủ và ngăn ứng dụng.
2. Thêm cây: chọn từ danh sách → nhập chiều cao → xem lại → lưu; mở lại cây, chiều cao còn.
3. Thêm cây tự nhập, tra cứu (cần backend + `adb reverse`, có thể timeout), đổi tên loài, kiểm tra lịch gợi ý đổi theo loài, lịch tự sửa giữ nguyên.
4. Sửa cây: đổi tên loài tự nhập (có nhắc rà thông tin), nhập thông tin quá 1000 ký tự (nút Lưu bị chặn), xóa một việc (hộp thoại "Xoá…/Huỷ"), xoá cả cây.
5. Thông báo nhắc: cấp quyền, đổi giờ, nhấn "Xong"/"Hoãn".
6. Trước khi thử, nên xuất ZIP sao lưu trong Cài đặt nếu dữ liệu vườn thật quan trọng.

## 23. Phản hồi của Codex (mục 22)

### Lần thử lại — 28/10/2026

- Đã đọc toàn bộ mục 22, gồm phần Quyền và các danh sách cấm. Chưa thể bắt đầu mục A: lệnh đọc/truy vấn từ phiên Codex (`Get-Content docs/HANDOFF.md` và `Get-Location`) treo quá thời gian công cụ; không lấy được `git status`, `git log` hoặc HEAD thực tế. Terminal PowerShell người dùng hiển thị đúng thư mục dự án nhưng không khắc phục được bộ chạy lệnh của phiên Codex.
- Theo yêu cầu dừng khi bị chặn: chưa viết lại mục 1/3/5, chưa đóng lô nhỏ #1, chưa thêm danh sách “Việc còn mở” hoàn chỉnh và chưa commit. Không thể báo hash commit tài liệu hoặc xác nhận trạng thái working tree.
- Phần B chưa chạy: không chạy Gradle, không cài APK, không gọi adb/instrumented; vì vậy không có tổng test JVM hay kết quả mới của từng lớp. Không có log build hay log thiết bị mới. Bản sao Git chưa được tạo/xác minh trong lần thử này.
- Thử tay người dùng sau khi tiếp tục được: (1) xem logo trên màn hình chủ và ngăn ứng dụng; (2) thêm cây từ danh sách, nhập chiều cao, xem lại/lưu rồi mở lại kiểm tra; (3) thêm cây tự nhập, tra cứu, đổi loài và kiểm tra lịch gợi ý/lịch đã sửa; (4) sửa cây, đổi tên loài, thử giới hạn 1000 ký tự, xóa một việc và xóa cây; (5) thử thông báo, đổi giờ, chọn “Xong”/“Hoãn”. Nếu dữ liệu vườn thật quan trọng, xuất ZIP trước khi thử.
- Tiếp tục khi phiên Codex có thể đọc repo và chạy Git trong thư mục dự án; trước tiên xác minh `git status`/HEAD rồi làm lại mục A. Camera vẫn hoãn; không chạy CameraToFormTest.

### Lần thực hiện hiện tại — 28/10/2026

- Trước sửa, HEAD `a24326f`; working tree chỉ có `docs/HANDOFF.md` chưa commit (mục 22–23 do lượt trước thêm). Đã đọc mục 22 gồm Quyền, đọc quy tắc repo, đối chiếu mã AddSheet/EditSheet và lịch sử Git. Viết lại mục 1, 3, 5, đóng M1–M4, thêm “Việc còn mở”. `git diff --check` không báo lỗi nội dung. Commit riêng **chỉ `docs/HANDOFF.md`**: `470017bcc0bab4f9d2d398f4e565eca74c37db70` (`docs: cập nhật HANDOFF theo hiện trạng 28/10`). HEAD tại điểm dừng trước khi ghi bổ sung mục này: `470017b`; không có thay đổi mã ứng dụng/test.
- Đã đọc đủ sáu lớp instrumented được chỉ định: `BackupTransactionTest` dùng Room in-memory và tệp tạm trong cache; các lớp Compose dùng dữ liệu giả/callback cục bộ; `ExampleInstrumentedTest` chỉ đọc package name. Không thêm lớp khác.
- Bị chặn tại bước B1: `adb devices -l` trả “The term 'adb' is not recognized”. Tìm `adb.exe`/`.bat`/`.cmd` **chỉ trong repo** không thấy. Giới hạn hiện hành cấm thư mục ngoài repo, nên không tìm hoặc gọi binary Android SDK ở ngoài. Chưa xác nhận serial `271c3b59c41c7ece` ở trạng thái `device`; dừng phần B theo yêu cầu.
- **Kết quả lần này:** chưa chạy Gradle, nên không có tổng JVM test mới hoặc kết quả build/lint mới; chưa cài APK, chưa chạy `am instrument`, nên từng lớp `BackupTransactionTest`, `AddSheetDraftDiscardTest`, `EditablePlantProfileTest`, `PlantScanFormTest`, `RecognitionFlowTest`, `ExampleInstrumentedTest` đều **chưa chạy (0 kết quả mới)**. Chưa mở app/chụp màn hình/logcat. Không có test fail để phân tích. Không tạo/xác minh bundle vì đường dẫn `D:\Backup\` nằm ngoài phạm vi người dùng cho phép ở lời nhắn hiện hành. Không gọi backend/AI, không push và không đụng dữ liệu ứng dụng.
- Lệnh đã chạy: đọc tài liệu/mã, `git status`, `git log --oneline -25`, `git diff --check`, `git diff --stat HEAD`, `git add docs/HANDOFF.md`, `git commit`, `adb devices -l`, và `rg --files` giới hạn trong repo. Bước tiếp theo: khi `adb` khả dụng trong phạm vi được phép, kiểm tra đúng serial rồi tiếp tục B theo thứ tự mục 22; không chạy các lớp bị cấm.
- Danh sách người dùng tự thử tay sau khi có bản cài: (1) xem logo mới trên màn hình chủ và ngăn ứng dụng; (2) thêm cây từ danh sách, nhập chiều cao, xem lại/lưu rồi mở lại kiểm tra; (3) thêm cây tự nhập, tra cứu trong môi trường backend do người dùng chuẩn bị, đổi loài và kiểm tra lịch gợi ý thay đổi/lịch đã sửa được giữ; (4) sửa cây, đổi tên loài và rà thông tin, thử giới hạn 1000 ký tự, xác nhận “Xoá/Huỷ” một việc rồi xóa cây; (5) cấp quyền thông báo, đổi giờ, thử “Xong”/“Hoãn”. Nếu dữ liệu vườn thật quan trọng, xuất ZIP sao lưu trong Cài đặt trước khi thử.

### Tiếp tục phần B theo quyền đường dẫn mới — 28/10/2026

- Đã đọc lại mục 22–23; đầu lượt HEAD `b0990b98d7f29106710409f3e7465252828e586e`, working tree sạch. Phần A đã hoàn tất ở hai commit tài liệu trước.
- B1: chạy đúng file được cấp quyền `C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe` với `devices -l`. ADB khởi động daemon và liệt kê serial `271c3b59c41c7ece` ở trạng thái **`unauthorized`** (`transport_id:1`), không phải `device`. Theo điều kiện dừng rõ trong B1, dừng phần B tại đây; không thử thao tác trên điện thoại hoặc serial khác.
- B2–B8 **chưa chạy**: chưa chạy Gradle, nên không có tổng JVM test/build/lint mới; chưa cài hai APK; cả sáu lớp `BackupTransactionTest`, `AddSheetDraftDiscardTest`, `EditablePlantProfileTest`, `PlantScanFormTest`, `RecognitionFlowTest`, `ExampleInstrumentedTest` đều chưa có kết quả instrumented mới; chưa mở app/chụp màn hình/đọc logcat; chưa tạo hoặc xác minh bundle. Không có test fail để phân tích. Không gọi AI/backend, không push, không đụng dữ liệu vườn.
- Việc cần làm trước lượt tiếp theo: người dùng cho phép USB debugging trên Note9 để ADB hiện `device`; sau đó thực hiện lại B1–B8 theo thứ tự. Danh sách thử tay vẫn ở đoạn ngay phía trên và mục 22C.

### Kiểm chứng Note9 sau khi USB debugging được cấp quyền — 28/10/2026

- B1: đúng file `C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe` báo serial duy nhất được phép `271c3b59c41c7ece` ở trạng thái **`device`**, model `SM_N960F`.
- B2: với `JAVA_HOME=C:\Program Files\Android\Android Studio\jbr` và `GRADLE_USER_HOME=D:\Android\gradle`, chạy `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`: **BUILD SUCCESSFUL** (1m 1s; 88 task, 3 executed, 85 up-to-date). Cộng 25 XML trong `app/build/test-results/testDebugUnitTest`: **97 JVM test, 0 failure, 0 error, 0 skipped**. Log build tại `docs/build-log-note9-2026-10-28.txt` (ignored).
- B3: `install -r` riêng APK debug và `app-debug-androidTest.apk` trên đúng serial; cả hai trả **Success**. Không gỡ app hoặc xóa dữ liệu.
- B4–B6: đã đọc sáu lớp, dùng Room in-memory/tệp cache tạm hoặc dữ liệu Compose giả; không chạy lớp khác. Sau `logcat -c`, chạy một lệnh `am instrument -w -e class` chỉ gồm sáu lớp được phép. Runner trả **OK (30 tests)**: `BackupTransactionTest` 7/7, `AddSheetDraftDiscardTest` 9/9, `EditablePlantProfileTest` 4/4, `PlantScanFormTest` 6/6, `RecognitionFlowTest` 3/3, `ExampleInstrumentedTest` 1/1; **0 thất bại**. Output ở `app/build/instrument-note9-2026-10-28.txt` (ignored). Không chạy CameraToForm, CalendarDiary, GardenNavigation, AccountFlow/AccountBackup hoặc audit AI/backend/notification.
- B7: `am start -W -n com.example.vuonnha/.ui.MainActivity` trả `Status: ok` (cold start). Ảnh `app/build/note9-screen-2026-10-28.png` (ignored) hiển thị màn Vườn Nhà. `logcat -d` sau lượt này không có dòng `FATAL EXCEPTION`, `ANR in com.example.vuonnha` hoặc `Process: com.example.vuonnha` khớp bộ lọc; đây không phải xác minh toàn diện mọi luồng app.
- B8 **bị chặn**: `git bundle create D:\Backup\VuonNha_1-2026-10-28.bundle --all` trả `fatal: Unable to create 'D:/Backup/VuonNha_1-2026-10-28.bundle.lock': Permission denied`. Không tạo/xác minh được bundle trong lượt này; không thử đường dẫn hoặc cách ghi khác. Dừng theo yêu cầu. HEAD trước commit ghi kết quả là `b0990b98d7f29106710409f3e7465252828e586e`; không sửa mã ứng dụng/test, không push, không gọi backend/AI.
- Người dùng tự thử tay khi thuận tiện: xem logo trên màn chủ/ngăn ứng dụng; thêm cây từ danh sách rồi kiểm chiều cao sau khi lưu/mở lại; thêm cây tự nhập, tra cứu trong môi trường do người dùng chuẩn bị rồi đổi loài và kiểm lịch gợi ý/lịch tự sửa; EditSheet đổi tên loài, thử giới hạn 1000 ký tự, xác nhận Xoá/Huỷ việc và xóa cây; thử quyền/giờ/thao tác Xong/Hoãn của thông báo. Nên xuất ZIP trong Cài đặt trước khi thử nếu dữ liệu vườn quan trọng.

## 24. Thiết kế luồng thêm riêng cho Cá cảnh và Thú cưng — 28/10/2026 · Claude (chat)

**Yêu cầu của người dùng:** khi thêm cá hay thú cưng, các bước sau vẫn hỏi "Cho cây sẽ ở góc nào", "Vị trí đặt cây", "Ánh sáng nơi trồng", "Chậu & giá thể" — không liên quan. Thiết kế riêng cho từng nhóm; **Cây cảnh vẫn là luồng chính và phải giữ nguyên hành vi/chữ hiện tại**.

### Hiện trạng (đọc từ mã HEAD `9ba24c6`, số dòng xấp xỉ trong `PlantsScreen.kt`, Codex tự xác minh)

1. Chip "Nhóm" (Cây cảnh / Cá cảnh / Thú cưng) chỉ hiện ở nhánh "Tự nhập tên loài" (~546–555). Nhánh "Chọn từ danh sách" (~527–542) trộn cây, cá, hamster trong một dropdown `SPECIES`, không có nhóm.
2. `Species` (`data/SpeciesDb.kt`) không có trường nhóm. `profileGroup = if (custom) group else SpeciesGroup.PLANT` (~415): chọn "Cá betta" từ danh sách bị coi như PLANT khi lọc profile.
3. Mọi chữ ở bước 1–3 đều nói về cây: "Bạn đang trồng cây gì?" (~521), "Cây sẽ ở góc nào?" (~626), "Vị trí đặt cây" (~640), "Ánh sáng nơi trồng"/"Chậu & giá thể" (~641–646), "Lịch chăm cây" / "Đặt lịch kiểm tra cây; chỉ tưới hoặc bón khi cây cần" (~664–666), "Sẽ lưu hồ sơ cây…" (~708), và hộp xem lại (~750–762).
4. `EditSheet` (~810) xác định nhóm chỉ bằng tiền tố `custom:<GROUP>:`, nên cá/thú **có sẵn** (id `betta`, `vang`, `guppy`, `hamster`) hiện "Thông tin cây" và trường ánh sáng/đất.
5. Chỉ chip gợi ý việc cho cây tự nhập (~667); "Tra cứu" và quét ảnh chỉ dành cho cây (giữ nguyên, không đổi).

### Thiết kế mục tiêu

**Bước 1 — Chọn loại.** Đưa chip Nhóm lên đầu bước 1, dùng chung cho cả hai nhánh, mặc định 🪴 Cây cảnh. Dưới đó là chip "Tự nhập / Chọn từ danh sách". Danh sách chỉ hiện loài của nhóm đang chọn (đổi nhóm thì chọn loài đầu tiên của nhóm đó). "Tra cứu", "Điền gợi ý", "Thông tin bổ sung về cây" chỉ hiện khi nhóm là Cây cảnh (như hiện nay).

**Chữ theo nhóm** (tạo `GroupCopy` thuần, ví dụ `ui/plants/GroupCopy.kt`, một hàm `groupCopy(group: SpeciesGroup)`):

| Mục | Cây cảnh (GIỮ NGUYÊN) | Cá cảnh | Thú cưng |
|---|---|---|---|
| Tên bước 1 | Chọn cây | Chọn cá | Chọn thú cưng |
| Tên bước 2 | Góc trồng | Bể của cá | Nơi ở |
| Mô tả bước 1 | Bắt đầu với tên cây… | Bắt đầu với loài cá bạn nuôi. | Bắt đầu với loài hoặc giống bạn nuôi. |
| Tiêu đề bước 1 | Bạn đang trồng cây gì? | Bạn đang nuôi cá gì? | Bạn đang nuôi thú cưng gì? |
| Nhãn ô tên loài / ví dụ | Tên loài hoặc mô tả · VD: Cây kim tiền… | Tên loài cá · VD: Cá betta, cá bảy màu | Tên loài hoặc giống · VD: Hamster, mèo Anh lông ngắn |
| Tiêu đề bước 2 | Cây sẽ ở góc nào? | Cá sẽ ở bể nào? | Bạn ấy sẽ ở đâu? |
| Mô tả bước 2 | Đặt tên thân quen và chọn một góc cho cây… | Đặt tên cho bể hoặc nhóm cá và ghi thông tin bể. Các mục đều tuỳ chọn. | Đặt tên thân quen và ghi nơi ở. Các mục đều tuỳ chọn. |
| Tiêu đề bước 3 / phụ | Lịch chăm cây / Đặt lịch kiểm tra cây; chỉ tưới… | Lịch chăm cá / Chọn việc cần nhắc: cho ăn, thay nước, vệ sinh lọc. | Lịch chăm thú cưng / Chọn việc cần nhắc: cho ăn, nước uống, dọn chuồng. |
| Dòng tóm tắt lưu | Sẽ lưu hồ sơ cây… | Sẽ lưu hồ sơ cá… | Sẽ lưu hồ sơ thú cưng… |

**Trường ở bước 2** (lưu trong `profileJson` tự do, không đổi schema Room):

- **Cây cảnh:** giữ y hệt hiện tại (`location`, `actualHeight`, `actualLight`, `pottingMix`).
- **Cá cảnh:** `location` nhãn "Vị trí đặt bể" (VD: Kệ TV phòng khách), `tankVolume` "Dung tích bể (lít)" (VD: 20), `fishCount` "Số lượng cá" (VD: 5), `filterType` "Hệ thống lọc (không bắt buộc)" (VD: Lọc thùng, lọc treo). Bỏ ánh sáng/chậu & giá thể/chiều cao.
- **Thú cưng:** `location` nhãn "Nơi ở" (VD: Chuồng ở phòng khách), `petAge` "Tuổi hoặc ngày nhận nuôi" (VD: 8 tháng), `petWeight` "Cân nặng (g/kg)" (VD: 120 g), `housing` "Loại chuồng / chỗ ở (không bắt buộc)". Bỏ ánh sáng/chậu & giá thể/chiều cao.
- Mọi trường tuỳ chọn, giới hạn 1000 ký tự như hiện nay.

**Bước 3 — chip gợi ý việc** (bấm thêm, dùng tên có sẵn để `TaskType.infer` nhận đúng loại): Cây giữ "Kiểm tra độ ẩm đất" 3, "Kiểm tra dinh dưỡng" 30. Cá: "Cho ăn" 1, "Thay nước" 7, "Vệ sinh lọc" 21. Thú: "Cho ăn" 1, "Thay nước uống" 1, "Thay lót chuồng" 7. Chip hiện cho mọi nhánh (không chỉ tự nhập) khi nhóm tương ứng đang chọn; chip không tạo trùng tên đã có.

### Kỹ thuật (đề xuất, Codex quyết chi tiết)

- Thêm `val group: SpeciesGroup = SpeciesGroup.PLANT` cuối `Species`; `SPECIES`: `betta`, `vang`, `guppy` = FISH, `hamster` = PET; `customSpecies` gán `group`. Không đổi id loài, không đổi Room schema, không đổi backup format.
- Hàm thuần `GroupCopy.kt` (chuỗi theo nhóm) và `GroupFields.kt` (danh sách trường mỗi nhóm: key, nhãn, placeholder, có là số không). Giao diện lặp qua danh sách này thay vì `if (group == ...)` rải rác.
- `profileToSave`/`profileIsValidForSave` (`ProfileToSave.kt`): với FISH/PET chỉ giữ các key thuộc nhóm (allowlist), bỏ `REFERENCE_KEYS` và key của nhóm khác (tránh lưu ẩn dữ liệu của nhóm trước khi người dùng đổi nhóm). PLANT giữ nguyên hành vi hiện tại. Đổi `profileGroup` (~415) thành nhóm thật của loài (`sp.group` hoặc `group` khi tự nhập).
- Đổi nhóm: giữ `name` và `location`, dọn `REFERENCE_KEYS` như `clearSpeciesGroupReferenceData` hiện có; key riêng nhóm cũ không còn hiện; xóa key đó khỏi profile khi đổi nhóm (không để lại ẩn). Cần hàm thuần + test.
- `hasUnsavedDraft` (`DraftGuard.kt`): chỉ đếm trường của nhóm đang chọn; cần giữ các test hiện có.
- Hộp xem lại (~750–762): hiển thị trường theo `GroupFields` của nhóm, không hiện "Ánh sáng/Chậu và giá thể" cho cá/thú; không hiện hộp "Lưu thông tin bổ sung về cây" cho cá/thú.
- `EditSheet` và `ItemDetailScreen`: xác định nhóm bằng `findSpecies(item.speciesId)?.group` (không chỉ tiền tố custom); tiêu đề "Thông tin cây" → "Thông tin bể cá"/"Thông tin thú cưng"; dùng cùng `GroupFields` để sửa/hiển thị. Dữ liệu cũ của cá/thú (profile có thể chỉ có `location`) vẫn mở được, trường mới trống.
- Quét ảnh (`PlantScan*`), "Tra cứu" (`PlantLookupRepository`), màn Home ("Thêm cây vào vườn", "Bắt đầu với chậu cây đầu tiên"): **không đổi** trong lô này. Ghi vào "Việc còn mở" nếu thấy chữ Home cần đổi theo nhóm (ví dụ "Thêm vào vườn").
- Kiểm tra `AssistantRepository` (snapshot vườn) có đọc key profile nào không; nếu có dùng key cây cho cá/thú thì ghi lại, không sửa trong lô này.

### Chia commit (mỗi commit xanh Gradle trước khi sang commit kế)

- **G1** mô hình dữ liệu + chuỗi: `Species.group`, `GroupCopy.kt`, `GroupFields.kt`, test JVM: (a) chuỗi nhóm PLANT **bằng đúng chuỗi hiện tại** (golden, chống lệch chữ cây); (b) mỗi nhóm có đủ trường, key không trùng giữa PLANT và REFERENCE_KEYS ngoài `actualHeight`; (c) `betta`/`hamster` ra đúng nhóm. Commit: `feat: thêm nhóm cho loài và bộ chữ/trường theo nhóm`.
- **G2** bước 1: chip Nhóm lên đầu, dùng chung, danh sách lọc theo nhóm, chữ bước 1 theo nhóm. Test hàm thuần lọc danh sách và đổi nhóm. Commit: `feat: chọn nhóm ngay bước 1 và lọc danh sách theo nhóm`.
- **G3** bước 2 + lưu: trường theo nhóm, allowlist trong `profileToSave`, dọn key khi đổi nhóm, hộp xem lại, `DraftGuard`. Test: cá lưu `tankVolume`/`fishCount` và bỏ `actualLight`; thú lưu `petAge`; đổi nhóm xóa key cũ; PLANT không đổi. Commit: `feat: trường riêng cho cá cảnh và thú cưng ở bước 2 và khi lưu`.
- **G4** bước 3 + sửa/xem: chip gợi ý việc theo nhóm, chữ bước 3, `EditSheet`, `ItemDetailScreen`. Commit: `feat: lịch, sửa và xem chi tiết theo nhóm cá/thú cưng`.

### Quy tắc

- Cây cảnh không hỏng: toàn bộ test hiện có (97 JVM + 30 instrumented) phải còn đạt; nếu test androidTest tìm theo chữ của cây thì chữ PLANT phải giữ y.
- Chạy Gradle của Codex được: `.\gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`; cộng XML lấy tổng test. Sau G4, cài `install -r` và chạy 6 lớp instrumented an toàn như mục 22 (không chạy CameraToForm, CalendarDiary, GardenNavigation, Account*, audit AI). Thêm test Compose cho luồng cá (chọn Cá cảnh, tự nhập "Cá koi", kiểm có chữ "Dung tích bể" và không có "Ánh sáng nơi trồng") theo mẫu `PlantScanFormTest` (dataclass giả, không ghi DB thật).
- Quyền như mục 22: chỉ trong `D:\Hoc_Tap\VuonNha_1\VuonNha_1\`; adb bằng đường dẫn `C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe`, serial `271c3b59c41c7ece`; cấm `connectedDebugAndroidTest`, `uninstall`, `pm clear`, `shell input`, `reverse`; không đổi schema/API/dependency; không push; không gọi AI; không tạo bundle (người dùng tự tạo).
- Quy trình mỗi commit: đọc mã → test trước → code → Gradle → ghi mục 25 → `git add` đúng file → commit. Bị chặn hoặc cần quyết định UX thì ghi mục 25 và dừng, đừng tự đoán.

### Thử tay cho người dùng sau G4 (Codex chép vào mục 25)

1. Thêm cây từ danh sách và tự nhập: chữ và trường giống hệt trước (không hồi quy).
2. Chọn Cá cảnh → danh sách chỉ có betta/cá vàng/cá bảy màu; bước 2 hỏi vị trí bể, dung tích, số lượng, lọc; không có ánh sáng/chậu.
3. Chọn Thú cưng → danh sách có hamster; bước 2 hỏi nơi ở, tuổi, cân nặng, chuồng.
4. Lưu rồi mở lại/sửa: các trường còn đúng và đúng nhãn; chip việc gợi ý đúng nhóm.
5. Đổi nhóm giữa chừng sau khi đã nhập: tên/vị trí giữ, trường riêng nhóm cũ mất, lưu không dính dữ liệu nhóm cũ.
6. Mở cây/cá/thú đã có từ trước (bản cũ) vẫn xem và sửa được.

## 25. Phản hồi của Codex (mục 24)

### G1 — nhóm loài và bộ chữ/trường — 28/10/2026

- Đã đọc `SpeciesDb.kt`, `PlantsScreen.kt`, `ProfileToSave.kt`, `DraftGuard.kt`, `ItemDetailScreen.kt` và `AssistantRepository.kt`. Snapshot Trợ lý đọc các key profile của cây (`actualLight`, `pottingMix`...), chưa đổi trong G1.
- Viết `GroupPresentationTest` trước: khóa nguyên văn các chuỗi PLANT ở AddSheet, kiểm bộ key theo nhóm không trùng, kiểm betta/cá vàng/cá bảy màu = FISH, hamster = PET và custom giữ nhóm. Sau đó thêm `Species.group` mặc định PLANT ở cuối constructor; gán nhóm cho loài có sẵn/custom; tạo `GroupCopy.kt`, `GroupFields.kt`. Chưa đổi UI ở G1, nên chữ và hành vi Cây cảnh đang dùng vẫn nguyên.
- Chạy `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain` với JBR và Gradle home được phép: **BUILD SUCCESSFUL**; 25 XML, **100 JVM test, 0 failure, 0 error, 0 skipped**. Kotlin daemon không kết nối được, Gradle tự biên dịch dự phòng; lượt này mất 58m 58s. Log `docs/build-log-g1-2026-10-28.txt` được ignore. `assembleDebugAndroidTest` chỉ biên dịch, chưa chạy instrumented.
- File của G1: `app/src/main/java/com/example/vuonnha/data/SpeciesDb.kt`, `app/src/main/java/com/example/vuonnha/ui/plants/GroupCopy.kt`, `GroupFields.kt`, `app/src/test/java/com/example/vuonnha/ui/plants/GroupPresentationTest.kt`, `docs/HANDOFF.md` (bao gồm mục 24 do người dùng cung cấp, có sẵn chưa commit trước G1). Không đổi schema/API/dependency, không gọi AI, không push. Bước tiếp theo: G2 theo mục 24 sau khi G1 commit.
- Commit G1: `954ca18df66bd99e83a8d8c8639405b00523d63d`.

### G2 — chọn nhóm và lọc danh sách ở bước 1 — 28/10/2026

- Đã đọc state AddSheet, nhánh tự nhập/danh sách và các test draft/đổi nhóm hiện có. Viết `GroupSpeciesSelectionTest` trước: kiểm bốn cây/ba cá/một thú trong danh sách tương ứng; đổi nhóm chọn loài đầu tiên; chọn cùng nhóm giữ loài hiện tại.
- Thêm `GroupSpeciesSelection.kt`; đưa chip Nhóm lên trên hai cách nhập, dùng chung ở bước 1. Dropdown chỉ dùng `speciesForGroup(group)`; khi đổi nhóm hoặc vào nhánh danh sách, `sp` được đưa về loài hợp lệ. Bước 1 lấy tên/mô tả/nhãn ô từ `GroupCopy`; chuỗi PLANT giữ nguyên. Chưa đổi bước 2, profile/lưu hoặc bước 3 trong G2.
- Chạy đủ `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`: **BUILD SUCCESSFUL** (2m 23s); 27 XML, **103 JVM test, 0 failure, 0 error, 0 skipped**. Kotlin daemon lại không kết nối, Gradle biên dịch dự phòng thành công. Log `docs/build-log-g2-2026-10-28.txt` ignored. Chưa chạy instrumented runtime.
- File G2: `PlantsScreen.kt`, `GroupSpeciesSelection.kt`, `GroupSpeciesSelectionTest.kt`, `docs/HANDOFF.md`. Không đổi schema/API/dependency, không gọi AI, không push. Bước tiếp theo: G3 sau commit G2.
- Commit G2: `409c76369d3f5fc3229ee6f779911c26b5f4bcc0`.

### G3 — trường riêng ở bước 2 và dữ liệu lưu — 28/10/2026

- Đọc `ProfileToSave.kt`, `SpeciesChange.kt`, `DraftGuard.kt`, `PlantReview.kt`, AddSheet bước 2/hộp xem lại và test hiện có. Viết test trước cho cá lưu `tankVolume`/`fishCount` và bỏ field cây, thú lưu `petAge`, đổi nhóm dọn key cũ nhưng giữ `location`, PLANT giữ quy tắc cũ. Thêm Compose test `FishAddSheetTest` với dữ liệu giả: cá tự nhập có Dung tích bể, không có Ánh sáng/Chậu. Test này mới **chỉ biên dịch**, chưa chạy trên máy.
- `profileToSave` dùng allowlist theo `GroupFields` cho FISH/PET; PLANT giữ quy tắc `keepExtra` cũ. `profileGroup` lấy `sp.group` cho loài danh sách. Đổi nhóm chỉ giữ `location` trong profile, lọc `editedFields`; tên riêng không đổi. Bước 2 dùng field của nhóm, cá/thú có nhãn, gợi ý và giới hạn 1000 ký tự; PLANT giữ thứ tự/nhãn cũ và phần điều kiện trồng mở rộng. Hộp xem lại cá/thú chỉ hiện field của nhóm và không hiện checkbox thông tin bổ sung về cây. `DraftGuard` chỉ so field của nhóm đang chọn; trường hợp PLANT giữ so sánh cũ.
- Lượt Gradle đầu **fail rõ nguyên nhân**: test mới dự đoán sai PLANT khi `keepExtra=false` (vốn bỏ `actualHeight`), `ProfileValidationTest` cũ còn đòi cá/thú giữ field cây, và hai import Compose test không tồn tại. Đã sửa đúng kỳ vọng theo hợp đồng mới, không xóa test. Lượt chạy lại đủ `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`: **BUILD SUCCESSFUL** (1m 38s); 28 XML, **108 JVM test, 0 failure, 0 error, 0 skipped**. Log `docs/build-log-g3-2026-10-28.txt` và `docs/build-log-g3-retry-2026-10-28.txt` ignored.
- File G3: `DraftGuard.kt`, `GroupFields.kt`, `PlantsScreen.kt`, `ProfileToSave.kt`, `SpeciesChange.kt`, `DraftGuardTest.kt`, `ProfileToSaveTest.kt`, `ProfileValidationTest.kt`, `GroupProfileTest.kt`, `FishAddSheetTest.kt`, `docs/HANDOFF.md`. Không đổi schema/API/dependency, không gọi AI, không push. Bước tiếp theo: G4 sau commit G3.
- Commit G3: `d9ca33279ee89aa9e60baf4ad1fe345e95e62cd3`.

### G4 — lịch, sửa và xem chi tiết theo nhóm — 02/11/2026

- Đã đọc bước 3 của AddSheet, EditSheet, ItemDetailScreen và `TaskType.infer`. Viết `GroupCareSuggestionsTest` trước: tên/chu kỳ chip PLANT đúng hiện trạng, cá/thú đúng thiết kế, `TaskType` suy ra đúng và nhãn chi tiết/trường theo nhóm. Sau đó thêm `GroupCareSuggestions.kt`, chữ bước 3 trong `GroupCopy`, nối bước 3 với copy/chip theo nhóm. Chip PLANT vẫn chỉ ở nhánh tự nhập để giữ hành vi cây hiện tại; cá/thú có chip ở cả nhánh tự nhập và danh sách, bấm chip không thêm tên trùng.
- EditSheet và ItemDetailScreen lấy nhóm từ `findSpecies(item.speciesId)?.group`, gồm cả betta/cá vàng/cá bảy màu/hamster có sẵn. PLANT giữ danh sách trường/chữ cũ. Cá/thú dùng `GroupFields` để sửa/xem, hiện cả khi profile cũ rỗng; khi lưu EditSheet bỏ key ẩn không thuộc nhóm. Custom fish/pet có nhãn tên loài phù hợp. Chữ Home và snapshot Trợ lý vẫn nguyên phạm vi mục 24; snapshot hiện còn chọn các key của cây, cần cân nhắc riêng nếu muốn Trợ lý dùng field bể/thú.
- Lượt Gradle đầu mất phiên theo dõi sau khi người dùng gửi ảnh Android Studio; log dừng giữa lint, không có dòng kết thúc nên không dùng làm kết luận. Chạy lại đủ `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`: **BUILD SUCCESSFUL** (1m 43s); 29 XML, **113 JVM test, 0 failure, 0 error, 0 skipped**. Log `docs/build-log-g4-retry-2026-11-02.txt` ignored. `FishAddSheetTest` và các instrumented test mới chỉ được biên dịch; chưa chạy trên thiết bị trước commit.
- File G4: `GroupCopy.kt`, `GroupCareSuggestions.kt`, `PlantsScreen.kt`, `ItemDetailScreen.kt`, `GroupCareSuggestionsTest.kt`, `docs/HANDOFF.md`. Không đổi schema/API/dependency, không gọi AI, không push. Sau commit G4: cài bằng `install -r`, chạy đúng sáu lớp an toàn ở mục 22 bằng `am instrument` trên serial Note9 được phép.
- Commit G4: `a8634276c1327ef5eef02ef525df8fdd20c72691`.

### Kiểm chứng sau G4 trên Note9 — 02/11/2026

- `adb devices -l` nhận đúng `271c3b59c41c7ece` ở trạng thái `device` (SM_N960F). Dùng đúng file adb được phép; `install -r` APK debug và APK androidTest lần lượt trả `Success`, không gỡ app hoặc xóa dữ liệu.
- Sau `logcat -c`, chạy một lệnh `am instrument -w -e class` chỉ gồm sáu lớp an toàn ở mục 22. Output: `BackupTransactionTest` 7 dấu đạt, `AddSheetDraftDiscardTest` 9, `EditablePlantProfileTest` 4, `PlantScanFormTest` 6, `RecognitionFlowTest` 1; tổng **27 bài hiện dấu đạt**. Trước khi hai bài còn lại của RecognitionFlow và một bài ExampleInstrumentedTest chạy xong, runner trả `INSTRUMENTATION_RESULT: shortMsg=Process crashed.` và `INSTRUMENTATION_CODE: 0`, **không có `OK (30 tests)`**. Vì vậy không kết luận 30/30 đạt; `RecognitionFlowTest` chỉ hoàn tất 1/3, `ExampleInstrumentedTest` chưa chạy. Output lưu ở `app/build/instrument-g4-2026-11-02.txt` (ignored).
- Logcat `TestRunner` ghi `nicknameDoesNotReplaceSpeciesOrScientificName` hoàn tất rồi bắt đầu `repeatedKeyboardInputAndGroupSwitchesKeepFormResponsive`; ngay sau đó `ActivityManager` ghi force-stop app và “Crash of app ... running instrumentation”. Bộ lọc AndroidRuntime không cho stack trace Java tương ứng. Chưa xác định vì sao process bị dừng; không quy kết cho mã app hay môi trường. Theo yêu cầu khi fail không rõ nguyên nhân, **dừng tại đây, không chạy lại**. `FishAddSheetTest` mới chỉ biên dịch, không nằm trong sáu lớp được phép chạy.
- G1–G4 đã commit riêng; lần Gradle cuối trước cài đạt 113 JVM test, 0 failure/error/skip, APK debug/androidTest và lint thành công. Chưa có xác nhận runtime đầy đủ 30 instrumented sau G4. Không chạy CameraToForm/CalendarDiary/GardenNavigation/Account*/audit, không gọi backend/AI, không push, không tạo bundle.
- Người dùng tự thử tay khi thuận tiện: (1) thêm cây từ danh sách và tự nhập, kiểm chữ/trường vẫn như trước; (2) Cá cảnh lọc đúng betta/cá vàng/cá bảy màu và có vị trí bể/dung tích/số lượng/lọc, không có ánh sáng/chậu; (3) Thú cưng chỉ hiện hamster trong danh sách và có nơi ở/tuổi/cân nặng/chuồng; (4) lưu rồi mở lại/sửa cá, thú, kiểm nhãn và chip việc; (5) đổi nhóm sau khi nhập, tên/vị trí còn, field nhóm cũ mất; (6) mở cây/cá/thú cũ và sửa thử. Nếu dữ liệu vườn quan trọng, xuất ZIP trong Cài đặt trước khi thử.

### Thử lại theo yêu cầu người dùng — 02/11/2026

- `git status --short` đầu lượt sạch; G4 đã commit riêng ở `a8634276c1327ef5eef02ef525df8fdd20c72691`, nên không tạo lại commit G4. HEAD đầu lượt là `12f5e66d558e04356365f7bcc60ae6c24ba919c2`.
- ADB bằng đúng đường dẫn cho phép thấy serial `271c3b59c41c7ece` ở trạng thái `device`. Bước `install -r app/build/outputs/apk/debug/app-debug.apk` **không thực hiện được**: `adb.exe: failed to stat ... No such file or directory`. Kiểm tra đường dẫn trong repo: APK debug không tồn tại, APK androidTest còn tồn tại. Chưa rõ vì sao APK debug biến mất sau lượt Gradle trước; không suy đoán.
- Theo quy tắc bị chặn thì dừng: không cài riêng APK test; không chạy sáu lớp instrumented hoặc `FishAddSheetTest` ở lượt này; không dùng `connectedDebugAndroidTest`. Kết quả runtime gần nhất vẫn là lượt 27 dấu đạt rồi process crash đã ghi phía trên, **không có kết quả bảy lớp mới**. Không gọi AI/backend, không push, không tạo bundle, không truy cập dữ liệu vườn.
- Bước tiếp theo khi được tiếp tục: tạo lại APK debug trong đúng repo bằng bộ Gradle cho phép, xác nhận file APK hiện diện, rồi `install -r` cả hai APK và chạy bảy lớp đã được người dùng chỉ định. Không chạy các lớp bị cấm.

### Lượt cài và kiểm chứng được yêu cầu lại — 02/11/2026

- Đọc lại mục 22, 24, 25 và `FishAddSheetTest`. Test cá chỉ dựng `AddSheet` bằng Compose, nhập `Cá koi` và kiểm nhãn; không mở Room, không ghi DB thật. Theo yêu cầu lượt này, **không chạy Gradle**.
- `Get-Item` trên đúng hai đường dẫn người dùng đưa xác nhận `app-debug.apk` (25.769.929 byte) và `app-debug-androidTest.apk` (1.209.670 byte) đều tồn tại. `adb devices -l` xác nhận đúng serial `271c3b59c41c7ece` ở trạng thái `device`.
- Lệnh `adb -s 271c3b59c41c7ece install -r D:\Hoc_Tap\VuonNha_1\VuonNha_1\app\build\outputs\apk\debug\app-debug.apk` trả **exit code 1**: `Performing Streamed Install` rồi `adb.exe: failed to install ...app-debug.apk:` nhưng không kèm lý do cụ thể. Chưa xác định nguyên nhân; lượt cài bị ngắt trước đó cũng không có kết quả cuối để dùng làm bằng chứng.
- Theo yêu cầu bị chặn thì dừng: không cài APK test, không xóa app/dữ liệu, không chạy `logcat -c`, bảy lớp instrumented hoặc `am start` trong lượt này. Không có `OK (n tests)` hay kết quả từng lớp mới; kết quả gần nhất vẫn là lượt 27 dấu đạt rồi runner crash nêu trên. Không push, không gọi AI/backend, không tạo bundle. Bước tiếp theo cần bằng chứng lỗi cài đặt cụ thể hoặc một lượt cài được phép thử lại trước khi kiểm chứng runtime.

### Tiếp tục từ bước instrumented sau khi người dùng xác nhận hai APK đã cài — 02/11/2026

- Người dùng xác nhận cả hai APK đã cài thành công và yêu cầu bỏ bước cài; Codex **không tự cài lại** trong lượt này. Đã đọc `FishAddSheetTest` ở lượt trước: chỉ dùng Compose và dữ liệu giả, không ghi DB thật.
- Trên đúng serial `271c3b59c41c7ece`, chạy `logcat -c`, sau đó một lệnh `am instrument -w -e class` chỉ gồm `BackupTransactionTest`, `AddSheetDraftDiscardTest`, `EditablePlantProfileTest`, `PlantScanFormTest`, `RecognitionFlowTest`, `ExampleInstrumentedTest`, `FishAddSheetTest`. Runner trả **`OK (31 tests)`** trong 52,432 giây: lần lượt **7/7, 9/9, 4/4, 6/6, 3/3, 1/1, 1/1**; 0 failure. Output tại `app/build/instrument-seven-2026-11-02.txt` (ignored). Lượt 27 dấu đạt rồi crash trước đây được thay bằng kết quả đầy đủ này; không chạy lớp khác.
- `am start -W -n com.example.vuonnha/.ui.MainActivity` trả `Status: ok`, `LaunchState: WARM`. Logcat sau lượt test và mở app không có dòng khớp `FATAL EXCEPTION`, `ANR in com.example.vuonnha` hoặc `Process: com.example.vuonnha` trong bộ lọc đã dùng. Đây là kiểm tra khởi động và bảy lớp được chọn, chưa phải mọi luồng tích hợp.
- HEAD trước commit tài liệu lượt này: `5bc9d6b07acf4fdaae6fae7a66e3a36a480048fb`. Không sửa mã ứng dụng/test, không chạy Gradle lại, không push, không gọi AI/backend, không gỡ app/xóa dữ liệu.

## 26. Thiết kế lại luồng "Thêm vào vườn" cho gọn mắt — 02/11/2026 · Claude (chat)

**Bối cảnh:** người dùng thấy màn thêm cây hiện tại "rối mắt": nhiều chữ, nhiều thao tác trên một màn (3 hàng chip, hai đoạn mô tả, nút tra cứu, chữ lỗi đỏ, ô thông tin bổ sung). Đã duyệt bản vẽ gọn (ảnh `simple.png` trong chat). Người dùng đồng ý, **riêng phần gợi ý loài ở bước 1 phải xếp lại cho đẹp** (bản vẽ dùng chip xuống dòng lộn xộn, không đạt) — xem U2. Bước 2 và bước 3 người dùng đã duyệt "ổn".

**Nguyên tắc:** mỗi màn một câu hỏi, một nút chính. Ít chữ: không dòng mô tả dưới tiêu đề (trừ khi cần giải thích lỗi). Thao tác phụ ẩn sau liên kết "＋…". **Không đổi logic/dữ liệu**: đây là đổi giao diện; tất cả tính năng hiện có phải còn (xem "Phải bảo toàn").

Mục 24 (nhóm Cây/Cá/Thú và bộ chữ/trường theo nhóm, `GroupCopy`, `GroupFields`, `GroupCareSuggestions`) vẫn đúng, chỉ thay cách trình bày. Chuỗi PLANT trong `GroupCopy` được phép rút gọn **chỉ khi** cập nhật test golden `GroupPresentationTest` cùng lần và ghi rõ từng chuỗi đổi trong mục 27.

### Quyền (như mục 24, nhắc lại)

Chỉ trong `D:\Hoc_Tap\VuonNha_1\VuonNha_1\`; cấm mọi thư mục khác (đặc biệt `D:\Hoc_Tap\mobile_giao_vien`), `.env`, keystore, `backend\data`, `.audit`. adb bằng `C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe`, serial `271c3b59c41c7ece`: được `devices`, `install -r`, `am start`, `am instrument`, `exec-out screencap -p` (lưu ảnh ngoài repo), `logcat -c/-d`. Cấm `connectedDebugAndroidTest`, `uninstall`, `pm clear`, `shell input`, `shell settings`, `reverse`. Không đổi schema/API/dependency; không push; không gọi AI; không tạo bundle. Nếu `install -r` thất bại (Samsung thường cần bấm cho phép trên máy), ghi lỗi thật vào mục 27 rồi dừng — người dùng sẽ tự cài bằng PowerShell.

### Phải bảo toàn (không được mất tính năng)

Tất cả đang hoạt động và có test; đọc mã để liệt kê đủ, tối thiểu:
- Ba nhóm Cây/Cá/Thú và bộ chữ/trường theo nhóm (`GroupCopy`, `GroupFields`); "Tra cứu", "Điền gợi ý", thông tin bổ sung chỉ cho Cây cảnh.
- `editedFields`, `derivedFor`, `clearSpeciesDerivedData`, `mergeLookupProfile`, `LookupRequestGate` (tranh chấp lookup), `effectivePlantName`/`nameAutofilled`.
- `manualPlans` và `suggestedPlans`, `reviewedTasks`, nguồn lịch, `cyclesForExistingTasks`; ngày bắt đầu nhắc (`firstReminder`, `pickingDate`, `CareDatePicker`).
- `preparePlantReview`/hộp "Xem lại trước khi lưu" và `profileToSave`; `DraftGuard` (hỏi trước khi bỏ); `BackHandler`.
- Luồng nhận diện ảnh: `AddSheet` vẫn nhận `initialCandidate`, có đường "bỏ gợi ý AI" (`usingAi`, `onAiSuggestionDiscarded`), và `PlantScanScreen` gọi `AddSheet(fullScreen = true)`. Chỉ `fullScreen = true` dùng trong production; nhánh `!fullScreen` hiện chỉ test dùng — đừng làm hỏng test, hỏi người dùng nếu muốn bỏ.
- Khu vực trồng (`zones`) ở bước 2 khi có khu vực; lỗi hiển thị (`addError`, `lookupError`, độ dài tối đa 120/1000/30 việc).
- `fullScreen=true` dùng bàn phím: ô nhập không bị che, nút chính luôn thấy.

### Thiết kế mục tiêu (ba màn)

**Chung:** thanh trên: nút quay lại `‹` bên trái + ba chấm tiến độ ở giữa (chấm hiện tại dài hơn, 22dp, rộng; chấm còn lại 8dp). Bỏ thanh tiến độ dài và dòng "Bước 1/3 · …". Tiêu đề lớn 28sp, khoảng cách dọc 20–26dp. Nút chính đáy màn: cao 54dp, bo tròn hết, màu `primary`, chữ `onPrimary` — dùng `MaterialTheme.colorScheme`, không hard-code màu. Vùng chạm ≥ 48dp. Hỗ trợ sáng/tối; chữ 1.3× không cắt chữ/che nút.

**Bước 1 — "Bạn thêm gì vào vườn?"** (trên màn này mục đích là một câu hỏi: loại + tên):
- Ba thẻ nhóm ngang bằng nhau (emoji 30sp + nhãn "Cây"/"Cá"/"Thú cưng"); thẻ chọn: nền `primaryContainer`, viền 2dp `primary`. Chữ của tiêu đề theo nhóm (`GroupCopy.stepOneTitle`) hiện dưới ô nhập nếu cần, nhưng mặc định dùng tiêu đề chung "Bạn thêm gì vào vườn?" (ghi đổi chuỗi trong mục 27).
- Một ô nhập tên (nhãn theo nhóm: `GroupCopy.speciesLabel`/`speciesPlaceholder`), bo 18dp. Icon 🔍 ở cuối ô **chỉ hiện khi nhóm là Cây cảnh** và tên ≥ 2 ký tự; bấm = Tra cứu (cùng cơ chế `LookupRequestGate`). Đang tra cứu: thay icon bằng vòng xoay nhỏ, bấm lại để hủy. Lỗi tra cứu: **một dòng nhỏ** dưới ô (màu `error`, 13sp, có "Thử lại"), không khung lớn.
- **Gợi ý loài có sẵn (phần người dùng chê — xếp lại):** KHÔNG dùng chip xuống dòng. Dùng **danh sách dọc** dưới ô nhập, mỗi hàng cao 56dp, bo 16dp, viền mỏng, khoảng cách 8dp: bên trái ô emoji 40dp nền `primaryContainer`; giữa tên loài (16sp, đậm vừa, 1 dòng); bên phải dấu ✓ khi đang chọn. Tối đa 4 hàng, không cuộn. Nội dung: khi ô trống → các loài có sẵn của nhóm (`speciesForGroup`), khi đã gõ → các loài khớp (không phân biệt hoa thường và dấu tiếng Việt), hàng đầu ưu tiên khớp đầu chuỗi. Không có hàng nào khớp → ẩn danh sách (người dùng cứ gõ tên tự do rồi bấm Tiếp tục). Bấm một hàng = chọn loài đó (trạng thái như chọn từ dropdown cũ: `custom=false`, `sp=s`, `derivedFor`, dọn dữ liệu lần loài trước). Gõ sửa chữ sau khi đã chọn → chuyển sang tự nhập (`custom=true`), bỏ ✓. Loài tự nhập trùng tên loài có sẵn thì vẫn được lưu là tự nhập (không tự ép).
- Bỏ hai chip "Tự nhập tên loài / Chọn từ danh sách" và dropdown cũ (không mất tính năng: chọn loài = bấm hàng gợi ý; tự nhập = gõ tên).
- "Thông tin bổ sung về cây" (Cây cảnh) không còn ở bước 1; chuyển vào "＋ Thêm chi tiết" ở bước 2 (xem dưới), cùng các trường còn lại. "Điền gợi ý theo tên cây" đi theo.
- Khi vào từ nhận diện ảnh: giữ banner gợi ý AI hiện có ở đầu bước 1 (nội dung/nút "bỏ gợi ý AI" như cũ, chỉ gọn bố cục), tên loài đã điền sẵn.
- Nút "Tiếp tục": tắt (nhạt) khi tên trống hoặc quá 120 ký tự hoặc đang tra cứu (như `canContinue` hiện có).

**Bước 2 — "Cây sẽ ở đâu?"** (theo nhóm: `GroupCopy.stepTwoTitle`; không dòng mô tả):
- Hai ô: "Tên riêng (tuỳ chọn)" và "Vị trí" (nhãn/ví dụ theo nhóm từ `GroupFields`, chỉ lấy trường `location`). Placeholder ngắn để không xuống dòng.
- Liên kết "＋ Thêm chi tiết" mở phần còn lại (trường của nhóm trong `GroupFields`, gồm `actualHeight`/ánh sáng/giá thể với Cây, dung tích/số lượng/lọc với Cá, tuổi/cân nặng/chuồng với Thú, cộng "Điền gợi ý" và thông tin tham khảo của Cây). Mở ra thì cùng màn, không sang màn khác.
- Nếu có khu vực (`zones`): một hàng "Khu vực" nhỏ gọn (menu chọn) dưới ô Vị trí; không có khu vực thì ẩn.

**Bước 3 — "Nhắc bạn chăm cây"** (theo nhóm: `GroupCopy.stepThreeTitle`):
- Mỗi việc một hàng cao 72dp, bo 18dp: emoji 26sp bên trái; giữa tên việc (16sp đậm) + "Mỗi N ngày" (13sp, `onSurfaceVariant`); phải là công tắc (Material `Switch`). Công tắc BẬT = việc nằm trong lịch sẽ lưu (`plans`); TẮT = loại khỏi lịch nhưng giữ lại để bật lại (không mất chu kỳ đã chỉnh). Danh sách hàng = việc gợi ý của loài + việc người dùng thêm.
- Bấm vào hàng (vùng giữa) mở hộp thoại nhỏ chỉnh chu kỳ bằng nút −/+ (`CycleStepper` hiện có) và — nếu lịch đang dùng ngày bắt đầu — chọn ngày bắt đầu nhắc (đưa `firstReminder`/`pickingDate`/`CareDatePicker` vào đây); với việc tự thêm còn sửa tên việc và nút xóa. Bỏ chip "Tính từ lần đã chăm / Chọn ngày nhắc đầu" khỏi màn chính, đưa vào hộp thoại này.
- Liên kết "＋ Thêm việc khác" mở menu gồm các việc gợi ý của nhóm (`GroupCareSuggestions`, không tạo trùng tên) và "Việc tự đặt…" (hộp thoại tên + chu kỳ).
- Nút chính "Thêm vào vườn" ở đáy; vẫn mở hộp thoại "Xem lại trước khi lưu" (giữ nguyên nội dung/logic). Khi tất cả công tắc TẮT, vẫn lưu được cây mà chưa đặt lịch.
- Lỗi (`addError`), giới hạn 30 việc/120 ký tự: hiện một dòng nhỏ gần nút chính.

### Chia commit (mỗi commit xanh Gradle, test đọc mã trước)

- **U1 — thành phần và hàm thuần.** Tạo composable dùng chung (chấm tiến độ, thẻ nhóm, hàng gợi ý loài, hàng việc có công tắc, nút chính đáy màn) ở `ui/common/` hoặc `ui/plants/`; hàm thuần `speciesSuggestions(group, query, limit)` (lọc, khớp đầu chuỗi lên trước, bỏ dấu tiếng Việt qua `java.text.Normalizer`, không phân biệt hoa thường) và `resolveSpeciesInput(...)` (gõ/bấm hàng → trạng thái `custom`/`sp`). Test JVM cho hai hàm: rỗng → loài của nhóm; "kim" khớp "Kim tiền" (nếu có trong dữ liệu) không nhầm nhóm khác; gõ "trầu ba" khớp "Trầu bà lá xẻ" (bỏ dấu); không khớp → rỗng; tối đa limit. Chưa đổi màn cũ. Commit: `feat: thành phần giao diện gọn và hàm gợi ý loài`.
- **U2 — bước 1.** Thay bước 1 theo thiết kế (thẻ nhóm, ô tên, 🔍, danh sách gợi ý dọc, lỗi một dòng); bỏ dropdown và hai chip cũ; chuyển "Thông tin bổ sung"/"Điền gợi ý" sang bước 2 (khi U3). Nếu U3 chưa làm, giữ tạm hai thứ đó dưới ô nhập trong U2 để không mất tính năng rồi chuyển ở U3. Cập nhật test androidTest phụ thuộc chữ/chip cũ (`rg "Tự nhập tên loài|Chọn từ danh sách|Cây cảnh|Cá cảnh|Thú cưng|Tra cứu"` trong `app\src\androidTest` và `app\src\test`), lập bảng ánh xạ "selector cũ → mới" vào mục 27 và giữ ý định của từng test (không xoá test để cho xanh). Commit: `feat: bước 1 gọn - thẻ nhóm, ô tên và gợi ý dọc`.
- **U3 — bước 2.** Hai ô + "＋ Thêm chi tiết" + khu vực (nếu có). Commit: `feat: bước 2 gọn - hai ô và thêm chi tiết`.
- **U4 — bước 3.** Hàng việc có công tắc, hộp thoại chỉnh chu kỳ/ngày, "＋ Thêm việc khác". Test JVM cho hàm thuần chuyển công tắc ↔ `plans` (TẮT giữ chu kỳ đã chỉnh; BẬT lại khôi phục). Commit: `feat: bước 3 gọn - hàng việc có công tắc`.
- **U5 — thanh trên, hoàn thiện và kiểm tra.** Nút quay lại + chấm tiến độ, bỏ thanh/dòng cũ; khoảng cách; khả năng tiếp cận (contentDescription cho thẻ nhóm/hàng gợi ý/công tắc, trạng thái selected); test Compose cỡ chữ 1.3× (`LocalDensity` fontScale) không che nút chính. Commit: `feat: thanh trên gọn và hoàn thiện giao diện thêm vào vườn`.

### Kiểm chứng sau U5

`.\gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`, cộng XML lấy tổng test (hiện 113 JVM, 31 instrumented). Đọc thư mục `app\build\outputs\apk\` để lấy đúng đường dẫn APK (`debug\app-debug.apk` và `androidTest\debug\app-debug-androidTest.apk`), `install -r`, chạy bảy lớp an toàn như mục 22/25 bằng `am instrument` (cấm `connectedDebugAndroidTest`). `screencap` ba bước lưu ngoài repo nếu máy mở được để người dùng so với bản vẽ.

### Thử tay cho người dùng sau U5 (Codex chép vào mục 27)

1. Bước 1: chọn Cây/Cá/Thú; danh sách gợi ý dọc gọn, không xuống dòng lộn xộn; gõ tên thì lọc; bấm một hàng thì có ✓.
2. Gõ tên không có trong danh sách (ví dụ "cây phượng") vẫn bấm Tiếp tục được; 🔍 tra cứu (cần backend) chỉ có ở nhóm Cây.
3. Bước 2 có hai ô, mở "Thêm chi tiết" thấy chiều cao/ánh sáng/chậu (Cây) hoặc dung tích/số lượng (Cá) hoặc tuổi/cân nặng (Thú).
4. Bước 3 có công tắc; bấm hàng để đổi chu kỳ; tắt hết vẫn lưu được.
5. "Xem lại trước khi lưu" và dữ liệu lưu (mở lại cây, cá, thú) đúng như trước.
6. Quét ảnh cây vào form: vẫn có gợi ý AI và bỏ được.
7. Back khi đã gõ tên: hỏi "Bỏ bản nhập?"; chưa gõ gì: đóng ngay.

## 27. Phản hồi của Codex (mục 26)

### U1 — thành phần gọn và hàm gợi ý loài — 02/11/2026

- Đọc `SpeciesDb.kt`, `GroupSpeciesSelection.kt`, AddSheet và test hiện có. Viết `SpeciesSuggestionsTest` trước: nhóm/rỗng/giới hạn, tìm bỏ dấu tiếng Việt, không khớp, gõ tên trùng loài có sẵn vẫn là custom và bấm hàng chuyển sang loài có sẵn.
- Tạo `SpeciesSuggestions.kt` và `CompactAddComponents.kt` gồm chấm tiến độ, thẻ nhóm, hàng gợi ý loài, hàng việc có công tắc và nút chính. U1 chưa nối các thành phần vào AddSheet, nên UI và dữ liệu hiện hành chưa đổi.
- Lượt Gradle đầu fail biên dịch tại tham chiếu `speciesForGroup` và `taskEmoji` trong file mới; đã dùng trực tiếp `SPECIES.filter` và `TaskType.infer` tương đương. Chạy lại đủ `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`: **BUILD SUCCESSFUL**, 117 JVM test, 0 failure/error. Log `docs/build-log-u1-2026-11-02.txt` và `docs/build-log-u1-retry-2026-11-02.txt` ignored. Chưa chạy instrumented runtime ở U1.
- File U1: `SpeciesSuggestions.kt`, `CompactAddComponents.kt`, `SpeciesSuggestionsTest.kt`, `docs/HANDOFF.md` (gồm mục 26 đã có trước lượt này). Bước tiếp theo: U2 và bảng ánh xạ selector test cũ → mới.
- Commit U1: `d796ad2b446088eec8d6ccb9730f6e84b51dd3be`.

### U2 — bước 1 với thẻ nhóm và danh sách gợi ý dọc — 02/11/2026

- Đọc AddSheet và rà selector trong `app/src/androidTest`/`app/src/test`. Viết `CompactSpeciesSelectionTest` trước: chọn thẻ Cá, bấm hàng Cá betta và thấy dấu ✓. UI bước 1 dùng thẻ nhóm ngang, một ô tên, icon tra cứu cho cây tự nhập và danh sách gợi ý dọc tối đa bốn hàng. Gõ sau khi chọn chuyển về tự nhập; bấm hàng chọn loài có sẵn và giữ đường dọn profile/tên cũ. Banner AI, kết quả lookup, thông tin bổ sung và điền gợi ý vẫn ở bước 1 tạm thời, sẽ chuyển trong U3.
- Bảng ánh xạ selector (giữ ý định test):

  | Cũ | Mới | Test |
  |---|---|---|
  | `🐟 Cá cảnh`, `🪴 Cây cảnh` chip | `Cá`, `Cây` trên thẻ nhóm | `FishAddSheetTest`, `RecognitionFlowTest` |
  | `Tra cứu thông tin cây` dạng chữ nút | content description `Tra cứu thông tin cây` của icon | `GardenFlowAuditTest` (audit chỉ cập nhật selector, không chạy) |
  | `Tên loài hoặc mô tả *`, `Tên loài cá *` | giữ nguyên nhãn ô | `AddSheetDraftDiscardTest`, `PlantScanFormTest`, `EditablePlantProfileTest`, `FishAddSheetTest` |
  | `Thông tin bổ sung về cây` | giữ tạm ở bước 1 U2 | `EditablePlantProfileTest`, `RecognitionFlowTest` |

- Chạy đủ `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`: **BUILD SUCCESSFUL** (1m 39s), 117 JVM test, 0 failure/error. Log `docs/build-log-u2-2026-11-02.txt` ignored. Compose test mới chỉ biên dịch, chưa chạy trên Note9.
- File U2: `PlantsScreen.kt`, `CompactSpeciesSelectionTest.kt`, `FishAddSheetTest.kt`, `RecognitionFlowTest.kt`, `GardenFlowAuditTest.kt`, `docs/HANDOFF.md`. Bước tiếp theo: U3.
- Commit U2: `0a4368e28147c85d7d581bc04b754b175664aed8`.

### U3 — bước 2 với hai ô và phần chi tiết — 02/11/2026

- Đọc AddSheet bước 1–2, `GroupFields` và selector test. Viết `CompactStepTwoFieldsTest` trước để khóa `location` là ô chính và các field còn lại nằm trong phần chi tiết cho từng nhóm. Sau đó bước 2 hiển thị ô Tên riêng và Vị trí, liên kết “＋ Thêm chi tiết” mở các field khác theo nhóm; với Cây tự nhập, phần này chứa thông tin tham khảo, checkbox lưu kèm và “Điền gợi ý theo tên cây”. Khu vực vẫn hiện khi có zone. Nhánh `!fullScreen` vẫn hiển thị chi tiết như trước.
- Chuỗi PLANT đổi có chủ đích: tiêu đề bước 2 `Cây sẽ ở góc nào?` → `Cây sẽ ở đâu?`; nhãn `Tên gọi riêng (không bắt buộc)` → `Tên riêng (tuỳ chọn)`; placeholder vị trí `VD: Ban công, cạnh cửa sổ hướng đông` → `VD: Ban công`. `GroupPresentationTest` được cập nhật cho tiêu đề. Selector test cũ `Tên gọi riêng (không bắt buộc)` → `Tên riêng (tuỳ chọn)` trong `RecognitionFlowTest`, `EditablePlantProfileTest`, `CameraToFormTest`; ý định nhập/xóa tên riêng giữ nguyên.
- Chạy đủ `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`: **BUILD SUCCESSFUL** (1m 36s), 118 JVM test, 0 failure/error. Log `docs/build-log-u3-2026-11-02.txt` ignored. Instrumented mới chỉ biên dịch ở U3.
- File U3: `GroupFields.kt`, `GroupCopy.kt`, `PlantsScreen.kt`, `GroupPresentationTest.kt`, `CompactStepTwoFieldsTest.kt`, `RecognitionFlowTest.kt`, `EditablePlantProfileTest.kt`, `CameraToFormTest.kt`, `docs/HANDOFF.md`. Bước tiếp theo: U4.
- Commit U3: `be87939347b09bb7e595fa2952029c4379b9e130`.

### U4 — hàng việc có công tắc và hộp thoại chỉnh lịch — 02/11/2026

- Đọc AddSheet bước 3, `GroupCareSuggestions` và `PlantScanFormTest`. Viết `CarePlanToggleTest` trước: tắt rồi bật lại giữ chu kỳ 12 ngày đã chỉnh; bật gợi ý hai lần không nhân đôi. Thêm hàm thuần `toggleCarePlan`, lưu các việc tắt riêng cho lịch tự nhập và lịch loài có sẵn. Hàng việc có công tắc; chạm tên mở hộp thoại sửa tên (lịch tự nhập), chu kỳ, kiểu/ngày bắt đầu và xóa. “＋ Thêm việc khác” mở menu gợi ý nhóm và “Việc tự đặt…”. Tất cả công tắc tắt vẫn lưu được mà không tạo lịch.
- Ánh xạ selector giữ ý định test: `＋ Thêm việc chăm sóc` → `＋ Thêm việc khác` rồi `Việc tự đặt…`/`Thêm việc`; giải thích tên việc rỗng ở ô nhập → dòng lỗi gần nút lưu; `Bỏ việc` ở thẻ → `Bỏ việc` trong hộp thoại sau khi chạm hàng `(Chưa đặt tên)` (`PlantScanFormTest`).
- Chạy `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`: **BUILD SUCCESSFUL** (1m 36s), 120 JVM test, 0 failure/error. Log ignored `docs/build-log-u4-2026-11-02.txt`. Instrumented chỉ biên dịch; chờ chạy trên Note9 sau U5.
- File U4: `CarePlanToggle.kt`, `CarePlanToggleTest.kt`, `PlantsScreen.kt`, `PlantScanFormTest.kt`, `docs/HANDOFF.md`. Bước tiếp theo: U5.

### U5 — thanh trên, cỡ chữ và hoàn thiện — 02/11/2026

- Đọc thanh trên/đáy AddSheet và các test dùng selector cũ. Viết `CompactAddFontScaleTest` trước: ở cỡ chữ 1,3×, nút chính “Tiếp tục” vẫn hiển thị. Sau đó thay header và thanh tiến độ dài bằng nút quay lại 48dp cùng ba chấm có mô tả bước; bỏ dòng giải thích dài trên từng bước; tiêu đề bước 2/3 dùng cỡ headline; nút chính 54dp cố định ở đáy. Thẻ nhóm và hàng loài báo trạng thái selected, hàng việc có vùng chạm sửa tối thiểu 48dp. Sửa chuyển công tắc để việc trùng tên không bị xóa cùng lúc.
- Ánh xạ selector giữ ý định: `Bước 2/3 · Góc trồng` → tiêu đề `Cây sẽ ở đâu?`; `Bước 1/3 · Chọn cây` và `Bạn đang trồng cây gì?` → `Bạn thêm gì vào vườn?`; chữ nút `‹ …`/nút đáy `Quay lại` của AddSheet → content description `Quay lại` ở thanh trên (`AddSheetDraftDiscardTest`, `CameraToFormTest`, `RecognitionFlowTest`, `PlantScanFormTest`). `Thông tin bổ sung về cây` ở bước 1 → bước 2 sau “Tiếp tục” (`EditablePlantProfileTest`, `RecognitionFlowTest`); trường bể Cá → mở “＋ Thêm chi tiết” (`FishAddSheetTest`). `GardenNavigationTest` dùng chữ quay lại của màn Tài khoản, không đổi.
- Lượt Gradle đầu lỗi biên dịch do import thừa `onNode` trong test mới; đã xóa và chạy lại đủ `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --offline --console=plain`: **BUILD SUCCESSFUL** (1m 31s), 120 JVM test, 0 failure/error. Log ignored `docs/build-log-u5-2026-11-02.txt` và `docs/build-log-u5-retry-2026-11-02.txt`. Test Compose mới chỉ biên dịch; chờ chạy trên Note9.
- File U5: `CompactAddFontScaleTest.kt`, `CompactAddComponents.kt`, `CarePlanToggle.kt`, `PlantsScreen.kt`, `AddSheetDraftDiscardTest.kt`, `CameraToFormTest.kt`, `EditablePlantProfileTest.kt`, `FishAddSheetTest.kt`, `PlantScanFormTest.kt`, `RecognitionFlowTest.kt`, `docs/HANDOFF.md`. Bước tiếp theo: cài hai APK và chạy bảy lớp instrumented an toàn.
- Commit U4: `881fdc031307368d5819d40d4d934505b29ca703`; commit U5: `cc7abd8cced803d65fd501d089633b8f6197145e`.

### Kiểm chứng Note9 và sửa selector sau U5 — 02/11/2026

- Hai APK ở `app/build/outputs/apk/debug/app-debug.apk` và `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk` đều `install -r`: **Success**, serial `271c3b59c41c7ece`. Chạy `logcat -c`, rồi instrument từng lớp; `BackupTransactionTest` **OK (7 tests)**, `AddSheetDraftDiscardTest` **OK (9 tests)**. `EditablePlantProfileTest` lượt đầu **FAILURES (4 tests, 1 failure)** ở `reviewCanBeCancelledAndOptionalReferenceExcludedWithoutLosingLocation`, dòng 30: `onNode(isToggleable())` khớp ba node (hai Switch mới và checkbox xem lại). Không thấy crash app trong logcat AndroidRuntime/ActivityManager đã lọc. Đã dừng chuỗi tại lỗi này.
- Nguyên nhân selector rõ từ dump semantics. Gắn content description `Lưu thông tin bổ sung về cây` cho checkbox xem lại và đổi test từ `isToggleable()` sang đúng mô tả; vẫn kiểm việc bỏ thông tin tham khảo trước khi lưu. Lượt build sửa đầu lỗi biên dịch do API `hasRole` không có trong bản Compose hiện tại; đã dùng content description. Chạy lại đủ Gradle: **BUILD SUCCESSFUL** (1m 22s), 120 JVM test, 0 failure/error, APK và lint đạt. Log ignored `docs/build-log-u5-selector-fix-2026-11-02.txt`, `docs/build-log-u5-selector-retry-2026-11-02.txt`. Bước tiếp theo: cài hai APK mới và chạy lại bảy lớp.
- Commit sửa selector: `5a7afbb7f41c216abfc21b2088ee72818e276c4f`. Hai APK mới đều `install -r`: **Success**. Chạy lại `logcat -c` và từng lớp: `BackupTransactionTest` **OK (7 tests)**; `AddSheetDraftDiscardTest` **OK (9 tests)**; `EditablePlantProfileTest` **OK (4 tests)**; `PlantScanFormTest` **OK (6 tests)**; `RecognitionFlowTest` **FAILURES (3 tests, 1 failure)**. Lỗi tại `nextReplacesResultAndBackRestoresItWithoutSaving`, dòng 36: sau hai thao tác quay lại, `assertExists` không tìm thấy chữ `Kết quả nhận diện`. Logcat lọc ứng dụng không cho thấy `FATAL EXCEPTION`/ANR; dấu vết hiện tại chưa xác định vì sao màn kết quả không xuất hiện. Đã dừng, không đoán nguyên nhân và không chạy lại. `ExampleInstrumentedTest`, `FishAddSheetTest` **chưa chạy**. Tổng lớp đã chạy lượt hai: 26 test, 25 đạt, 1 thất bại; **không có tổng OK cho cả bảy lớp**. Chưa chụp ba bước vì chuỗi kiểm chứng chưa đạt. Bước tiếp theo: xác định trạng thái UI sau quay lại trong test lỗi, sửa đúng nguyên nhân, build/cài lại và chạy đủ bảy lớp.
- Danh sách thử tay khi kiểm chứng xong: (1) chọn Cây/Cá/Thú, lọc gợi ý dọc và dấu ✓; (2) tên tự nhập và tra cứu chỉ cho Cây; (3) hai ô bước 2 và chi tiết theo từng nhóm; (4) công tắc việc, chu kỳ, tắt hết vẫn lưu; (5) xem lại và mở lại dữ liệu Cây/Cá/Thú; (6) gợi ý ảnh AI và bỏ gợi ý; (7) Back khi form bẩn hỏi bỏ, form trống đóng ngay.

### Tiếp tục kiểm tra lỗi quay lại — 07/11/2026

- Người dùng yêu cầu sửa hết và đã cắm Note9. Đọc lại `RecognitionFlowTest`, `AddSheet`, `DraftGuard`, `PlantRecognitionResult`, `PlantScanScreen` và Git; HEAD trước lượt này `b739e02`, working tree sạch. Thêm tạm các assertion sau từng lần quay lại để chẩn đoán trạng thái UI, rồi build `assembleDebugAndroidTest --offline --console=plain`: **BUILD SUCCESSFUL** (3m 49s, Kotlin daemon bị từ chối tạo file tạm ngoài phạm vi; Gradle dùng fallback). Sau khi thêm assertion chi tiết, một lệnh thử với tham số `-P` không được PowerShell chuyển đúng nên **BUILD FAILED**; lệnh sửa dùng tham số được quote và biên dịch Kotlin trong tiến trình: **BUILD SUCCESSFUL** (23s). Các assertion chẩn đoán đã được bỏ khỏi mã sau khi không thể chạy trên máy. Không thay đổi logic ứng dụng.
- `adb.exe devices` và `devices -l` (đúng file adb được phép) đều trả `List of devices attached` **trống** nhiều lần. Người dùng đã mở khóa, chấp nhận gỡ lỗi USB; ảnh PowerShell của người dùng cũng cho thấy chính lệnh adb đó trả danh sách trống. Vì serial `271c3b59c41c7ece` chưa xuất hiện, **không cài APK, không chạy instrumented và chưa xác định nguyên nhân** của lỗi `RecognitionFlowTest` từ 02/11. Dừng tại điểm chặn; không suy đoán lỗi app. Bước tiếp theo sau khi Windows/ADB nhận Note9: chạy test chẩn đoán trạng thái sau hai lần quay lại, sửa theo kết quả, chạy Gradle đầy đủ, cài đè và chạy bảy lớp an toàn.

### Chỉnh sửa báo cáo môn học — 10/11/2026

- Đối chiếu C:\Users\DELL\Downloads\Bao_Cao_Cay_Trong.docx với mã nguồn Vườn Nhà; thay nội dung cũ về quản lý chi tiêu bằng báo cáo thống nhất về ứng dụng chăm sóc cây (4 chương, kết luận và tài liệu tham khảo). Giữ bản trước chỉnh sửa tại C:\Users\DELL\Downloads\Bao_Cao_Cay_Trong_truoc_chinh_sua.docx.
- Không chạy lệnh build/test vì chỉ biên tập tài liệu. Kiểm tra mở lại Word: 121 đoạn, 0 bảng cũ; không còn các nội dung tài chính/React Native. Bước tiếp theo: nhóm rà soát thông tin trang bìa và đối chiếu ảnh minh họa nếu cần bổ sung ảnh chụp ứng dụng thật.


### Cập nhật lại báo cáo theo mẫu gốc — 10/11/2026

- Sau phản hồi về bố cục, dựng lại C:\Users\DELL\Downloads\Bao_Cao_Cay_Trong.docx từ bản gốc dự phòng thay vì dùng bản tái định dạng trước đó. Giữ trang bìa/kiểu tiêu đề, cập nhật mục lục Word và nội dung bốn chương; thêm bốn sơ đồ có chú thích (use case, kiến trúc, Room, luồng nhắc việc).
- Rà soát bằng Word: 14 trang, mục lục tự động 39 dòng, 4 sơ đồ ảnh, không còn nội dung quản lý chi tiêu/React Native. Giữ nguyên bản gốc dự phòng. Không chạy build/test vì chỉ biên tập báo cáo.


### Mở rộng báo cáo theo góp ý — 10/11/2026

- Bổ sung nội dung đặc tả các luồng, kiến trúc, Room v5, lịch/notification, API, backup, bảo mật, phạm vi test; thêm bảng dữ liệu/luồng và tình huống lỗi. Bản Word mở rộng có 27 trang, 10 bảng, 4 sơ đồ; mục lục tự động 39 dòng. Không chạy build/test vì chỉ biên tập báo cáo.
- File chính đang bị một tiến trình khác giữ mở nên không ghi đè được. Bản mở rộng lưu riêng tại C:\Users\DELL\Downloads\Bao_Cao_Cay_Trong_ban_mo_rong.docx; file chính và bản gốc dự phòng giữ nguyên. Bước tiếp theo: sau khi đóng file chính trong Word, có thể thay bằng bản mở rộng nếu người dùng muốn.


### Hoàn thiện bố cục báo cáo Vườn Nhà — 10/11/2026

- Tạo bản trình bày riêng từ bản mở rộng để giữ nguyên bản gốc và bản mở rộng. Duy trì nội dung bốn chương, bảng biểu và sơ đồ đã có; đặt lại giãn dòng/khoảng cách đoạn, khôi phục cấp tiêu đề mục con và cập nhật mục lục.
- Chèn thêm hai sơ đồ quy trình “Luồng thêm và lưu hồ sơ cây” và “Luồng sao lưu và xử lý revision”; không tạo ảnh chụp ứng dụng giả. Bản đầu ra: `C:\Users\DELL\Downloads\Bao_Cao_Cay_Trong_ban_trinh_bay.docx`.
- Mở lại và lưu bằng Word; kiểm tra XML của DOCX xác nhận hai chú thích sơ đồ mới, nội dung không còn cụm quản lý chi tiêu/React Native và các hình nhúng được giữ. Không chạy build/test vì chỉ chỉnh tài liệu. Số trang cuối chưa xác minh được tự động do Word COM ngắt kết nối khi lấy số trang; đề nghị mở bản đầu ra để kiểm tra trực quan trang mục lục và vị trí hình.

## Cập nhật 12/11/2026 — chuẩn bị đồng bộ repo GitHub

- Đã fetch `origin/main`; remote có `c44d979` và `1a6a558`. Local `ea4f240` cùng các commit cha của nó vẫn được giữ. Hai lịch sử không có merge base chung.
- Cây tích hợp giữ nội dung project mới nhất từ thư mục local `VuonNha_1/` và đặt lại ở gốc theo cấu trúc GitHub. Các file chỉ có trên GitHub được giữ; `.gitignore` bao gồm quy tắc project và log ngoài project. Checkout lồng trên đĩa được giữ lại, còn metadata Git lồng và file bị ignore không được đưa vào cây publish.
- Thay đổi staged: di chuyển các file project sang gốc, cập nhật `.gitignore`, `AGENTS.md`, README và mã/test local; xóa các file IDE local khỏi cây publish.
- Lệnh đã chạy: `git fetch origin` thành công; kiểm tra status, lịch sử, đường dẫn hai cây; `git diff --cached --check` không báo lỗi. Chưa chạy test/build vì lượt này chỉ xử lý tích hợp Git.
- Tích hợp đang ở nhánh `codex/integrate-origin-main`; chưa tạo merge commit hoặc push. Bước tiếp theo: hoàn tất kiểm tra cây staged, tạo merge commit và push fast-forward lên `origin/main`.
