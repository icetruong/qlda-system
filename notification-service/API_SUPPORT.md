PHẦN V. API BÁO CÁO & PHÂN TÍCH DỮ LIỆU
Phần này chỉ lấy chức năng 41 – Dashboard báo cáo và 42 – Phân tích dữ liệu.
1. Quy ước chung
   Base URL
   /api/reports
   Service phụ trách:
   report-service
2. Authentication
   Tất cả API đều yêu cầu header:
   Authorization: Bearer <access_token>
3. Định dạng dữ liệu
   Request
   Content-Type: application/json
   Response
   Tất cả API trả về JSON.
4. Cấu trúc Response chung
   Thành công
   {
   "success": true,
   "message": "Request processed successfully",
   "data": {}
   }
   Lỗi
   {
   "success": false,
   "message": "Invalid request",
   "errorCode": "INVALID_REQUEST"
   }
5. Error Codes
   Error Code
   Ý nghĩa
   REPORT_NOT_FOUND
   Không tìm thấy báo cáo
   INVALID_DATE_RANGE
   Khoảng thời gian không hợp lệ
   EXPORT_FAILED
   Xuất báo cáo thất bại
   FORBIDDEN
   Không có quyền xem báo cáo
   INTERNAL_SERVER_ERROR
   Lỗi hệ thống


6. API chi tiết
   6.1. Dashboard tổng quan
   GET /api/reports/dashboard
   Query Params
   fromDate=2026-04-01
   toDate=2026-04-30
   donViId=1
   Response
   {
   "success": true,
   "message": "Get dashboard successfully",
   "data": {
   "totalDocuments": 120,
   "incomingDocuments": 70,
   "outgoingDocuments": 50,
   "completedDocuments": 80,
   "processingDocuments": 30,
   "overdueDocuments": 10,
   "completionRate": 66.67,
   "overdueRate": 8.33
   }
   }
   6.2. Thống kê văn bản
   GET /api/reports/documents/statistics
   Query Params
   fromDate=2026-04-01
   toDate=2026-04-30
   donViId=1
   groupBy=status
   Quy ước groupBy:
   status
   type
   unit
   month
   Response
   {
   "success": true,
   "message": "Get document statistics successfully",
   "data": {
   "groupBy": "status",
   "items": [
   {
   "label": "Đang xử lý",
   "value": 30
   },
   {
   "label": "Đã hoàn thành",
   "value": 80
   },
   {
   "label": "Trễ hạn",
   "value": 10
   }
   ]
   }
   }
   6.3. Báo cáo tiến độ xử lý
   GET /api/reports/workflows/progress
   Query Params
   fromDate=2026-04-01
   toDate=2026-04-30
   donViId=1
   nguoiXuLyId=2
   Response
   {
   "success": true,
   "message": "Get workflow progress successfully",
   "data": {
   "totalTasks": 50,
   "completedTasks": 35,
   "processingTasks": 10,
   "overdueTasks": 5,
   "items": [
   {
   "documentId": 1,
   "soKyHieu": "123/CV-ABC",
   "trichYeu": "Văn bản triển khai hệ thống",
   "nguoiXuLyId": 2,
   "nguoiXuLy": "Nguyễn Văn A",
   "trangThaiXuLy": 1,
   "tyLeHoanThanh": 60,
   "hanXuLy": "2026-05-02T17:00:00"
   }
   ]
   }
   }
   6.4. Báo cáo văn bản trễ hạn
   GET /api/reports/overdue-documents
   Query Params
   donViId=1
   nguoiXuLyId=2
   page=0
   size=10
   Response
   {
   "success": true,
   "message": "Get overdue documents successfully",
   "data": {
   "content": [
   {
   "documentId": 1,
   "soKyHieu": "123/CV-ABC",
   "trichYeu": "Văn bản triển khai hệ thống",
   "nguoiXuLyId": 2,
   "nguoiXuLy": "Nguyễn Văn A",
   "hanXuLy": "2026-04-25T17:00:00",
   "soNgayTre": 5,
   "trangThaiXuLy": 1
   }
   ],
   "page": 0,
   "size": 10,
   "totalElements": 1,
   "totalPages": 1
   }
   }
   6.5. Xuất báo cáo
   GET /api/reports/export
   Query Params
   reportType=dashboard
   format=excel
   fromDate=2026-04-01
   toDate=2026-04-30
   donViId=1
   Quy ước reportType:
   dashboard
   document_statistics
   workflow_progress
   overdue_documents
   Quy ước format:
   excel
   pdf
   Response
   {
   "success": true,
   "message": "Export report successfully",
   "data": {
   "fileName": "bao-cao-dashboard-20260430.xlsx",
   "fileUrl": "/exports/bao-cao-dashboard-20260430.xlsx"
   }
   }
   6.6. Thông báo hệ thống
   Phần thông báo có thể để trong workflow-service hoặc notification-service. Nếu bạn đã có notification-service, workflow chỉ gọi sang notification-service. Nhưng để đủ API cho chức năng 26, có thể định nghĩa như sau:
   6.6.1. Tạo thông báo
   POST /api/notifications
   Request
   {
   "tieuDe": "Thông báo xử lý văn bản",
   "noiDung": "Bạn có văn bản mới cần xử lý",
   "nguoiNhanId": 2,
   "documentId": 1,
   "loaiThongBao": "NHAC_VIEC",
   "kenhGui": "SYSTEM"
   }
   Response
   {
   "success": true,
   "message": "Create notification successfully",
   "data": {
   "id": 1,
   "nguoiNhanId": 2,
   "documentId": 1,
   "daDoc": false
   }
   }
   6.6.2. Gửi thông báo
   POST /api/notifications/{id}/send
   Request
   {
   "kenhGui": ["SYSTEM", "EMAIL", "TEAMS"]
   }
   Response
   {
   "success": true,
   "message": "Send notification successfully",
   "data": {
   "notificationId": 1,
   "sentChannels": ["SYSTEM", "EMAIL", "TEAMS"]
   }
   }
   6.6.3. Xem danh sách thông báo của người dùng
   GET /api/notifications
   Query Params
   nguoiNhanId=2
   daDoc=false
   page=0
   size=10
   Response
   {
   "success": true,
   "message": "Get notifications successfully",
   "data": {
   "content": [
   {
   "id": 1,
   "tieuDe": "Thông báo xử lý văn bản",
   "noiDung": "Bạn có văn bản mới cần xử lý",
   "loaiThongBao": "NHAC_VIEC",
   "kenhGui": "SYSTEM",
   "daDoc": false,
   "ngayGui": "2026-04-30T10:00:00"
   }
   ],
   "page": 0,
   "size": 10,
   "totalElements": 1,
   "totalPages": 1
   }
   }
   6.6.4. Đánh dấu đã đọc
   PATCH /api/notifications/{id}/read
   Request
   {
   "nguoiNhanId": 2
   }
   Response
   {
   "success": true,
   "message": "Mark notification as read successfully",
   "data": {
   "notificationId": 1,
   "daDoc": true,
   "ngayDoc": "2026-04-30T10:30:00"
   }
   }
   6.6.5. Xóa thông báo
   DELETE /api/notifications/{id}
   Response
   {
   "success": true,
   "message": "Delete notification successfully",
   "data": {
   "notificationId": 1
   }
   }


Tóm tắt

GET /api/reports/dashboard
GET /api/reports/documents/statistics
GET /api/reports/workflows/progress
GET /api/reports/overdue-documents
GET /api/reports/export

POST   /api/notifications
POST   /api/notifications/{id}/send
GET    /api/notifications
PATCH  /api/notifications/{id}/read
DELETE /api/notifications/{id}
