Hệ thống sử dụng cơ chế xác thực riêng cho giao tiếp nội bộ:
- Sử dụng INTERNAL_API_KEY hoặc Service Token.
- Mỗi service khi gọi service khác phải gửi header:

Authorization: Bearer <INTERNAL_SERVICE_TOKEN>
X-Service-Name: <service-name>
Nguyên tắc:
- Chỉ các service hợp lệ mới được phép gọi Internal API.
- Không sử dụng token người dùng (JWT Azure AD) cho service-to-service.
- Token nội bộ có thể cấu hình qua environment variables.

4.3. ai-service
ai-service thực hiện các tác vụ trí tuệ nhân tạo.
4.3.1. ai-service gọi document-service lấy thông tin văn bản
GET /internal/documents/{id}
Dùng khi:
Kiểm tra văn bản có tồn tại không
Lấy trichYeu, loaiVanBanId, donViChuTriId
Gắn metadata vào kết quả AI
Response:
{
"success": true,
"message": "Get internal document successfully",
"data": {
"id": 1,
"soKyHieu": "123/CV-ABC",
"trichYeu": "Văn bản triển khai hệ thống",
"loaiVanBanId": 1,
"tenLoaiVanBan": "Công văn",
"documentType": "INCOMING",
"donViChuTriId": 1,
"nguoiTaoId": 2,
"hanXuLy": "2026-05-10T17:00:00",
"trangThai": 1,
"daOCR": false,
"daKySo": false
}
}

4.3.2. ai-service gọi document-service lấy nội dung văn bản
GET /internal/documents/{id}/content
Dùng khi:
Tóm tắt văn bản
Phân loại văn bản
Trích xuất metadata
Gợi ý hướng xử lý
Chatbot cần context theo văn bản
Response:
{
"success": true,
"message": "Get document content successfully",
"data": {
"documentId": 1,
"trichYeu": "Văn bản triển khai hệ thống",
"noiDung": "Nội dung văn bản...",
"ocrText": "Nội dung OCR nếu có...",
"language": "vi"
}
}

4.3.3. ai-service gọi document-service lấy file đính kèm
GET /internal/documents/{id}/attachments
Dùng khi:
OCR file PDF
Tóm tắt từ file
Trích xuất metadata từ file
Response:
{
"success": true,
"message": "Get internal document attachments successfully",
"data": [
{
"id": 1,
"tenTep": "van-ban.pdf",
"duongDanTep": "/uploads/van-ban.pdf",
"loaiTep": "pdf",
"kichThuoc": 204800
}
]
}
Ghi chú:
- duongDanTep có thể là Azure Blob SAS URL hoặc internal file URL.
- ai-service sử dụng URL này để tải file trực tiếp phục vụ OCR và xử lý AI.
- URL chỉ dùng nội bộ giữa các service.


4.4.4. ai-service cập nhật trạng thái OCR cho document-service
PATCH /internal/documents/{id}/ocr-status
Dùng khi:
OCR xử lý thành công
document-service cần hiển thị daOCR = true
Nội dung OCR chi tiết vẫn lưu ở DB của ai-service
Request:
{
"daOCR": true
}
Response:
{
"success": true,
"message": "Update OCR status successfully",
"data": {
"documentId": 1,
"daOCR": true
}
}
