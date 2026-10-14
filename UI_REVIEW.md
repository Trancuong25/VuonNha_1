# Rà soát giao diện — 10/10/2026

## Luồng quét mới

Camera → kết quả nhận diện → Tiếp theo → màn hình thêm cây độc lập → Lưu vào vườn.
Kết quả gồm ảnh, tên loài, tên khoa học, đặc điểm, mức chắc chắn và gợi ý chăm sóc. Có thể chọn loài khác hoặc chụp lại. Cây cùng loài đã có trong vườn được gợi ý mở; không khẳng định cùng một cá thể.
Màn hình thêm dùng thông tin điền sẵn và cho đổi tên gọi riêng; tên loài và tên khoa học được lưu riêng. Chiều cao thực tế và giống chưa xác định không tự bịa. Quay lại kết quả giữ ảnh và kết quả, nhưng chưa giữ các sửa đổi biểu mẫu đã rời khỏi. Chỉ thao tác Lưu mới ghi cây và ảnh vào dữ liệu.

Build mới đạt; 23 kiểm thử JVM đạt. Sáu kiểm thử giao diện chạy trên máy ảo Pixel 6 đạt, gồm hai kiểm thử luồng kết quả/biểu mẫu và tên gọi riêng mới. Dữ liệu nhận diện trong các kiểm thử là mẫu cố định, không phải kết quả gọi Gemini thật. Bản mới đã cài trên máy ảo; không cài hoặc thao tác trên điện thoại trong đợt này.

## Thay đổi

- Thống nhất nền, màu xanh, tương phản chế độ tối, tiêu đề và khoảng cách các màn hình vườn, lịch, nhắc nhở, nhật ký, thời tiết, cài đặt và chi tiết.
- Vườn dùng nút lọc rõ trạng thái: Trong vườn / Đến hạn; giữ nút Thêm vào vườn và quét cây.
- Biểu mẫu thêm chia ba phần: chọn loài, tên/vị trí, lịch chăm sóc. Thông tin tham khảo mở khi cần.
- Giữ thông tin tự nhập khi đổi sang danh sách rồi quay lại. Không lưu khi đang tra cứu. Giới hạn tên loài 120 ký tự.
- Trang chi tiết đặt ảnh và tên cây trước thông tin tham khảo. Nút hành động và chọn giao diện xuống dòng khi thiếu chiều rộng.
- Thay hướng dẫn cũ “Tổng quan” bằng “Vườn nhà”.

## Kết quả và giới hạn

- Build debug, unit tests và lint đạt; lint còn 31 cảnh báo, không có lỗi.
- Backend: 12 kiểm thử đạt; API tra cứu “hồ điệp” trả một kết quả.
- Bản mới đã cài lên Note9, giữ dữ liệu ứng dụng.
- Ba kiểm thử lưu/khôi phục và rollback dữ liệu trên Note9 đạt. Bốn kiểm thử giao diện đạt sau khi người dùng mở khóa Note9. Lần đầu thất bại vì điện thoại Dozing, màn hình OFF và khóa đang hiện.
- Danh mục tra cứu chỉ có ba loài được nhập dữ liệu có nguồn. Cây khác dùng nhập thủ công; chưa phải cơ sở dữ liệu mọi loài.
- Debug cần máy tính chạy backend và USB reverse. Release chưa có địa chỉ dịch vụ HTTPS công khai; chưa sẵn sàng phân phối chức năng tra cứu cho mọi người.
- Kiểm thử backend dùng phản hồi AI giả lập, không chứng minh nhận diện ảnh thật hoạt động. Phần camera chưa được thiết kế lại trong đợt này theo yêu cầu bỏ qua phần chụp trước đó.
- Chưa kiểm tra trực quan mọi màn hình với mọi cỡ chữ và mọi thiết bị; không khẳng định ứng dụng hoàn toàn không còn lỗi.
