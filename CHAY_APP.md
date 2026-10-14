# Chạy Vườn Nhà trên Pixel 6

1. Mở Android Studio → Open → chọn `D:\Hoc_Tap\VuonNha_1\VuonNha_1` (thư mục có `settings.gradle.kts`). Chờ Gradle Sync hoàn tất.
2. Device Manager → Pixel 6 → nút ▶. Đợi giả lập vào màn hình Android.
3. Mở thư mục `D:\Hoc_Tap\VuonNha_1\VuonNha_1\backend`, chạy `run-backend.cmd`. Giữ cửa sổ này mở. File dùng `.env` hiện có; không cần nhập key trong app.
4. Trên thanh công cụ Android Studio chọn Pixel 6 và cấu hình `app`, bấm Run ▶.

Để nhận nhắc chăm sóc, vào Cài đặt → Thông báo và cấp quyền khi Android hỏi. Bản mới không hỏi quyền thông báo ngay lúc mở ứng dụng.

## Nếu app báo không kết nối được

Mở Terminal trong Android Studio, chạy:

```powershell
& 'C:\Users\DELL\AppData\Local\Android\Sdk\platform-tools\adb.exe' -s emulator-5554 reverse tcp:8787 tcp:8787
Invoke-RestMethod http://127.0.0.1:8787/health
```

`ready: true` nghĩa là backend có cấu hình; không chứng minh Gemini còn quota hay ảnh được nhận diện đúng. Nếu connection refused, chạy lại `run-backend.cmd`. Nếu backend đã mở trước khi giả lập khởi động, chạy lại lệnh reverse trên.

Vườn, nhật ký và lịch lưu trên thiết bị. Nhận diện, tra cứu qua backend và trợ lý cần backend đang chạy và kết nối mạng khi gọi Gemini.

## Bộ giao diện mới

- Typography và khoảng cách thống nhất; giao diện sáng/tối dùng cùng hệ màu xanh.
- Vườn nhà có tóm tắt việc hôm nay và hướng dẫn lần đầu.
- Biểu mẫu chia thành Loài cây → Tên/vị trí → Lịch chăm sóc.
- Kết quả nhận diện có ảnh, hồ sơ dễ đọc và nút tiếp theo riêng.
- Lịch, nhật ký và chi tiết cây dùng cùng hệ tiêu đề/thẻ.
- Trợ lý đưa quản lý hội thoại vào menu, giữ vùng nhập ở cuối.
- Cài đặt dùng các hàng tuỳ chọn; giao diện mở hộp chọn, giờ nhắc dùng bộ chọn giờ.

Đây là bản chạy cục bộ phục vụ phát triển và đồ án; chưa phải backend công khai cho APK phát hành.

## Kiểm tra giao diện ngày 14/10/2026
Build debug và APK kiểm thử đạt; 23 kiểm thử JVM đạt. 11 kiểm thử Android đạt qua lượt đầu và chạy lại các bài bị ảnh hưởng. Lượt đầu có 4 lỗi do hộp thoại hệ thống giả lập và mất focus; sau xử lý hộp thoại, chạy lại 6 bài đạt. Đã xem ảnh Vườn nhà, Chăm sóc, Trợ lý và Cài đặt mới. Không kết luận mọi ANR của giả lập đã được giải quyết.
