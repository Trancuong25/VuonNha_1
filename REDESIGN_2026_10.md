# Bản thiết kế Vườn Nhà — 11/10/2026

## Luồng sử dụng
Vườn nhà → Thêm cây hoặc Quét cây → xem kết quả loài → Tiếp theo → biểu mẫu riêng → xác nhận lưu.
Thanh dưới: Vườn nhà, Chăm sóc, Quét cây, Trợ lý, Cài đặt.
Chăm sóc gộp lịch và nhật ký: chọn ngày xem việc dự kiến, việc đã hoàn thành, ảnh/ghi chú. Nút thêm ghi chú mở nhật ký ảnh đã có.

## Dữ liệu cây
Tên loài tách tên gọi riêng. Vị trí đặt cây, khu vực, ánh sáng thực tế, chậu/giá thể là dữ liệu người dùng.
Ánh sáng/độ ẩm/chiều cao trưởng thành gợi ý là tham khảo. Không đoán chiều cao hiện tại hoặc giống.
Gợi ý điền một lần; khi sửa/xóa một trường, tra cứu lại không ghi đè trường đó. Không tự điền khi mất focus.
Lịch có ngày bắt đầu, chu kỳ. Giờ nhắc chung đặt trong Cài đặt. Mốc khởi tạo lịch không phải lịch sử đã chăm sóc.

## Trợ lý và backend
Android Room tiếp tục là nguồn dữ liệu trên điện thoại; chưa có đăng nhập/đồng bộ đa thiết bị.
Khi gửi câu hỏi, Android lấy snapshot nhất quán từ Room (transaction), chỉ gửi tên/loài, vị trí, các trường chăm cây, lịch nhắc và lịch sử hoàn thành.
Không gửi ảnh/ghi chú nhật ký, không gửi thông tin thiết bị ngoài khóa ngẫu nhiên từng bản cài.
Backend lưu SQLite tại backend/data/garden.sqlite (đã bỏ qua Git). Bản đồng bộ được thay thế khi hỏi tiếp theo; không phải sao lưu toàn bộ app.
Authorization SCAN_TOKEN cho môi trường phát triển + khóa ngẫu nhiên riêng từng bản cài qua X-Garden-Key. Backend lưu hash của khóa này, tách vườn/hội thoại theo hash.
Không có tài khoản để khôi phục khóa sau khi gỡ app. Chưa sẵn sàng công khai: cần HTTPS, đăng nhập hoặc đăng ký thiết bị, hạn mức toàn cục và cơ chế quản lý người dùng trước phát hành.
Chat hết hạn trước 48 giờ; kiểm tra khi đọc và mỗi phút lúc backend chạy, dọn lại khi khởi động. SQLite secure_delete bật. App cũng ẩn tin hết hạn.
Xóa dữ liệu Trợ lý xóa hội thoại và snapshot backend của bản cài; không xóa Room/ảnh điện thoại. Câu hỏi sau đồng bộ lại.
Hỏi việc đã làm hôm nay/hôm qua trả từ log thực tế theo múi giờ Việt Nam, không dùng AI; không có log thì nói chưa ghi nhận.
Câu hỏi chăm cây khác gửi Gemini kèm dữ liệu chọn lọc. Không tự thay lịch hoặc vườn từ nội dung trả lời.
Tra cứu tên: danh mục → Gemini khi không có. Kiến thức AI không đồng nghĩa đã tìm kiếm web. Chưa bật Google Search để tránh phát sinh phí khi tài khoản chưa có hạn mức miễn phí phù hợp.

## API
GET /assistant: hội thoại chưa hết hạn.
POST /assistant: {question, garden:{plants,logs}}; ghi snapshot và trả hội thoại.
DELETE /assistant: xóa hội thoại.
DELETE /garden: xóa hội thoại và snapshot của bản cài.
GET /plants?q=...: danh mục hoặc gợi ý AI.
POST /identify: nhận diện ảnh JPEG; profile gợi ý có thể điền vào biểu mẫu.

## Giới hạn bản này
Snapshot tối đa 200 cây, 5000 log, 30 việc/cây; vượt giới hạn báo rõ, không âm thầm cắt log.
Trợ lý biết dữ liệu snapshot lúc gửi, không đọc ảnh/nhật ký và không tự truy cập mọi nguồn trên mạng.
Gemini phụ thuộc model/hạn mức/kết nối tài khoản. Tests dùng provider giả lập không chứng minh AI thật chạy thành công.

## Nguồn tham khảo thiết kế nghiệp vụ
https://extension.umn.edu/garden-and-home/yard-and-garden/gardening-in-minnesota/fertilizing-and-watering-container-plants
https://ai.google.dev/gemini-api/docs/google-search
https://ai.google.dev/gemini-api/docs/pricing

## Kết quả kiểm tra 11/10/2026
- Build APK debug và APK kiểm thử đạt.
- 23 kiểm thử JVM đạt (lịch, chu kỳ, giới hạn sao lưu, loài tùy chỉnh).
- 20 kiểm thử backend đạt: nhận diện, tra cứu, phân tách vườn, whitelisting dữ liệu, hết hạn chat, SQLite qua restart, lỗi dịch vụ.
- Kiểm thử Android: sửa/xóa gợi ý và giữ trống, đổi tên riêng không thay loài, chuyển kết quả sang biểu mẫu, gõ bàn phím, chọn ngày đúng log/ghi chú, điều hướng và câu hỏi database thật, camera đóng trước biểu mẫu đều đạt.
- API thật: câu hỏi chăm kim tiền trả HTTP 200; cây táo ngoài danh mục trả HTTP 200 và 3 lựa chọn loài. Không coi gợi ý là dữ liệu đã xác minh trên web.
- Docker compose config --quiet đạt. Chưa build/chạy container; backend đang chạy bằng Node trên máy.
- Bản mới cài Pixel 6; không sử dụng Note9.
- Tổng cộng 11 kiểm thử Android đạt, gồm 4 kiểm thử biểu mẫu thêm cây hiện có.
