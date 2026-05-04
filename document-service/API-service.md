Cơ chế xác thực giữa các service
Hệ thống sử dụng cơ chế xác thực riêng cho giao tiếp nội bộ:
- Sử dụng INTERNAL_API_KEY hoặc Service Token.
- Mỗi service khi gọi service khác phải gửi header:

Authorization: Bearer <INTERNAL_SERVICE_TOKEN>
X-Service-Name: <service-name>
Nguyên tắc:
- Chỉ các service hợp lệ mới được phép gọi Internal API.
- Không sử dụng token người dùng (JWT Azure AD) cho service-to-service.
- Token nội bộ có thể cấu hình qua environment variables.
  4.1. document-service
  document-service là service trung tâm quản lý dữ liệu văn bản, chịu trách nhiệm tạo văn bản, cập nhật văn bản, lưu file, lưu trạng thái xử lý và lưu kết quả AI.
  4.1.1. document-service gọi auth-service
  Mục đích:
  Kiểm tra người dùng có tồn tại không
  Kiểm tra đơn vị có tồn tại không
  Kiểm tra vai trò, quyền của người dùng
  Validate người ký, người nhận, người phê duyệt, đơn vị chủ trì, đơn vị nhận

API 1: Lấy thông tin user
GET /internal/auth/users/{id}
Trả về:
{
"id": 1,
"username": "nva",
"hoTen": "Nguyễn Văn A",
"email": "nva@company.com",
"donViId": 1,
"tenDonVi": "Phòng Hành chính",
"nhomQuyenId": 2,
"maNhomQuyen": "CHUYEN_VIEN",
"trangThai": 1
}
Dùng khi:
Kiểm tra nguoiTaoId
Kiểm tra nguoiKyId
Kiểm tra nguoiNhanId
Kiểm tra nguoiPheDuyetId

API 2: Kiểm tra nhiều user tồn tại
POST /internal/auth/users/validate
Trả về:
{
"valid": true,
"invalidUserIds": []
}
Dùng khi:
Gửi văn bản cho nhiều người
Gửi góp ý văn bản
Gửi thông báo nhiều người
Validate nguoiNhanIds

API 3: Lấy thông tin đơn vị
GET /internal/auth/units/{id}
Trả về:
{
"id": 1,
"maDonVi": "HC",
"tenDonVi": "Phòng Hành chính",
"donViChaId": null,
"suDung": true
}
Dùng khi:
Kiểm tra donViChuTriId
Kiểm tra donViXuLyId
Kiểm tra donViNhanId

API 4: Kiểm tra nhiều đơn vị tồn tại
POST /internal/auth/units/validate
Trả về:
{
"valid": true,
"invalidUnitIds": []
}
Dùng khi:
Gửi văn bản cho nhiều đơn vị
Validate donViNhanIds

API 5: Lấy vai trò user
GET /internal/auth/users/{id}/roles
Trả về:
{
"userId": 1,
"roles": ["CHUYEN_VIEN"],
"permissions": [
{
"maChucNang": "DOCUMENT_INCOMING",
"isView": true,
"isCreate": false,
"isEdit": true,
"isDelete": false,
"isApprove": false
}
]
}
Dùng khi:
Kiểm tra người dùng có vai trò phù hợp không
Kiểm tra người ký có phải lãnh đạo không
Kiểm tra người nhận có quyền xử lý văn bản không

API 6: Kiểm tra quyền
POST /internal/auth/permissions/check
Trả về:
{
"allowed": true,
"userId": 1,
"maChucNang": "DOCUMENT_INCOMING",
"permission": "IsEdit"
}
Dùng khi:
Tạo văn bản
Cập nhật văn bản
Chuyển xử lý
Trình ký
Phát hành văn bản

4.1.2. document-service gọi workflow-service
Mục đích:
Khởi tạo workflow cho văn bản
Chuyển xử lý văn bản
Trình phê duyệt văn bản
Lấy trạng thái xử lý
Lấy timeline xử lý

API 1: Khởi tạo workflow cho văn bản
POST /internal/workflows/documents/{documentId}/start
Trả về:
{
"documentId": 1,
"workflowId": 1,
"processingId": 10,
"currentStep": "Văn thư tiếp nhận",
"trangThaiXuLy": 1
}
Dùng khi:
Tạo văn bản đến
Tạo văn bản đi
Tạo hồ sơ/văn bản cần đi theo quy trình

API 2: Chuyển xử lý văn bản
POST /internal/workflows/documents/{documentId}/transfer
Trả về:
{
"processingId": 20,
"documentId": 1,
"nguoiNhanId": 2,
"trangThaiXuLy": 1
}
Dùng khi:
Chuyển văn bản đến cho chuyên viên
Luân chuyển văn bản qua bước xử lý tiếp theo

API 3: Trình phê duyệt văn bản
POST /internal/workflows/documents/{documentId}/submit-approval
Trả về:
{
"documentId": 1,
"processingId": 30,
"nguoiPheDuyetId": 4,
"trangThaiXuLy": 1
}
Dùng khi:
Trình ký văn bản nháp
Gửi phê duyệt văn bản đi
Trình lãnh đạo duyệt văn bản

API 4: Lấy trạng thái workflow của văn bản
GET /internal/workflows/documents/{documentId}/status
Trả về:
{
"documentId": 1,
"currentStep": "Lãnh đạo phê duyệt",
"trangThaiXuLy": 1,
"tyLeHoanThanh": 60,
"hanXuLy": "2026-05-02T17:00:00",
"isOverdue": false
}
Dùng khi:
Xem chi tiết văn bản
Hiển thị trạng thái xử lý văn bản
Kiểm tra văn bản có quá hạn không

API 5: Lấy timeline workflow
GET /internal/workflows/documents/{documentId}/timeline
Trả về:
[
{
"processingId": 1,
"tenBuoc": "Văn thư tiếp nhận",
"nguoiXuLyId": 1,
"hanhDongXuLy": "CREATE",
"ngayNhan": "2026-04-30T08:00:00",
"ngayHoanThanh": "2026-04-30T09:00:00",
"trangThaiXuLy": 2
}
]
Dùng khi:
Xem lịch sử xử lý văn bản
Xem tiến trình luân chuyển văn bản

4.1.4. document-service gửi notification event qua Kafka
document-service không gọi trực tiếp notification-service bằng REST API. Khi phát sinh nghiệp vụ cần thông báo, document-service publish event vào Kafka để notification-service consume và xử lý.
Mục đích:
Gửi thông báo khi có văn bản mới
Gửi thông báo khi chuyển xử lý
Gửi thông báo khi trình ký
Gửi thông báo khi phát hành văn bản
Kafka topic:
notification-events
Event mẫu: thông báo một người
{
"eventId": "evt-001",
"eventType": "DOCUMENT_CREATED",
"sourceService": "document-service",
"nguoiNhanIds": [2],
"tieuDe": "Thông báo văn bản mới",
"noiDung": "Bạn có văn bản mới cần xử lý",
"loaiThongBao": "VAN_BAN",
"kenhGui": ["SYSTEM", "EMAIL"],
"referenceType": "DOCUMENT",
"referenceId": 1,
"metadata": {
"documentId": 1,
"soKyHieu": "123/CV-ABC",
"trichYeu": "Văn bản triển khai hệ thống"
},
"createdAt": "2026-04-30T10:00:00"
}
Event mẫu: thông báo nhiều người
{
"eventId": "evt-002",
"eventType": "DOCUMENT_PUBLISHED",
"sourceService": "document-service",
"nguoiNhanIds": [2, 3, 4],
"tieuDe": "Văn bản đã được phát hành",
"noiDung": "Một văn bản mới đã được phát hành đến bạn",
"loaiThongBao": "PHAT_HANH_VAN_BAN",
"kenhGui": ["SYSTEM", "EMAIL"],
"referenceType": "DOCUMENT",
"referenceId": 1,
"metadata": {
"documentId": 1,
"ngayPhatHanh": "2026-04-30"
},
"createdAt": "2026-04-30T10:00:00"
}
Kết quả xử lý:
document-service chỉ publish event thành công vào Kafka.
notification-service tự consume event, lưu thông báo vào DB và gửi qua các kênh tương ứng.
Nếu gửi thông báo lỗi, không làm rollback nghiệp vụ tạo/chuyển/phát hành văn bản


7.3. Internal API của document-service
Các service gọi đến document-service:
workflow-service
ai-service
report-service
document-service chỉ quản lý dữ liệu văn bản, file, trạng thái văn bản và thông tin nghiệp vụ văn bản.
document-service không quản lý AI-result.

7.3.1. Lấy thông tin văn bản
GET /internal/documents/{id}
Dùng cho:
workflow-service lấy thông tin văn bản khi xử lý workflow
ai-service lấy metadata văn bản trước khi xử lý AI
report-service lấy thông tin văn bản nếu cần tổng hợp
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

7.3.2. Lấy nội dung văn bản cho AI
GET /internal/documents/{id}/content
Dùng cho:
ai-service lấy nội dung để tóm tắt
ai-service lấy nội dung để phân loại
ai-service lấy nội dung để trích xuất metadata
ai-service lấy nội dung để gợi ý xử lý
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
Ghi chú:
Nếu OCR-result lưu ở ai-service, trường ocrText có thể bỏ.
Nếu document-service vẫn lưu nội dung OCR phục vụ tìm kiếm nhanh thì có thể giữ.

7.3.3. Lấy file đính kèm của văn bản
GET /internal/documents/{id}/attachments
Dùng cho:
ai-service lấy file để OCR
ai-service lấy file để tóm tắt từ file
ai-service lấy file để trích xuất metadata từ file
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

7.3.4. Cập nhật trạng thái văn bản
PATCH /internal/documents/{id}/status
Service gọi:
workflow-service
Request:
{
"trangThai": 3,
"reason": "Đã gửi phê duyệt",
"updatedByService": "workflow-service"
}
Response:
{
"success": true,
"message": "Update document status successfully",
"data": {
"documentId": 1,
"trangThai": 3
}
}

7.3.5. Cập nhật người đang xử lý
PATCH /internal/documents/{id}/assignee
Service gọi:
workflow-service
Request:
{
"nguoiXuLyId": 2,
"donViXuLyId": 1,
"hanXuLy": "2026-05-10T17:00:00"
}
Response:
{
"success": true,
"message": "Update document assignee successfully",
"data": {
"documentId": 1,
"nguoiXuLyId": 2,
"donViXuLyId": 1
}
}

7.3.6. Cập nhật trạng thái workflow của văn bản
PATCH /internal/documents/{id}/workflow-status
Service gọi:
workflow-service
Request:
{
"workflowStatus": "PROCESSING",
"currentStep": "Lãnh đạo phân công",
"processingId": 20
}
Response:
{
"success": true,
"message": "Update document workflow status successfully",
"data": {
"documentId": 1,
"workflowStatus": "PROCESSING",
"processingId": 20
}
}

7.3.7. Cập nhật trạng thái OCR của văn bản
PATCH /internal/documents/{id}/ocr-status
Service gọi:
ai-service
Dùng khi:
ai-service OCR xong và cần báo lại document-service rằng văn bản đã OCR
document-service chỉ lưu cờ trạng thái daOCR
AI-result và nội dung OCR chi tiết vẫn lưu trong DB của ai-service
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
Ghi chú:
Không lưu ocrText ở document-service nếu AI-result đã thuộc ai-service.
Nếu muốn tìm kiếm nhanh bằng OCR text ở document-service thì có thể bổ sung sau.

7.3.8. API thống kê văn bản cho report-service
GET /internal/documents/statistics
Query params:
fromDate=2026-04-01
toDate=2026-04-30
donViId=1
groupBy=status
Response:
{
"success": true,
"message": "Get internal document statistics successfully",
"data": {
"totalDocuments": 120,
"incomingDocuments": 70,
"outgoingDocuments": 50,
"items": [
{
"label": "Đang xử lý",
"value": 30
}
]
}
}

7.3.9. Lấy văn bản trễ hạn
GET /internal/documents/overdue
Query params:
donViId=1
nguoiXuLyId=2
page=0
size=10
Response:
{
"success": true,
"message": "Get internal overdue documents successfully",
"data": {
"content": [
{
"documentId": 1,
"soKyHieu": "123/CV-ABC",
"trichYeu": "Văn bản triển khai hệ thống",
"hanXuLy": "2026-04-25T17:00:00",
"soNgayTre": 5,
"trangThai": 1
}
],
"page": 0,
"size": 10,
"totalElements": 1
}
}
