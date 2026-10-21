# AGENTS.md — Quy tắc chung cho Codex và Antigravity

Tài liệu này áp dụng cho mọi AI agent làm việc trên project VuonNha_1.
Cập nhật lần đầu: 21/10/2026 · Antigravity

---

## 1. Nguyên tắc bất biến

| Quy tắc | Chi tiết |
|---|---|
| **Không thay đổi kiến trúc** | Giữ nguyên phân lớp: Android Compose / Room / Domain / Node.js backend. Mọi tính năng mới phải đúng lớp. |
| **Không đổi database schema** | Room hiện ở v4 (`AppDb.kt`). Thêm migration là thay đổi schema — chỉ Codex được phép khi có kế hoạch rõ ràng. |
| **Không đổi API contract** | Endpoint backend (`server.mjs`) và kiểu dữ liệu giữa Android ↔ backend không được đổi unilaterally. |
| **Không đổi dependencies** | `gradle/libs.versions.toml` và `backend/package.json` chỉ cập nhật khi cần thiết, không nâng version tự ý. |
| **Không đọc/hiển thị bí mật** | `.env`, API key, token, mật khẩu, session token — không đọc, không log, không commit. |
| **Không xóa dữ liệu** | Không `git clean`, `git reset --hard`, không xóa `backend/data/`, ảnh nhật ký hay AVD. |

---

## 2. Quy tắc Git

```
Commit nhỏ, mục đích rõ:
  fix: sửa validation valid khi keepExtra=false (PlantsScreen)
  feat: thêm error state cho removeItem/done/snooze (PlantsViewModel)
  test: bổ sung unit test lỗi ViewModel
  docs: cập nhật HANDOFF.md sau khi sửa P1
```

- **Không commit:** `.env`, `backend/data/`, `local.properties`, `app/build/`, `*.log`, `.idea/`, `.kotlin/`, `node_modules/`
- **Phải commit:** Mọi file `.kt`, `.mjs`, `.md`, `build.gradle.kts`, `libs.versions.toml`, `AndroidManifest.xml`, drawables, XML resources
- Trước mỗi commit: chạy `git diff --stat HEAD` để xem thay đổi thực tế
- Không force-push. Không rebase history đã chia sẻ.

---

## 3. Phân vùng trách nhiệm

### Antigravity làm được độc lập (không cần Codex review)
- Sửa lỗi P1 trong `PlantsViewModel.kt` (error handling không thay đổi signature)
- Sửa lỗi P1 trong `PlantsScreen.kt` (logic `valid` — không thay đổi UI flow)
- Viết thêm JVM unit test và Android instrumented test
- Cập nhật tài liệu `docs/HANDOFF.md`, `docs/ARCHITECTURE.md`
- Tạo/cập nhật `AGENTS.md`
- Sửa bug UI nhỏ không liên quan đến luồng chính

### Codex phụ trách — Antigravity không tự ý sửa
- Sửa lỗi P0: logic reset profile/gợi ý khi đổi loài (`PlantsScreen.kt` AddSheet)
- Sửa lỗi P1: thêm UI tra cứu cho nhánh danh sách (`!custom` branch)
- Mọi thay đổi Room schema hoặc migration
- Thay đổi backend API endpoint hoặc authentication logic
- Thay đổi luồng account/backup
- Thêm dependency mới

---

## 4. Quy tắc test

- **Không xóa test hiện có.** Chỉ thêm, không sửa test đã đạt trừ khi có lý do rõ ràng.
- Mọi lần sửa code phải kèm test xác minh.
- Test account (`AccountFlowTest`, `AccountBackupTest`) cần server riêng cổng 8788 — không chạy lẫn vào môi trường production.
- `LiveRecognitionAuditTest`, `NotificationDeliveryAuditTest`, `GardenFlowAuditTest` là audit test — không chạy trong CI tự động, chỉ chạy khi kiểm tra tích hợp.

---

## 5. Quy tắc backend

- Không lưu API key trong code, APK hay tài liệu.
- `SCAN_TOKEN` trong debug build (`vuonnha-local-2026`) là token phát triển, không phải Gemini key.
- Backend chỉ lắng nghe `127.0.0.1` (localhost). Không expose ra ngoài khi phát triển cục bộ.
- Khi sửa backend: chạy `node --test --test-isolation=none` từ thư mục `backend/` trước khi commit.

---

## 6. Quy tắc tài liệu

- **README.md** (`VuonNha_1/README.md`): Tổng quan, luồng, hiện trạng, ưu tiên — Codex cập nhật.
- **backend/README.md**: Cấu hình, API, bảo mật — Codex cập nhật.
- **docs/ARCHITECTURE.md**: Mô tả kỹ thuật — cả hai AI cập nhật khi thay đổi kiến trúc.
- **docs/HANDOFF.md**: Trạng thái bàn giao — AI nào hoàn thành việc gì thì cập nhật.
- **AGENTS.md** (file này): Quy tắc — cập nhật khi có thỏa thuận mới.

Không tạo thêm Markdown theo từng lần sửa. Sửa nội dung tại chỗ trong các file trên.
