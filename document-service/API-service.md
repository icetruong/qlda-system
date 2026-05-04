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

4.1.3. document-service gọi ai-service
Mục đích:
OCR file văn bản
Tóm tắt văn bản
Phân loại văn bản
Trích xuất metadata
Gợi ý hướng xử lý

API 1: OCR nội bộ
POST /internal/ai/ocr
Trả về:
{
"documentId": 1,
"ocrText": "Nội dung văn bản sau OCR...",
"confidence": 92.5,
"modelUsed": "ocr-model"
}
Sau khi nhận kết quả, document-service lưu OCR bằng:
PATCH /internal/documents/{id}/ocr-status

API 2: Tóm tắt văn bản
POST /internal/ai/summarize
Trả về:
{
"documentId": 1,
"summaryType": "SHORT",
"summary": "Văn bản đề cập đến việc triển khai hệ thống xử lý văn bản điện tử.",
"confidence": 91.5,
"modelUsed": "gpt-4.1"
}
Sau khi nhận kết quả, document-service lưu vào AI result.

API 3: Phân loại văn bản
POST /internal/ai/classify
Trả về:
{
"documentId": 1,
"category": "CONG_VAN",
"categoryName": "Công văn",
"confidence": 94.2,
"reason": "Nội dung phù hợp với cấu trúc công văn."
}
Dùng khi:
Tự động phân loại văn bản đến
Gợi ý loại văn bản khi upload file

API 4: Trích xuất metadata
POST /internal/ai/metadata/extract
Trả về:
{
"documentId": 1,
"metadata": {
"soKyHieu": "123/CV-ABC",
"ngayVanBan": "2026-04-30",
"donViBanHanh": "Sở Thông tin và Truyền thông",
"nguoiKy": "Nguyễn Văn A"
},
"confidence": 90.6,
"modelUsed": "gpt-4.1"
}
Dùng khi:
Tự điền thông tin văn bản
Trích xuất số ký hiệu, ngày văn bản, đơn vị ban hành, người ký

API 5: Gợi ý xử lý
POST /internal/ai/suggestions
Trả về:
{
"documentId": 1,
"suggestions": [
{
"action": "CHUYEN_XU_LY",
"description": "Chuyển văn bản cho Phòng Hành chính xử lý trong vòng 24 giờ.",
"priority": "HIGH"
}
],
"confidence": 87.3
}
Dùng khi:
Gợi ý chuyển xử lý
Gợi ý độ ưu tiên
Gợi ý hướng xử lý cho lãnh đạo/chuyên viên

4.1.4. document-service gọi notification-service
Mục đích:
Gửi thông báo khi có văn bản mới
Gửi thông báo khi chuyển xử lý
Gửi thông báo khi trình ký
Gửi thông báo khi phát hành văn bản

API 1: Gửi một thông báo
POST /internal/notifications/send
Trả về:
{
"notificationId": 1,
"nguoiNhanId": 2,
"sentChannels": ["SYSTEM", "EMAIL"]
}

API 2: Gửi nhiều thông báo
POST /internal/notifications/bulk-send
Trả về:
{
"totalReceivers": 3,
"totalSent": 3,
"sentChannels": ["SYSTEM", "EMAIL"]
}


7.3. Internal API của document-service
Các service gọi đến: workflow-service, ai-service, report-service.
7.3.1. Lấy thông tin văn bản
GET /internal/documents/{id}
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
Dùng cho workflow-service khi xử lý theo documentId.

7.3.2. Lấy nội dung văn bản cho AI
GET /internal/documents/{id}/content
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

7.3.3. Lấy file đính kèm của văn bản
GET /internal/documents/{id}/attachments
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

7.3.7. Lưu kết quả AI vào văn bản
POST /internal/documents/{id}/ai-results
Request:
{
"loaiXuLyAI": "SUMMARY",
"noiDungDauVao": "Nội dung văn bản...",
"ketQuaTraVe": "Tóm tắt văn bản...",
"doTinCay": 91.5,
"modelSuDung": "gpt-4.1"
}
Response:
{
"success": true,
"message": "Save document AI result successfully",
"data": {
"documentId": 1,
"aiResultId": 10,
"loaiXuLyAI": "SUMMARY"
}
}

7.3.8. Cập nhật trạng thái OCR
PATCH /internal/documents/{id}/ocr-status
Request:
{
"daOCR": true,
"ocrText": "Nội dung sau OCR...",
"confidence": 92.5
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

7.3.9. API thống kê văn bản cho report-service
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

7.3.10. Lấy văn bản trễ hạn
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
