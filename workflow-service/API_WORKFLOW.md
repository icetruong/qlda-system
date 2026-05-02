PHẦN III. API WORKFLOW & XỬ LÝ
Phần này tương ứng chức năng 19 → 26 trong danh sách 43 chức năng của bạn.
1. Quy ước chung
   Base URL
   /api/workflows
   Service phụ trách:
   workflow-service
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
   WORKFLOW_NOT_FOUND
   Không tìm thấy quy trình
   WORKFLOW_STEP_NOT_FOUND
   Không tìm thấy bước xử lý
   PROCESSING_NOT_FOUND
   Không tìm thấy bản ghi xử lý văn bản
   DOCUMENT_NOT_FOUND
   Không tìm thấy văn bản
   USER_NOT_FOUND
   Không tìm thấy người dùng
   INVALID_WORKFLOW_STATUS
   Trạng thái quy trình không hợp lệ
   APPROVAL_FAILED
   Phê duyệt thất bại
   TRANSFER_FAILED
   Luân chuyển văn bản thất bại
   SLA_NOT_FOUND
   Không tìm thấy SLA
   FORBIDDEN
   Không có quyền thao tác
   INTERNAL_SERVER_ERROR
   Lỗi hệ thống


6. API chi tiết
   6.1. Thiết kế Workflow
   6.1.1. Tạo workflow
   POST /api/workflows
   Request
   {
   "maQuyTrinh": "QT_VB_DEN",
   "tenQuyTrinh": "Quy trình xử lý văn bản đến",
   "loaiVanBanId": 1,
   "moTa": "Quy trình tiếp nhận, phân công và xử lý văn bản đến",
   "suDung": true
   }
   Response
   {
   "success": true,
   "message": "Create workflow successfully",
   "data": {
   "id": 1,
   "maQuyTrinh": "QT_VB_DEN",
   "tenQuyTrinh": "Quy trình xử lý văn bản đến",
   "suDung": true
   }
   }
   6.1.2. Cập nhật workflow
   PUT /api/workflows/{id}
   Request
   {
   "tenQuyTrinh": "Quy trình xử lý văn bản đến cập nhật",
   "loaiVanBanId": 1,
   "moTa": "Cập nhật mô tả quy trình",
   "suDung": true
   }
   Response
   {
   "success": true,
   "message": "Update workflow successfully",
   "data": {
   "id": 1
   }
   }
   6.1.3. Xem danh sách workflow
   GET /api/workflows
   Query Params
   page=0
   size=10
   keyword=văn bản
   loaiVanBanId=1
   suDung=true
   Response
   {
   "success": true,
   "message": "Get workflows successfully",
   "data": {
   "content": [
   {
   "id": 1,
   "maQuyTrinh": "QT_VB_DEN",
   "tenQuyTrinh": "Quy trình xử lý văn bản đến",
   "loaiVanBanId": 1,
   "soBuoc": 3,
   "suDung": true
   }
   ],
   "page": 0,
   "size": 10,
   "totalElements": 1,
   "totalPages": 1
   }
   }
   6.1.4. Xem chi tiết workflow
   GET /api/workflows/{id}
   Response
   {
   "success": true,
   "message": "Get workflow detail successfully",
   "data": {
   "id": 1,
   "maQuyTrinh": "QT_VB_DEN",
   "tenQuyTrinh": "Quy trình xử lý văn bản đến",
   "loaiVanBanId": 1,
   "moTa": "Quy trình tiếp nhận, phân công và xử lý văn bản đến",
   "soBuoc": 3,
   "suDung": true,
   "steps": [
   {
   "id": 1,
   "tenBuoc": "Văn thư tiếp nhận",
   "thuTuBuoc": 1,
   "vaiTroXuLy": "VAN_THU"
   }
   ]
   }
   }
   6.1.5. Xóa workflow
   DELETE /api/workflows/{id}
   Response
   {
   "success": true,
   "message": "Delete workflow successfully",
   "data": {
   "id": 1
   }
   }
   6.1.6. Tạo bước xử lý trong workflow
   POST /api/workflows/{workflowId}/steps
   Request
   {
   "tenBuoc": "Lãnh đạo phân công",
   "thuTuBuoc": 2,
   "vaiTroXuLy": "LANH_DAO",
   "thoiGianXuLy": 24,
   "batBuocPheDuyet": true,
   "ghiChu": "Bước lãnh đạo xem và phân công xử lý"
   }
   Response
   {
   "success": true,
   "message": "Create workflow step successfully",
   "data": {
   "id": 2,
   "workflowId": 1,
   "tenBuoc": "Lãnh đạo phân công",
   "thuTuBuoc": 2
   }
   }
   6.1.7. Cập nhật bước xử lý
   PUT /api/workflows/{workflowId}/steps/{stepId}
   Request
   {
   "tenBuoc": "Lãnh đạo phê duyệt",
   "thuTuBuoc": 2,
   "vaiTroXuLy": "LANH_DAO",
   "thoiGianXuLy": 24,
   "batBuocPheDuyet": true,
   "ghiChu": "Cập nhật bước xử lý"
   }
   Response
   {
   "success": true,
   "message": "Update workflow step successfully",
   "data": {
   "workflowId": 1,
   "stepId": 2
   }
   }
   6.1.8. Xóa bước xử lý
   DELETE /api/workflows/{workflowId}/steps/{stepId}
   Response
   {
   "success": true,
   "message": "Delete workflow step successfully",
   "data": {
   "workflowId": 1,
   "stepId": 2
   }
   }
   6.2. Phê duyệt văn bản
   6.2.1. Xem danh sách văn bản chờ duyệt
   GET /api/workflows/approvals/pending
   Query Params
   page=0
   size=10
   nguoiDuyetId=4
   keyword=kế hoạch
   fromDate=2026-04-01
   toDate=2026-04-30
   Response
   {
   "success": true,
   "message": "Get pending approvals successfully",
   "data": {
   "content": [
   {
   "processingId": 10,
   "documentId": 1,
   "soKyHieu": "123/CV-ABC",
   "trichYeu": "Văn bản về việc triển khai hệ thống",
   "nguoiGuiId": 2,
   "nguoiGuiTen": "Nguyễn Văn A",
   "ngayNhan": "2026-04-30T09:00:00",
   "hanXuLy": "2026-05-02T17:00:00",
   "trangThaiXuLy": 1
   }
   ],
   "page": 0,
   "size": 10,
   "totalElements": 1,
   "totalPages": 1
   }
   }
   6.2.2. Góp ý văn bản
   POST /api/workflows/approvals/{processingId}/comment
   Request
   {
   "noiDungGopY": "Cần bổ sung thêm căn cứ pháp lý trước khi phê duyệt"
   }
   Response
   {
   "success": true,
   "message": "Comment approval successfully",
   "data": {
   "processingId": 10,
   "noiDungGopY": "Cần bổ sung thêm căn cứ pháp lý trước khi phê duyệt"
   }
   }
   6.2.3. Phê duyệt văn bản
   POST /api/workflows/approvals/{processingId}/approve
   Request
   {
   "yKienXuLy": "Đồng ý phê duyệt văn bản",
   "chuyenBuocTiepTheo": true
   }
   Response
   {
   "success": true,
   "message": "Approve document successfully",
   "data": {
   "processingId": 10,
   "documentId": 1,
   "trangThaiXuLy": 2,
   "ngayHoanThanh": "2026-04-30T11:00:00"
   }
   }
   6.2.4. Từ chối văn bản
   POST /api/workflows/approvals/{processingId}/reject
   Request
   {
   "lyDoTuChoi": "Thông tin chưa đầy đủ, cần chỉnh sửa lại"
   }
   Response
   {
   "success": true,
   "message": "Reject document successfully",
   "data": {
   "processingId": 10,
   "documentId": 1,
   "trangThaiXuLy": 3,
   "lyDoTuChoi": "Thông tin chưa đầy đủ, cần chỉnh sửa lại"
   }
   }
   6.3. Ủy quyền xử lý
   6.3.1. Tạo ủy quyền xử lý
   POST /api/workflows/delegations
   Request
   {
   "nguoiUyQuyenId": 4,
   "nguoiDuocUyQuyenId": 5,
   "tuNgay": "2026-05-01",
   "denNgay": "2026-05-10",
   "phamViUyQuyen": "APPROVE_DOCUMENT",
   "ghiChu": "Ủy quyền trong thời gian đi công tác"
   }
   Response
   {
   "success": true,
   "message": "Create delegation successfully",
   "data": {
   "id": 1,
   "nguoiUyQuyenId": 4,
   "nguoiDuocUyQuyenId": 5,
   "tuNgay": "2026-05-01",
   "denNgay": "2026-05-10"
   }
   }
   6.3.2. Xem danh sách ủy quyền
   GET /api/workflows/delegations
   Query Params
   page=0
   size=10
   nguoiUyQuyenId=4
   nguoiDuocUyQuyenId=5
   active=true
   Response
   {
   "success": true,
   "message": "Get delegations successfully",
   "data": {
   "content": [
   {
   "id": 1,
   "nguoiUyQuyenId": 4,
   "nguoiDuocUyQuyenId": 5,
   "tuNgay": "2026-05-01",
   "denNgay": "2026-05-10",
   "phamViUyQuyen": "APPROVE_DOCUMENT",
   "active": true
   }
   ],
   "page": 0,
   "size": 10,
   "totalElements": 1,
   "totalPages": 1
   }
   }
   6.3.3. Hủy ủy quyền
   DELETE /api/workflows/delegations/{id}
   Response
   {
   "success": true,
   "message": "Cancel delegation successfully",
   "data": {
   "id": 1
   }
   }
   6.4. Theo dõi tiến độ
   6.4.1. Xem trạng thái xử lý văn bản
   GET /api/workflows/documents/{documentId}/status
   Response
   {
   "success": true,
   "message": "Get document workflow status successfully",
   "data": {
   "documentId": 1,
   "currentStep": "Lãnh đạo phê duyệt",
   "trangThaiXuLy": 1,
   "tyLeHoanThanh": 60,
   "hanXuLy": "2026-05-02T17:00:00",
   "isOverdue": false
   }
   }
   6.4.2. Xem timeline xử lý
   GET /api/workflows/documents/{documentId}/timeline
   Response
   {
   "success": true,
   "message": "Get document timeline successfully",
   "data": [
   {
   "processingId": 1,
   "tenBuoc": "Văn thư tiếp nhận",
   "nguoiXuLy": "Nguyễn Văn A",
   "hanhDongXuLy": "CREATE",
   "yKienXuLy": "Tiếp nhận văn bản",
   "ngayNhan": "2026-04-30T08:00:00",
   "ngayHoanThanh": "2026-04-30T09:00:00",
   "trangThaiXuLy": 2
   },
   {
   "processingId": 2,
   "tenBuoc": "Lãnh đạo phân công",
   "nguoiXuLy": "Trần Văn B",
   "hanhDongXuLy": "TRANSFER",
   "yKienXuLy": "Chuyển chuyên viên xử lý",
   "ngayNhan": "2026-04-30T09:00:00",
   "ngayHoanThanh": null,
   "trangThaiXuLy": 1
   }
   ]
   }
   6.4.3. Xem chi tiết tiến độ của một bước xử lý
   GET /api/workflows/processings/{processingId}
   Response
   {
   "success": true,
   "message": "Get processing detail successfully",
   "data": {
   "id": 10,
   "documentId": 1,
   "buocQuyTrinhId": 2,
   "nguoiGuiId": 1,
   "nguoiNhanId": 2,
   "donViXuLyId": 1,
   "hanhDongXuLy": "TRANSFER",
   "yKienXuLy": "Chuyển xử lý",
   "ngayNhan": "2026-04-30T09:00:00",
   "hanXuLy": "2026-05-02T17:00:00",
   "ngayHoanThanh": null,
   "tyLeHoanThanh": 60,
   "trangThaiXuLy": 1
   }
   }
   6.5. Nhắc việc tự động
   6.5.1. Kiểm tra deadline
   POST /api/workflows/reminders/check-deadlines
   Request
   {
   "checkDate": "2026-04-30T10:00:00",
   "beforeHours": 24
   }
   Response
   {
   "success": true,
   "message": "Check deadlines successfully",
   "data": {
   "totalNearDeadline": 5,
   "totalOverdue": 2
   }
   }
   6.5.2. Gửi thông báo nhắc việc
   POST /api/workflows/reminders/send
   Request
   {
   "processingIds": [10, 11, 12],
   "kenhGui": ["SYSTEM", "EMAIL"],
   "noiDung": "Bạn có văn bản sắp đến hạn xử lý"
   }
   Response
   {
   "success": true,
   "message": "Send reminders successfully",
   "data": {
   "totalSent": 3,
   "kenhGui": ["SYSTEM", "EMAIL"]
   }
   }
   6.6. Luân chuyển văn bản
   6.6.1. Chuyển văn bản
   POST /api/workflows/documents/{documentId}/transfer
   Request
   {
   "nguoiGuiId": 1,
   "nguoiNhanId": 2,
   "donViXuLyId": 1,
   "buocQuyTrinhId": 2,
   "hanhDongXuLy": "TRANSFER",
   "yKienXuLy": "Chuyển văn bản cho chuyên viên xử lý",
   "hanXuLy": "2026-05-02T17:00:00"
   }
   Response
   {
   "success": true,
   "message": "Transfer document successfully",
   "data": {
   "processingId": 20,
   "documentId": 1,
   "nguoiNhanId": 2,
   "trangThaiXuLy": 1
   }
   }
   6.6.2. Nhận văn bản
   POST /api/workflows/processings/{processingId}/receive
   Request
   {
   "nguoiNhanId": 2,
   "ghiChu": "Đã nhận văn bản để xử lý"
   }
   Response
   {
   "success": true,
   "message": "Receive document successfully",
   "data": {
   "processingId": 20,
   "receivedAt": "2026-04-30T10:00:00",
   "trangThaiXuLy": 1
   }
   }
   6.6.3. Xác nhận hoàn thành xử lý
   POST /api/workflows/processings/{processingId}/complete
   Request
   {
   "yKienXuLy": "Đã xử lý xong văn bản",
   "tepKetQua": "/files/results/ket-qua-xu-ly.docx",
   "tyLeHoanThanh": 100
   }
   Response
   {
   "success": true,
   "message": "Complete processing successfully",
   "data": {
   "processingId": 20,
   "ngayHoanThanh": "2026-04-30T15:00:00",
   "tyLeHoanThanh": 100,
   "trangThaiXuLy": 2
   }
   }
   6.7. Thiết lập SLA xử lý văn bản
   Với CSDL hiện tại chưa có bảng SLA riêng. Có thể dùng trường ThoiGianXuLy trong bảng BuocQuyTrinh để lưu SLA theo từng bước xử lý.
   6.7.1. Thiết lập thời gian xử lý cho bước workflow
   PATCH /api/workflows/{workflowId}/steps/{stepId}/sla
   Request
   {
   "thoiGianXuLy": 24,
   "donViThoiGian": "HOUR",
   "ghiChu": "Thời gian xử lý chuẩn là 24 giờ"
   }
   Response
   {
   "success": true,
   "message": "Update workflow step SLA successfully",
   "data": {
   "workflowId": 1,
   "stepId": 2,
   "thoiGianXuLy": 24,
   "donViThoiGian": "HOUR"
   }
   }
   6.7.2. Xem danh sách SLA
   GET /api/workflows/sla
   Query Params
   workflowId=1
   Response
   {
   "success": true,
   "message": "Get SLA list successfully",
   "data": [
   {
   "workflowId": 1,
   "stepId": 1,
   "tenBuoc": "Văn thư tiếp nhận",
   "thoiGianXuLy": 8,
   "donViThoiGian": "HOUR"
   },
   {
   "workflowId": 1,
   "stepId": 2,
   "tenBuoc": "Lãnh đạo phân công",
   "thoiGianXuLy": 24,
   "donViThoiGian": "HOUR"
   }
   ]
   }
   6.7.3. Kiểm tra vi phạm SLA
   GET /api/workflows/sla/violations
   Query Params
   fromDate=2026-04-01
   toDate=2026-04-30
   donViId=1
   Response
   {
   "success": true,
   "message": "Get SLA violations successfully",
   "data": [
   {
   "processingId": 20,
   "documentId": 1,
   "trichYeu": "Văn bản về việc triển khai hệ thống",
   "nguoiNhanId": 2,
   "hanXuLy": "2026-04-30T17:00:00",
   "ngayHoanThanh": null,
   "soGioTre": 5
   }
   ]
   }
   6.8. Thông báo hệ thống
   Phần thông báo có thể để trong workflow-service hoặc notification-service. Nếu bạn đã có notification-service, workflow chỉ gọi sang notification-service. Nhưng để đủ API cho chức năng 26, có thể định nghĩa như sau:
   6.8.1. Tạo thông báo
   POST /api/workflows/notifications
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
   6.8.2. Gửi thông báo
   POST /api/workflows/notifications/{id}/send
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
   6.8.3. Xem danh sách thông báo của người dùng
   GET /api/workflows/notifications
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
   6.8.4. Đánh dấu đã đọc
   PATCH /api/workflows/notifications/{id}/read
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
   6.8.5. Xóa thông báo
   DELETE /api/workflows/notifications/{id}
   Response
   {
   "success": true,
   "message": "Delete notification successfully",
   "data": {
   "notificationId": 1
   }
   }

Tóm tắt

POST   /api/workflows
PUT    /api/workflows/{id}
GET    /api/workflows
GET    /api/workflows/{id}
DELETE /api/workflows/{id}

POST   /api/workflows/{workflowId}/steps
PUT    /api/workflows/{workflowId}/steps/{stepId}
DELETE /api/workflows/{workflowId}/steps/{stepId}

GET    /api/workflows/approvals/pending
POST   /api/workflows/approvals/{processingId}/comment
POST   /api/workflows/approvals/{processingId}/approve
POST   /api/workflows/approvals/{processingId}/reject

POST   /api/workflows/delegations
GET    /api/workflows/delegations
DELETE /api/workflows/delegations/{id}

GET    /api/workflows/documents/{documentId}/status
GET    /api/workflows/documents/{documentId}/timeline
GET    /api/workflows/processings/{processingId}

POST   /api/workflows/reminders/check-deadlines
POST   /api/workflows/reminders/send

POST   /api/workflows/documents/{documentId}/transfer
POST   /api/workflows/processings/{processingId}/receive
POST   /api/workflows/processings/{processingId}/complete

PATCH  /api/workflows/{workflowId}/steps/{stepId}/sla
GET    /api/workflows/sla
GET    /api/workflows/sla/violations

POST   /api/workflows/notifications
POST   /api/workflows/notifications/{id}/send
GET    /api/workflows/notifications
PATCH  /api/workflows/notifications/{id}/read
DELETE /api/workflows/notifications/{id}
