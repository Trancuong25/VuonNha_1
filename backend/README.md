# Cấu hình nhận diện cây từ ảnh

Backend mẫu dành cho phát triển cục bộ, Node.js 24 trở lên, không cần cài thư viện. Khóa Gemini chỉ có trên backend; không đưa vào ứng dụng Android hoặc Git.

Nếu đã có `.env`, nhấp đúp `run-backend.cmd` để chạy và giữ cửa sổ mở. Docker không bắt buộc. Xem [CHAY_APP.md](../CHAY_APP.md) và phạm vi bản hiện tại ở [BAO_CAO_SAN_SANG.md](../BAO_CAO_SAN_SANG.md).

## Chạy trên máy tính

1. Giữ yêu cầu không bật Billing. Kiểm tra mô hình và hạn mức miễn phí được Google AI Studio hiển thị cho tài khoản trước khi chọn cấu hình. Không bảo đảm miễn phí vô hạn; khi hết hạn mức, backend trả lỗi, không tự nâng cấp hay đổi nhà cung cấp.
2. Sao chép `.env.example` thành `.env` trong thư mục này. File `.env` được Git bỏ qua.
3. Điền `GEMINI_API_KEY`, `GEMINI_MODEL=gemini-3.8-flash` và `SCAN_TOKEN` (mã truy cập backend do bạn tự tạo). Không chia sẻ nội dung file.
4. Chạy trong terminal tại thư mục `backend`:

```powershell
node --env-file=.env server.mjs
```

Server chỉ lắng nghe `127.0.0.1`. `GET /health` trả trạng thái đã cấu hình. Khi chưa có cấu hình, quét trả lỗi và không gọi AI.

## Chạy bằng Docker Desktop

Docker Desktop cần chạy chế độ Linux containers. Tạo `.env` như phần trên, rồi chạy trong thư mục `backend`:

```powershell
docker compose up -d --build
Invoke-RestMethod http://127.0.0.1:8787/health
```

`ready: true` chỉ xác nhận ba biến cấu hình đã có giá trị, không xác nhận API key hợp lệ, tài khoản có quyền dùng mô hình hay đã có hạn mức. Quét một ảnh thật mới kiểm tra được kết nối AI.

Backend lắng nghe mọi giao diện bên trong container, nhưng cổng trên máy tính chỉ mở ở `127.0.0.1`. File `.env` được truyền vào lúc chạy; `.dockerignore` loại khóa khỏi image. Đổi cấu hình rồi chạy `docker compose up -d --force-recreate`. Xem log bằng `docker compose logs --tail=30`; dừng bằng `docker compose down`.

## Kết nối Android emulator

Từ bản debug ngày 10/10/2026, cả điện thoại USB và emulator dùng localhost thông qua ADB reverse. Khi cắm/rút cáp hoặc khởi động lại thiết bị, chạy `connect-usb.ps1` hoặc mở lại `run-backend.cmd`; script tạo đường chuyển cổng cho mọi thiết bị đã cho phép gỡ lỗi USB. Giữ cáp và backend chạy trong khi thử. Nếu backend đã chạy, chỉ chạy script kết nối, không chạy thêm server.

Bản debug cấu hình sẵn URL `http://127.0.0.1:8787/identify` và token phát triển `vuonnha-local-2026` trong BuildConfig. Người dùng không nhập URL hoặc mã. Backend local cần SCAN_TOKEN trùng giá trị này. Không đưa GEMINI_API_KEY vào APK.

Bản release lấy URL HTTPS qua Gradle property `SCAN_ENDPOINT` khi build. Hiện chưa có URL triển khai và cơ chế cấp phiên xác thực cho người dùng. Token release để trống có chủ đích; không dùng token debug chung cho app công khai. Chưa đủ điều kiện phát hành tính năng quét cho người dùng thật.

Điện thoại thật dùng `127.0.0.1` với ADB reverse khi thử qua USB. Để đưa lên máy chủ, bổ sung HTTPS, đăng nhập người dùng và hạn mức sử dụng, rồi cấu hình URL HTTPS. Mã truy cập cố định trong mẫu chỉ dành cho thử nghiệm cá nhân, không phải hệ thống xác thực cho sản phẩm công khai.

## Luồng trong ứng dụng

### Tra cứu bằng tên: danh mục trước, AI khi chưa có

Thêm vào vườn → nhập tên loài → Tra cứu thông tin cây → chọn kết quả → kiểm tra và sửa → lưu. API `GET /plants?q=<tên>` ưu tiên `plant-catalog.mjs`; danh mục không cần khóa Gemini. Nếu ngoài danh mục, backend cần xác thực và gọi Gemini để đề xuất tối đa 3 loài, hiển thị là gợi ý AI. Đây không phải tìm kiếm web có nguồn. Khi dịch vụ lỗi hoặc không có kết quả, người dùng vẫn nhập tay được.

Trả tên khoa học, chiều cao trưởng thành tham khảo, độ ẩm không khí, đất/giá thể, ánh sáng, nguồn và chu kỳ kiểm tra gợi ý. Giống và chiều cao đo thực tế do người dùng điền. Thông tin được lưu trên điện thoại và nằm trong sao lưu. Thêm loài mới bằng dữ liệu có nguồn trong plant-catalog.mjs, khởi động lại backend. Với Docker cần build lại image.

Nút **Quét cây** → camera hoặc chọn ảnh → nhận diện → đọc màn hình kết quả → **Tiếp theo** → biểu mẫu **Thêm vào vườn**. Ảnh được thu nhỏ/nén JPEG; backend không lưu ảnh ra đĩa, nhưng gửi ảnh tới Gemini theo chính sách của nhà cung cấp.

Kết quả gồm tối đa ba loài, tên khoa học, đặc điểm nhìn thấy, mức tự đánh giá và hướng dẫn ngắn. Sau khi bấm Tiếp theo, biểu mẫu có thông tin/lịch gợi ý để người dùng duyệt. Không tự lưu hay xác nhận chắc chắn loài. Không nhận diện được trả danh sách rỗng. Cá/thú cưng vẫn thêm thủ công.

- Trong biểu mẫu, **Bỏ gợi ý, tự nhập** xoá thông tin được điền.
- Có thể ẩn nút quét trong Cài đặt và bật lại sau.
- Khi xác nhận lưu, ảnh quét trở thành mốc nhật ký đầu tiên cùng giao dịch thêm cây.
- Mục có cùng tên loài được gợi ý để mở cây cũ; không xác nhận cùng cá thể.

Backend giới hạn kích thước yêu cầu, số lượt quét và xử lý từng lượt. API: `POST /identify`, JSON `{"imageBase64":"…"}`, header `Authorization: Bearer <SCAN_TOKEN>`. Phản hồi theo cấu trúc trong `server.mjs`. Không ghi khóa API/ảnh vào log.

## Kiểm thử

```powershell
node --test
```

Các kiểm thử dùng mô hình giả lập qua dependency injection: kiểm tra xác thực, ảnh không hợp lệ, dữ liệu quá lớn, schema và lỗi provider; không gọi AI hoặc phát sinh phí. Chất lượng nhận diện thực tế cần đánh giá sau khi cấu hình dịch vụ.

Tài liệu chính thức: [Ảnh đầu vào](https://ai.google.dev/gemini-api/docs/image-understanding), [Structured Outputs](https://ai.google.dev/gemini-api/docs/structured-output).

## Hướng dẫn từng bước cho Android Studio emulator

1. Mở Docker Desktop và đợi Docker chạy.
2. Nhấn Windows + E, dán `D:\Hoc_Tap\VuonNha_1\VuonNha_1\backend` vào thanh địa chỉ, Enter.
3. Gõ `powershell` vào thanh địa chỉ của cửa sổ thư mục, Enter.
4. Chạy `notepad .env`. Điền bốn dòng sau (thay phần khóa bằng khóa mới của bạn):

```env
GEMINI_API_KEY=DAN_KHOA_GOOGLE_VAO_DAY
GEMINI_MODEL=gemini-3.8-flash
SCAN_TOKEN=vuonnha-local-2026
PORT=8787
```

5. Ctrl + S, đóng Notepad. Không để file thành `.env.txt`.
6. Trong PowerShell chạy `docker compose up -d --build`.
7. Chạy `Invoke-RestMethod http://127.0.0.1:8787/health`. `ready: True` chỉ xác nhận cấu hình, chưa kiểm tra khóa với Google.
8. Bản debug mới tự kết nối backend emulator; không nhập cấu hình trong app.
9. Quét cây → đưa cây vào camera hoặc Chọn ảnh → nhận diện → đọc kết quả → Tiếp theo → sửa biểu mẫu và lưu.

Nếu đã chạy container trước khi đổi khóa, chạy lại `docker compose up -d --build --force-recreate`.


## Trợ lý và dữ liệu vườn
Trợ lý dùng SQLite ở data/garden.sqlite; Docker giữ database trong volume garden-data. Thông tin vườn chỉ đồng bộ khi gửi câu hỏi. Hội thoại hết hạn trước 48 giờ; có API xóa theo từng bản cài. Xem [Thiết kế mới](../REDESIGN_2026_10.md) để biết dữ liệu, API và giới hạn triển khai. Không bật Billing hoặc Google Search tự động; kiến thức AI không đồng nghĩa tìm kiếm web.
