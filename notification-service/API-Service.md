Hệ thống sử dụng cơ chế xác thực riêng cho giao tiếp nội bộ:
- Sử dụng INTERNAL_API_KEY hoặc Service Token.
- Mỗi service khi gọi service khác phải gửi header:

Authorization: Bearer <INTERNAL_SERVICE_TOKEN>
X-Service-Name: <service-name>
Nguyên tắc:
- Chỉ các service hợp lệ mới được phép gọi Internal API.
- Không sử dụng token người dùng (JWT Azure AD) cho service-to-service.
- Token nội bộ có thể cấu hình qua environment variables.

report-service tổng hợp báo cáo, dashboard và phân tích dữ liệu.
4.5.1. report-service gọi document-service
Mục đích:
Lấy thống kê văn bản
Lấy văn bản trễ hạn
Lấy số lượng văn bản đến/đi
Lấy số lượng văn bản theo trạng thái

API 1: Thống kê văn bản
GET /internal/documents/statistics
Trả về:
{
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

API 2: Lấy văn bản trễ hạn
GET /internal/documents/overdue
Trả về:
{
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

4.5.2. report-service gọi workflow-service
Mục đích:
Thống kê tiến độ xử lý
Lấy số lượng task hoàn thành / đang xử lý / quá hạn
Lấy danh sách vi phạm SLA

API 1: Thống kê workflow
GET /internal/workflows/statistics
Trả về:
{
"totalTasks": 50,
"completedTasks": 35,
"processingTasks": 10,
"overdueTasks": 5
}

API 2: Lấy tiến độ workflow
GET /internal/workflows/progress
Trả về:
{
"totalTasks": 50,
"completedTasks": 35,
"processingTasks": 10,
"items": [
{
"documentId": 1,
"processingId": 20,
"nguoiXuLyId": 2,
"trangThaiXuLy": 1,
"tyLeHoanThanh": 60,
"hanXuLy": "2026-05-02T17:00:00"
}
]
}

API 3: Lấy vi phạm SLA
GET /internal/workflows/sla/violations
Trả về:
[
{
"processingId": 20,
"documentId": 1,
"nguoiNhanId": 2,
"hanXuLy": "2026-04-30T17:00:00",
"ngayHoanThanh": null,
"soGioTre": 5
}
]

4.5.3. report-service gọi auth-service
Mục đích:
Lấy tên người dùng từ userId
Lấy tên đơn vị từ donViId
Hiển thị dữ liệu báo cáo dễ đọc hơn
Internal API sử dụng:
GET /internal/auth/users/{id}
GET /internal/auth/units/{id}
Trả về user:
{
"id": 1,
"username": "nva",
"hoTen": "Nguyễn Văn A",
"email": "nva@company.com",
"donViId": 1,
"tenDonVi": "Phòng Hành chính",
"maNhomQuyen": "CHUYEN_VIEN",
"trangThai": 1
}
Trả về đơn vị:
{
"id": 1,
"maDonVi": "HC",
"tenDonVi": "Phòng Hành chính",
"donViChaId": null,
"suDung": true
}

7.6. Kafka Notification Event
notification-service không cung cấp Internal API để các service khác gọi trực tiếp.
Thay vào đó, các service nghiệp vụ publish event vào Kafka, sau đó notification-service consume event để tạo và gửi thông báo.
Các service publish event:
document-service
workflow-service
Service consume event:
notification-service
Kafka topic:
notification-events
Kafka dead-letter topic:
notification-events-dlq

7.6.1. Event gửi một thông báo
Topic:
notification-events
Event:
{
"eventId": "evt-001",
"eventType": "DOCUMENT_TRANSFERRED",
"sourceService": "workflow-service",
"nguoiNhanIds": [2],
"tieuDe": "Thông báo xử lý văn bản",
"noiDung": "Bạn có văn bản mới cần xử lý",
"loaiThongBao": "NHAC_VIEC",
"kenhGui": ["SYSTEM", "EMAIL"],
"referenceType": "DOCUMENT",
"referenceId": 1,
"metadata": {
"documentId": 1,
"processingId": 20
},
"createdAt": "2026-04-30T10:00:00"
}
notification-service xử lý:
Consume event
→ kiểm tra eventId để tránh xử lý trùng
→ tạo thông báo trong DB
→ gửi qua SYSTEM / EMAIL / TEAMS
→ lưu trạng thái gửi

7.6.2. Event gửi nhiều thông báo
Topic:
notification-events
Event:
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
notification-service xử lý:
Consume event
→ tạo thông báo cho từng nguoiNhanId
→ gửi theo danh sách kenhGui
→ lưu trạng thái từng thông báo
