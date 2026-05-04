Hệ thống sử dụng cơ chế xác thực riêng cho giao tiếp nội bộ:
- Sử dụng INTERNAL_API_KEY hoặc Service Token.
- Mỗi service khi gọi service khác phải gửi header:

Authorization: Bearer <INTERNAL_SERVICE_TOKEN>
X-Service-Name: <service-name>
Nguyên tắc:
- Chỉ các service hợp lệ mới được phép gọi Internal API.
- Không sử dụng token người dùng (JWT Azure AD) cho service-to-service.
- Token nội bộ có thể cấu hình qua environment variables.

workflow-service quản lý quy trình xử lý, phê duyệt, luân chuyển, SLA và tiến độ văn bản.
4.2.1. workflow-service gọi document-service
Mục đích:
Lấy thông tin văn bản
Cập nhật trạng thái văn bản
Cập nhật người đang xử lý
Cập nhật trạng thái workflow của văn bản

API 1: Lấy thông tin văn bản
GET /internal/documents/{id}
Trả về:
{
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
Dùng khi:
Khởi tạo workflow
Chuyển xử lý
Phê duyệt/từ chối
Kiểm tra văn bản có tồn tại không

API 2: Cập nhật trạng thái văn bản
PATCH /internal/documents/{id}/status
Trả về:
{
"documentId": 1,
"trangThai": 3
}
Dùng khi:
Văn bản đang xử lý
Văn bản đã trình duyệt
Văn bản bị từ chối
Văn bản hoàn thành

API 3: Cập nhật người đang xử lý
PATCH /internal/documents/{id}/assignee
Trả về:
{
"documentId": 1,
"nguoiXuLyId": 2,
"donViXuLyId": 1
}
Dùng khi:
Chuyển xử lý cho người mới
Phân công đơn vị xử lý

API 4: Cập nhật trạng thái workflow của văn bản
PATCH /internal/documents/{id}/workflow-status
Trả về:
{
"documentId": 1,
"workflowStatus": "PROCESSING",
"processingId": 20
}
Dùng khi:
Workflow chuyển bước
Workflow hoàn thành
Workflow bị từ chối

4.2.2. workflow-service gọi auth-service
Mục đích:
Kiểm tra người gửi, người nhận
Kiểm tra người phê duyệt
Kiểm tra đơn vị xử lý
Kiểm tra quyền duyệt/chuyển xử lý
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

4.2.3. workflow-service gọi notification-service
workflow-service không gọi trực tiếp notification-service bằng REST API. Khi có sự kiện trong workflow, service publish event vào Kafka.
Mục đích:
Gửi thông báo văn bản chờ xử lý
Gửi thông báo văn bản chờ phê duyệt
Gửi cảnh báo quá hạn
Gửi nhắc việc deadline
Gửi thông báo khi hoàn thành hoặc từ chối xử lý
Kafka topic:
notification-events
Event mẫu: chuyển xử lý
{
"eventId": "evt-003",
"eventType": "WORKFLOW_TRANSFERRED",
"sourceService": "workflow-service",
"nguoiNhanIds": [2],
"tieuDe": "Bạn có văn bản mới cần xử lý",
"noiDung": "Một văn bản vừa được chuyển đến bạn để xử lý",
"loaiThongBao": "NHAC_VIEC",
"kenhGui": ["SYSTEM", "EMAIL"],
"referenceType": "WORKFLOW",
"referenceId": 20,
"metadata": {
"documentId": 1,
"processingId": 20,
"nguoiGuiId": 1,
"nguoiNhanId": 2,
"hanXuLy": "2026-05-10T17:00:00"
},
"createdAt": "2026-04-30T10:00:00"
}
Event mẫu: trình phê duyệt
{
"eventId": "evt-004",
"eventType": "WORKFLOW_APPROVAL_REQUESTED",
"sourceService": "workflow-service",
"nguoiNhanIds": [4],
"tieuDe": "Bạn có văn bản cần phê duyệt",
"noiDung": "Một văn bản đang chờ bạn phê duyệt",
"loaiThongBao": "PHE_DUYET",
"kenhGui": ["SYSTEM", "EMAIL"],
"referenceType": "WORKFLOW",
"referenceId": 30,
"metadata": {
"documentId": 1,
"processingId": 30,
"nguoiTrinhId": 2,
"nguoiPheDuyetId": 4
},
"createdAt": "2026-04-30T10:00:00"
}
Event mẫu: cảnh báo SLA
{
"eventId": "evt-005",
"eventType": "WORKFLOW_SLA_VIOLATED",
"sourceService": "workflow-service",
"nguoiNhanIds": [2, 4],
"tieuDe": "Cảnh báo văn bản quá hạn xử lý",
"noiDung": "Có văn bản đã quá hạn xử lý theo SLA",
"loaiThongBao": "CANH_BAO_SLA",
"kenhGui": ["SYSTEM", "EMAIL"],
"referenceType": "WORKFLOW",
"referenceId": 20,
"metadata": {
"documentId": 1,
"processingId": 20,
"soGioTre": 5,
"hanXuLy": "2026-04-30T17:00:00"
},
"createdAt": "2026-04-30T10:00:00"
}
Kết quả xử lý:
workflow-service chỉ publish event vào Kafka.
notification-service consume event và xử lý gửi thông báo.
Workflow không phụ thuộc trực tiếp vào trạng thái sống/chết của notification-service.

Các service gọi đến: document-service, report-service.
7.4.1. Khởi tạo workflow cho văn bản
POST /internal/workflows/documents/{documentId}/start
Request:
{
 "workflowId": 1,
 "nguoiTaoId": 1,
 "donViChuTriId": 1,
 "documentType": "INCOMING",
 "hanXuLy": "2026-05-10T17:00:00"
}
Response:
{
 "success": true,
 "message": "Start document workflow successfully",
 "data": {
   "documentId": 1,
   "workflowId": 1,
   "processingId": 10,
   "currentStep": "Văn thư tiếp nhận",
   "trangThaiXuLy": 1
 }
}

7.4.2. Chuyển xử lý văn bản
POST /internal/workflows/documents/{documentId}/transfer
Request:
{
 "nguoiGuiId": 1,
 "nguoiNhanId": 2,
 "donViXuLyId": 1,
 "buocQuyTrinhId": 2,
 "yKienXuLy": "Chuyển chuyên viên xử lý",
 "hanXuLy": "2026-05-10T17:00:00"
}
Response:
{
 "success": true,
 "message": "Transfer document workflow successfully",
 "data": {
   "processingId": 20,
   "documentId": 1,
   "nguoiNhanId": 2,
   "trangThaiXuLy": 1
 }
}

7.4.3. Trình phê duyệt văn bản
POST /internal/workflows/documents/{documentId}/submit-approval
Request:
{
 "nguoiTrinhId": 2,
 "nguoiPheDuyetId": 4,
 "noiDungTrinh": "Trình lãnh đạo phê duyệt văn bản"
}
Response:
{
 "success": true,
 "message": "Submit document approval workflow successfully",
 "data": {
   "documentId": 1,
   "processingId": 30,
   "nguoiPheDuyetId": 4,
   "trangThaiXuLy": 1
 }
}

7.4.4. Lấy trạng thái workflow của văn bản
GET /internal/workflows/documents/{documentId}/status
Response:
{
 "success": true,
 "message": "Get internal workflow status successfully",
 "data": {
   "documentId": 1,
   "currentStep": "Lãnh đạo phê duyệt",
   "trangThaiXuLy": 1,
   "tyLeHoanThanh": 60,
   "hanXuLy": "2026-05-02T17:00:00",
   "isOverdue": false
 }
}

7.4.5. Lấy timeline workflow của văn bản
GET /internal/workflows/documents/{documentId}/timeline
Response:
{
 "success": true,
 "message": "Get internal workflow timeline successfully",
 "data": [
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
}

7.4.6. Thống kê workflow cho report-service
GET /internal/workflows/statistics
Query params:
fromDate=2026-04-01
toDate=2026-04-30
donViId=1
Response:
{
 "success": true,
 "message": "Get internal workflow statistics successfully",
 "data": {
   "totalTasks": 50,
   "completedTasks": 35,
   "processingTasks": 10,
   "overdueTasks": 5
 }
}

7.4.7. Lấy tiến độ workflow cho report-service
GET /internal/workflows/progress
Query params:
fromDate=2026-04-01
toDate=2026-04-30
donViId=1
nguoiXuLyId=2
Response:
{
 "success": true,
 "message": "Get internal workflow progress successfully",
 "data": {
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
}

7.4.8. Lấy vi phạm SLA
GET /internal/workflows/sla/violations
Query params:
fromDate=2026-04-01
toDate=2026-04-30
donViId=1
Response:
{
 "success": true,
 "message": "Get internal SLA violations successfully",
 "data": [
   {
     "processingId": 20,
     "documentId": 1,
     "nguoiNhanId": 2,
     "hanXuLy": "2026-04-30T17:00:00",
     "ngayHoanThanh": null,
     "soGioTre": 5
   }
 ]
}
