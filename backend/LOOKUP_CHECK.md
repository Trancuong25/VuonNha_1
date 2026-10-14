Tra cứu tên cây
- GET /plants?q=... tìm danh mục trước; nếu không thấy, dùng Gemini qua backend.
- Tra cứu AI yêu cầu Bearer SCAN_TOKEN; key Gemini chỉ nằm trong backend/.env.
- Kết quả AI là gợi ý chưa xác minh, không phải kết quả tìm kiếm web. Không tự tạo chiều cao hiện tại/giống hoặc lịch tưới.
- Bản APK debug dùng ADB reverse tcp:8787 tcp:8787.
- API thực tế ngày 10/10: model 2.5-flash trả 404, yêu cầu 3.8-flash. Đã đổi .env; 3.8-flash trả 503 hoặc timeout. Chưa xác nhận tra cứu trực tuyến thành công.
- 12 kiểm thử backend đạt; APK assembleDebug đạt.
Nhập tiếng Việt
- Ô nhập giữ nguyên Unicode, không bỏ dấu.
- Bật tiếng Việt trong Gboard: Settings > System > Keyboard > On-screen keyboard > Gboard > Languages > Add keyboard > Vietnamese > Telex.
- Khi dùng bàn phím máy tính, bộ gõ Windows có thể không truyền đúng ký tự qua emulator. Thử gõ bằng bàn phím Gboard trên màn hình.
