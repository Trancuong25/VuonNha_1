# Backend Vườn Nhà

Hướng dẫn cấu hình và vận hành, cập nhật 16/10/2026. Tổng quan, luồng người dùng, giới hạn và việc cần bổ sung chỉ cập nhật trong [README chính](../README.md).

## Chạy cục bộ

Node.js 24+, không cần npm install vì dùng module tích hợp. Docker tùy chọn.

1. Sao chép .env.example thành .env nếu chưa có; không ghi đè .env hiện có.
2. Điền GEMINI_API_KEY riêng, GEMINI_MODEL và SCAN_TOKEN. Mặc định mô hình hiện tại: gemini-3.1-flash-lite. Không in/chia sẻ khóa.
3. Bản Android debug dùng http://127.0.0.1:8787/identify và token phát triển vuonnha-local-2026. SCAN_TOKEN cục bộ cần trùng; đây không phải khóa Gemini.
4. Chạy run-backend.cmd và giữ tiến trình; script gọi connect-usb.ps1 để tạo ADB reverse cho thiết bị được cho phép. Nếu server đang chạy, chỉ chạy connect-usb.ps1 khi nối lại thiết bị.

Hoặc từ thư mục backend:

```powershell
node --env-file=.env server.mjs
```

Node mặc định chỉ lắng nghe 127.0.0.1. Khi đổi .env phải khởi động lại đúng server. Nếu cổng 8787 bận, kiểm tra tiến trình trước, không chạy nhiều bản hoặc dừng tiến trình không xác định.

```powershell
Invoke-RestMethod http://127.0.0.1:8787/health
```

ready chỉ xác nhận đã có biến cấu hình, không xác nhận khóa/quota/mô hình gọi thật thành công. Người dùng app không nhập API key. API Gemini phụ thuộc hạn mức tài khoản, không tự bật Billing hoặc chuyển sang dịch vụ tính phí.

## Docker (tùy chọn)

```powershell
docker compose up -d --build
# Đổi .env sau khi đã chạy:
docker compose up -d --force-recreate
docker compose logs --tail=30
docker compose down
```

compose.yaml chỉ mở cổng máy chủ ở 127.0.0.1, mount volume garden-data vào /app/data để giữ SQLite. Không chạy Docker và Node trên cùng cổng đồng thời. Không dùng down -v nếu muốn giữ tài khoản/chat. Container/image không chứa .env; cấu hình truyền lúc chạy.

## API và quyền truy cập

| API | Công dụng |
|---|---|
| GET /health | Trạng thái cấu hình, không kiểm tra AI thật. |
| GET /plants?q=... | Tìm danh mục, nếu ngoài danh mục dùng Gemini; AI cần Bearer SCAN_TOKEN. |
| POST /identify | Ảnh JPEG base64; Bearer SCAN_TOKEN; trả gợi ý đã kiểm tra schema. |
| POST /auth/register | Tạo tài khoản: name, username, password. |
| POST /auth/login | Tên đăng nhập/mật khẩu → phiên. |
| GET /auth/me | Đọc tài khoản từ X-Account-Session. |
| POST /auth/logout | Thu hồi phiên X-Account-Session. |
| GET /auth/backup/meta | Metadata bản ZIP của tài khoản theo X-Account-Session, hoặc null. |
| PUT /auth/backup | ZIP tối đa 25 MB; cần session, Content-Type application/zip và X-Backup-Revision đã biết (0 nếu chưa có). Sai phiên bản trả 409, giữ bản cũ. |
| GET /auth/backup | ZIP đầy đủ theo session; header X-Backup-Revision và X-Backup-Checksum SHA-256. |
| GET /assistant | Hội thoại chưa hết hạn. |
| POST /assistant | question và garden snapshot; trả lời từ log hoặc Gemini. |
| DELETE /assistant | Xóa hội thoại trong namespace hiện tại. |
| DELETE /garden | Xóa snapshot và hội thoại, không xóa Room trên điện thoại. |

Assistant/garden dùng cơ chế token phát triển hiện tại và X-Garden-Key của bản cài. Khi có X-Account-Session hợp lệ, backend chọn namespace tài khoản thay khóa khách. Phiên sai không được âm thầm dùng như khách. Đây chưa là cơ chế xác thực/quota hoàn chỉnh cho toàn bộ API công khai.

- Tên đăng nhập 3–40 ký tự ASCII: chữ, số, dấu chấm/gạch dưới/gạch ngang; chuẩn hóa chữ thường. Tên hiển thị 1–80 ký tự; mật khẩu 10–128.
- Mật khẩu scrypt + salt; session ngẫu nhiên, chỉ lưu hash ở backend, tối đa 7 ngày và 5 phiên/tài khoản.
- Giới hạn body/ảnh, số lượt và tác vụ đang chạy; kiểm tra cấu trúc đầu vào/đầu ra. Không ghi khóa/ảnh vào log.
- Tra cứu/nhận diện trả gợi ý AI, không phải kết quả tìm web có nguồn. Không suy đoán giống hoặc chiều cao hiện tại thành số đo thực.
- Chat tối đa 48 giờ; snapshot giữ đến khi thay thế/xóa. Backend dọn chat khi chạy và lúc khởi động lại.

## File quan trọng

| File | Vai trò |
|---|---|
| server.mjs | HTTP và nhận diện ảnh; kết nối các API. |
| accounts.mjs | SQLite tài khoản, scrypt và phiên. |
| assistant.mjs | Snapshot, lịch sử, múi giờ, chat và TTL. |
| plant-catalog.mjs | Danh mục ban đầu có nguồn: kim tiền, hồ điệp, trầu bà lá xẻ. |
| plant-lookup.mjs | Danh mục trước, Gemini khi ngoài danh mục. |
| provider-fetch.mjs, gemini-config.mjs | Timeout/thử lại giới hạn và cấu hình mô hình. |
| *.test.mjs | Kiểm thử bằng provider được kiểm soát. |
| data/accounts.sqlite, data/garden.sqlite | Dữ liệu thật cục bộ, Git bỏ qua; không xóa khi dọn file. |

## Kiểm thử và phát hành

```powershell
node --test --test-isolation=none
```

37 kiểm thử đạt trong lượt rà soát 16/10/2026, không gọi Gemini thật. Có ZIP tài khoản, phiên sai, cách ly tài khoản, dữ liệu qua restart và chống ghi đè bản cũ. Backend giữ ZIP dạng blob có giới hạn, không giải nén; Android kiểm tra archive khi khôi phục. Không coi kết quả là độ chính xác AI hoặc dịch vụ luôn sẵn sàng.

Bảng backups trong accounts.sqlite giữ một ZIP/tài khoản, không hết hạn theo chat 48 giờ. Bản lưu không mã hóa đầu cuối, bảo vệ bằng phiên và quyền truy cập máy chủ. Cần sao lưu accounts.sqlite lẫn garden.sqlite bằng quy trình SQLite phù hợp. Chưa có xóa tài khoản/bản lưu hoặc quên mật khẩu; không quảng bá tạo tài khoản là dữ liệu không bao giờ mất.

Trước phát hành cần HTTPS/hosting, xác thực và quota toàn bộ API, khôi phục/xóa tài khoản, bảo vệ bí mật, sao lưu database và theo dõi vận hành. Release Android hiện chưa có endpoint mặc định; SCAN_ENDPOINT chỉ chấp nhận HTTPS khi cấu hình build. Không đưa token debug chung hoặc khóa Gemini vào APK công khai.
