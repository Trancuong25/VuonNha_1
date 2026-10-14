# Vườn Nhà

Ứng dụng Android theo dõi cây cảnh, cá và thú cưng nhỏ trong nhà, viết bằng Kotlin và Jetpack Compose. Dữ liệu lưu cục bộ bằng Room; lịch nhắc dùng WorkManager.

Hướng dẫn chạy hiện tại: [CHAY_APP.md](CHAY_APP.md). Thiết kế mới: [REDESIGN_2026_10.md](REDESIGN_2026_10.md). Phạm vi và giới hạn khi báo cáo: [BAO_CAO_SAN_SANG.md](BAO_CAO_SAN_SANG.md). Các tài liệu ngày 10/10 là lịch sử kiểm tra, không mô tả đầy đủ bản mới.

## Chức năng

**Quét cây:** CameraX chụp hoặc chọn ảnh → backend gọi Gemini → màn hình đọc kết quả → Tiếp theo → biểu mẫu điền sẵn → người dùng duyệt và lưu. Ảnh quét có thể làm mốc nhật ký đầu tiên cùng giao dịch thêm cây. Gợi ý cùng loài không xác nhận cùng cá thể. Có thể nhập thủ công khi mất kết nối; không tạo kết quả nhận diện giả. Backend và cấu hình ở [backend/README.md](backend/README.md).

Thanh dưới gồm Vườn nhà, Chăm sóc, Quét cây, Trợ lý, Cài đặt. Chăm sóc kết hợp lịch và nhật ký theo ngày; ảnh/ghi chú vẫn có màn hình riêng để soạn và so sánh. Menu Vườn nhà có thời tiết và khu vực. Giao diện hỗ trợ sáng/tối.

- **Vườn:** thêm đối tượng theo loài, đặt tên, phân khu vực, chỉnh chu kỳ từng việc, thêm việc riêng và xoá đối tượng.
- **Loài khác / chưa rõ tên:** chọn nhóm cây, cá hoặc thú cưng; nhập tên loài hay mô tả tạm và tên gọi riêng. Tự thêm công việc, chu kỳ và ngày khởi tạo; có thể lưu chưa có lịch rồi bổ sung sau. Tên loài tự nhập được giữ khi sao lưu/khôi phục và sửa được trong thông tin đối tượng. Tìm kiếm hỗ trợ cả tên gọi lẫn tên loài.
- **Hôm nay:** theo dõi việc đến hạn/quá hạn, đánh dấu hoàn thành hoặc hoãn; lưu lịch sử chăm sóc.
- **Lịch tuần/tháng:** mở từ Hôm nay, chuyển kỳ, chọn ngày để xem việc, lịch sử hoàn thành và mở chi tiết đối tượng. Các lần lặp sau ngày đến hạn tiếp theo là dự kiến; việc quá hạn giữ nguyên ở ngày đến hạn và vẫn hiển thị trong Hôm nay.
- **Ngày khởi tạo lịch:** khi thêm vào vườn, chọn lần chăm sóc gần nhất hoặc ngày nhắc đầu tiên cho từng việc. Ngày khởi tạo không tạo lịch sử hoàn thành.
- **Chi tiết đối tượng:** bấm thẻ trong Vườn hoặc việc trong lịch để xem lịch riêng, lần chăm sóc gần nhất, biểu đồ, mốc ảnh và lịch sử; có thể sửa chu kỳ, hoàn thành/hoãn và mở nhật ký của đúng đối tượng.
- **Nhật ký:** ghi chú, chọn ảnh hoặc chụp ảnh theo mốc thời gian; so sánh ảnh đầu tiên với ảnh mới nhất và xem biểu đồ chăm sóc.
- **Thời tiết:** dự báo và gợi ý chăm sóc theo khu vực; cần mạng để lấy dữ liệu mới.
- **Trợ lý:** lịch sử đã làm lấy từ CareLog, phân biệt với mốc khởi tạo lịch. Hướng dẫn khác dùng Gemini và dữ liệu vườn, chưa có tìm kiếm web có nguồn. Hội thoại hết hạn tối đa 48 giờ; bản đồng bộ vườn giữ đến khi thay thế/xoá.
- **Cài đặt:** giao diện sáng/tối, giờ nhắc, quản lý quyền thông báo, xuất/nhập bản sao lưu ZIP có ảnh. Không có nút thử nghiệm trong giao diện người dùng.

## Chạy và kiểm thử

Mở thư mục này bằng Android Studio, chọn JDK phù hợp với cấu hình Gradle (daemon JDK 25), cài SDK Android 37 và chạy module `app`. Android tối thiểu: 8.0 (API 26).

```powershell
./gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

APK debug: `app/build/outputs/apk/debug/app-debug.apk`.

Kiểm thử lịch trong `ScheduleTest` kiểm tra ngày đến hạn qua thời điểm đổi giờ mùa hè, quá hạn, hoãn và hoàn thành. Thông báo cần quyền trên Android 13 trở lên; WorkManager có thể chạy trễ theo giới hạn nền của Android.

`CareCalendarTest` kiểm tra ngày nhắc đầu tiên, khởi tạo từ lần chăm sóc gần nhất, các lần lặp qua tháng, khoảng ngày tương lai và cập nhật khi hoãn/hoàn thành. Nhận diện ảnh dùng Gemini qua backend; đồng bộ chăm sóc gia đình chưa có.

## Kiểm tra trên thiết bị

1. Thêm một cây và một vật nuôi, đặt chu kỳ khác nhau, đóng/mở app để kiểm tra dữ liệu còn nguyên.
2. Sửa chu kỳ, hoàn thành/hoãn một việc và kiểm tra ngày đến hạn cùng lịch sử.
3. Chọn ảnh, ghi chú và lưu nhật ký; xoay màn hình khi soạn. Thử ảnh không đọc được: nội dung phải còn để thử lại.
4. Chụp ảnh nếu thiết bị có camera; thêm ít nhất hai ảnh để xem so sánh. Huỷ chọn ảnh phải giữ ảnh đã chọn trước đó.
5. Xoá một mốc: kiểm tra hộp xác nhận và cả nút Huỷ.
6. Cấp quyền thông báo trong Cài đặt và kiểm tra nhắc việc thực tế cùng nút hoàn thành/hoãn. WorkManager không bảo đảm đúng từng phút.
7. Xuất ZIP, nhập lại và kiểm tra danh sách, chu kỳ, lịch sử và ảnh.
8. Thêm đối tượng với ngày khởi tạo riêng cho từng việc; kiểm tra ngày đến hạn trong chi tiết và lịch. Không có lịch sử hoàn thành tự sinh.
9. Trong Hôm nay, mở lịch, chuyển tuần/tháng, đổi kỳ và chọn ngày; kiểm tra cả việc dự kiến và lịch sử hoàn thành.
10. Hoàn thành/hoãn từ chi tiết hoặc lịch: ngày đến hạn và các lần dự kiến phải cập nhật; ngày hoàn thành phải có dấu ✓.
11. Mở chi tiết, bấm Ghi nhật ký và xác nhận app chọn đúng cây/vật nuôi.
12. Thêm loài ngoài danh sách (VD: Kim tiền), đặt tên riêng và lịch tuỳ chọn. Kiểm tra tìm theo tên loài, mở chi tiết, sửa tên loài và xuất/nhập ZIP. Thử lưu loài chưa rõ tên với mô tả và chưa đặt lịch.

Các kiểm tra tương tác, camera và thông báo cần chạy trên thiết bị hoặc emulator; build và unit test không thay thế các kiểm tra này.
