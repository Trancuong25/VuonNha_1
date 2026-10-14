# Trạng thái sau rà soát ngày 10/10/2026

> Đây là kết quả lịch sử. Trạng thái và giới hạn bản mới được ghi trong [BAO_CAO_SAN_SANG.md](BAO_CAO_SAN_SANG.md); không dùng thanh điều hướng, số lượng test hoặc mô tả AI cũ để mô tả bản hiện tại.

## Phần đã có

- Thêm cây bằng tên: backend GET /plants?q=... tra cứu danh mục có nguồn, không gọi Gemini. App hiển thị kết quả để chọn rồi điền tên khoa học, chiều cao trưởng thành tham khảo, độ ẩm không khí, đất/giá thể và ánh sáng. Giống và chiều cao thực tế để người dùng nhập; hỗ trợ sửa, xem chi tiết và sao lưu. Danh mục khởi đầu chỉ có kim tiền, lan hồ điệp, trầu bà lá xẻ; loài khác nhập tay. Database v5 thêm profileJson qua migration, giữ dữ liệu cũ.

- Thêm cây/cá/thú cưng, loài tùy nhập, khu vực, lịch chăm sóc, hoàn thành/hoãn, nhật ký và so sánh ảnh, sao lưu ZIP.
- Quét cây mở CameraX trực tiếp trong app; chụp và yêu cầu nhận diện bằng một nút. Vẫn chọn ảnh có sẵn hoặc thêm thủ công được.
- Có phản hồi nhận diện thì tự mở biểu mẫu Thêm vào vườn, cho sửa/bỏ gợi ý và chọn kết quả khác. Chọn loài từ dropdown thay cho nhiều chip chen nhau.
- Lưu cây, lịch và ảnh đầu tiên trong giao dịch Room; nếu thất bại giữ biểu mẫu và xóa ảnh chưa lưu. Không đóng biểu mẫu trước khi lưu thành công; chặn gửi lưu hai lần.
- Nếu có mục cùng tên loài, gợi ý mở mục cũ; không xác nhận cùng cá thể từ ảnh.
- Đã bỏ nhập URL/token ở giao diện người dùng. Gemini key chỉ ở backend. Debug tích hợp địa chỉ phát triển của emulator qua BuildConfig.

## Kiểm tra thực tế

- Tra cứu mới: backend 12/12 test đạt, tra cứu HTTP thật “ho diep” trả hồ sơ lan hồ điệp, không dùng API AI. Unit Android 23 test đạt, build/lint thành công. 3 test giao dịch trên Note9 đạt (thêm test hồ sơ đi qua lưu và sao lưu/khôi phục); 4 test Compose đạt khi mở khóa Note9. Lượt Compose đầu thất bại do điện thoại Dozing, đã chạy lại khi mở khóa. Bản mới đã cài Note9, dữ liệu cũ mở được qua migration v5. Kiểm tra tra cứu thao tác trực tiếp trên Note9 chưa chốt vì biểu mẫu bị đổi trong lúc kiểm tra; không khẳng định đã thử toàn bộ luồng tra cứu tới lưu trực tiếp.

- Sau thay khóa ngày 10/10/2026: cấu hình có khóa, token khớp debug; Google API danh sách mô hình trả HTTP 200 và có gemini-2.5-flash. Đã chạy lại backend với .env mới, Note9 nhận /health HTTP 200 ready=true. Kiểm tra danh sách không xác nhận quota generateContent hay độ chính xác ảnh. File .env được Git bỏ qua.

- assembleDebug, testDebugUnitTest, assembleDebugAndroidTest và lintDebug thành công. 23 unit test đạt (thêm kiểm tra giới hạn dữ liệu sao lưu giải nén); lint có 0 lỗi, 31 cảnh báo (chủ yếu dependency, tài nguyên chưa dùng và gợi ý API).
- 4 kiểm thử Compose biểu mẫu đạt trên emulator API 34: tự điền, bỏ gợi ý, giữ thông tin khi lưu lỗi, trạng thái đang lưu.
- 2 kiểm thử giao dịch trên DB trong bộ nhớ tách biệt đạt: nhập sai rollback và dọn ảnh đã copy; thêm cây quét sai không để lại ảnh/DB. Không tác động vườn của người dùng.
- 10 kiểm thử backend với provider giả lập đạt (`node --test --test-isolation=none`); không tốn lượt API thật.
- Camera trực tiếp đã hiển thị và chụp được trên emulator. Thử HTTP phản hồi có kiểm soát đã xác nhận ảnh gửi đi và biểu mẫu tự mở. Server thử đã dừng và file thử đã xóa; không đưa dữ liệu nhận diện giả vào app/backend chính.
- AVD đang dùng `hw.camera.back=virtualscene`, `hw.camera.front=emulated`: hình cảnh mô phỏng không phải ảnh cây thật. Thử nhận diện nên chọn ảnh cây thật hoặc dùng điện thoại/webcam.

## Phần chưa đủ để phát hành

- Chưa kiểm chứng nhận diện ảnh cây thật qua Gemini trong lượt này. Đã khởi động backend thật bằng Node với .env có sẵn (không hiển thị khóa); /health trả ready=true trên máy tính và từ emulator qua 10.0.2.2. Chưa kiểm chứng khóa/quota Google bằng request ảnh thật. Đóng tiến trình/tắt máy thì cần chạy lại backend/run-backend.cmd.
- Chưa có backend công khai HTTPS và xác thực người dùng/giới hạn quota cho phát hành. Token debug chỉ dùng phát triển; release chưa có endpoint nên chưa hoạt động quét.
- Đã sửa lỗi Note9 không kết nối: bản debug dùng 127.0.0.1 qua ADB reverse cho cả USB và emulator; script backend/connect-usb.ps1 cấu hình chuyển cổng, run-backend.cmd tự gọi script. Note9 đã trả HTTP 200 ready=true từ backend thật. Rút cáp/khởi động lại cần tạo lại chuyển cổng. Đây chỉ là kết nối phát triển, bản phân phối vẫn cần backend HTTPS.
- Chưa nhận diện lại cá thể cây cũ. Nếu cần nhận biết chính xác mục đã lưu, QR/mã cây là hướng phù hợp hơn trong phạm vi đồ án.
- Chưa xác minh nhắc nhở lâu dài trên điện thoại thực, đổi quyền/tối ưu pin. WorkManager không bảo đảm thông báo đúng từng phút.
- Chưa có đồng bộ tài khoản, chăm sóc nhóm, cảm biến hoặc chẩn đoán bệnh cây. Không cần mở rộng các phần này trước khi luồng chính ổn định.
- Đã sửa thông báo mất mạng/timeout nhận diện thành nội dung dễ hiểu. Nhập backup giới hạn JSON 10 MB, ảnh 20 MB, tổng giải nén 200 MB/2000 mục, từ chối file trùng, dọn ảnh mới nếu rollback; nhập JSON cũ thành công dọn ảnh không còn được tham chiếu.

## Đánh giá phạm vi

Đủ nền tảng cho đồ án quản lý chăm sóc cây tại nhà. Nên tập trung cây cảnh trong bản trình bày; cá/thú cưng là nhóm mở rộng vì quy tắc chăm sóc khác nhau. AI đóng vai trò đề xuất tên loài và lịch tham khảo; người dùng duyệt trước khi lưu. Các việc tưới/bón phải tùy chỉnh theo điều kiện thực tế, không thể coi một chu kỳ AI đề xuất là đúng cho mọi cây.
