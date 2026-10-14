# Kiểm tra lỗi không phản hồi

## Lỗi tái diễn sau lượt kiểm thử đầu

Trace mới `../anr-form-repeat.txt`: input timeout 5107 ms; main thread đang chèn các node Compose, không còn đứng ở FlowRow. Memory pressure bằng 0 và system load thấp trước lỗi. Camera vẫn hoạt động lúc chuyển sang biểu mẫu, đóng muộn sau khi ANR được ghi nhận. Vì vậy việc đổi FlowRow trước đó chưa xử lý đầy đủ; không kết luận thiếu RAM hay backend là nguyên nhân.

Các kiểm thử biểu mẫu cũ không mở camera thật. Bổ sung CameraToFormTest chạy MainActivity, mở camera thật rồi chuyển sang nhập thủ công, mở bàn phím và gửi sự kiện gõ native Android nhiều lần. Camera preview đổi sang SurfaceView khi thiết bị hỗ trợ, yêu cầu độ phân giải 640×480 và 15 fps; ảnh chụp yêu cầu 1280×960 với fallback theo khả năng thiết bị. Các giá trị là yêu cầu, không bảo đảm camera mọi máy cung cấp đúng. Tham khảo [CameraX Preview.Builder](https://developer.android.com/reference/androidx/camera/core/Preview.Builder).

Khi chuyển từ camera sang nhập thủ công, gọi unbind và chờ CameraState.CLOSED trước khi dựng biểu mẫu. Có trạng thái đang đóng camera và thông báo nếu chờ quá 10 giây; vẫn có thể quay về vườn. Kiểm thử tích hợp đầu tiên đạt (37,327 giây), xác nhận camera thật mở và không còn client Vườn Nhà trước khi nhập, gửi gõ native ba lượt và quay lại camera. Đây là bằng chứng cho đường đi được kiểm tra, chưa xác định camera là nguyên nhân duy nhất hay mọi trường hợp ANR đã hết.

Lượt chạy lặp lại cùng kiểm thử tích hợp cũng đạt (37,735 giây). Log sự kiện chỉ còn ANR cũ trước bản sửa camera; chưa thấy ANR mới trong hai lượt này. Bản sửa đã cài lên Pixel 6, không thao tác điện thoại.

Ảnh báo “VuonNha isn't responding” khớp log ANR của MainActivity. Android đợi thao tác chạm 5126 ms. Trace lưu ở `../anr-form-before.txt`.

## Bằng chứng

- Main thread đang chạy đo FlowRow (`FlowLayoutOverflowState`, `FlowMeasurePolicy`) trong biểu mẫu cuộn có xử lý bàn phím.
- Máy ảo cũng có ANR của SystemUI, Google Play Services và các ứng dụng nền; đây không phải lỗi riêng backend.
- Máy tính có khoảng 16 GB RAM, lúc đọc còn gần 2 GB trống; máy ảo dùng khoảng 4 GB ở phía Windows. Không tăng thêm RAM máy ảo khi chưa cần.
- AVD Pixel 6: API 34, x86_64, 4 CPU, cấu hình RAM 2048 MB, GPU auto. Không đổi hoặc xóa dữ liệu AVD.
- SDK ở C, cache Gradle ở D. Ổ C còn khoảng 86 GB và D hơn 670 GB lúc kiểm tra; không thiếu dung lượng.
- Backend lúc đầu không chạy. Sau khởi động lại, health ready và tra cứu hồ điệp trả một kết quả; USB reverse máy ảo cổng 8787 hoạt động. Health không chứng minh API Gemini hợp lệ hay ảnh thật nhận diện thành công.

## Sửa và cấu hình

- Thay FlowRow ở biểu mẫu thêm/sửa bằng Row cuộn ngang cho các nút chọn nhỏ, tránh đường đo FlowRow xuất hiện trong trace.
- Đặt xử lý IME/navigation inset bên ngoài phần cuộn để bàn phím co viewport thay vì thêm khoảng đệm bên trong nội dung cuộn.
- Giới hạn Gradle 2 worker; dừng daemon sau khi build để dành RAM cho máy ảo.
- Giữ nguyên cấu hình SDK, phiên bản thư viện và dữ liệu ứng dụng.

Chưa khẳng định một frame trong trace là nguyên nhân duy nhất của ANR. Cần kiểm tra hồi quy nhập bàn phím nhiều lần trên bản đã sửa; nếu lỗi tái diễn, thu trace mới thay vì tiếp tục thay RAM hoặc backend theo phỏng đoán.

## Kết quả bản sửa

Build thành công, 23 kiểm thử JVM đạt. Cả 7 kiểm thử giao diện Pixel 6 đạt (70,397 giây), gồm nhập tên, mở/đóng bàn phím, đổi nhóm bốn lần, chuyển kết quả → biểu mẫu, quay lại và lưu tên riêng. Log sự kiện sau lượt kiểm tra chỉ còn ANR cũ nêu trên, không ghi nhận thêm ANR Vườn Nhà trong lượt này. Đây là kiểm tra hồi quy có giới hạn, không chứng minh mọi thao tác đều hết ANR.
