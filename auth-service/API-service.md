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

4.6. auth-service
auth-service là service nền tảng của hệ thống, quản lý người dùng, đơn vị, vai trò, chức năng và phân quyền.
Nguyên tắc thiết kế:
Không phụ thuộc vào các service nghiệp vụ khác
Không gọi ngược document-service, workflow-service, ai-service, report-service
Cung cấp dữ liệu định danh và phân quyền cho toàn hệ thống
Internal API cung cấp:
GET  /internal/auth/users/{id}
POST /internal/auth/users/validate
GET  /internal/auth/units/{id}
POST /internal/auth/units/validate
GET  /internal/auth/users/{id}/roles
POST /internal/auth/permissions/check
Service gọi đến:
document-service → auth-service
workflow-service → auth-service
report-service → auth-service
notification-service → auth-service

Quy ước chung cho Internal API
Base path:
/internal/*
Header bắt buộc:
Authorization: Bearer <INTERNAL_SERVICE_TOKEN>
X-Service-Name: <service-name>
Content-Type: application/json
Response dùng chung:
{
"success": true,
"message": "Request processed successfully",
"data": {}
}
Internal API chỉ dành cho backend service gọi nhau, không expose ra frontend.

7.2. Internal API của auth-service
Các service gọi đến: document-service, workflow-service, report-service, có thể notification-service.
7.2.1. Lấy thông tin user
GET /internal/auth/users/{id}
Dùng khi:
document-service kiểm tra người ký, người nhận, người tạo
workflow-service kiểm tra người xử lý, người duyệt
report-service lấy tên người dùng để hiển thị báo cáo
Response:
{
"success": true,
"message": "Get internal user successfully",
"data": {
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
}

7.2.2. Kiểm tra nhiều user tồn tại
POST /internal/auth/users/validate
Request:
{
"userIds": [1, 2, 3]
}
Response:
{
"success": true,
"message": "Validate users successfully",
"data": {
"valid": true,
"invalidUserIds": []
}
}
Dùng cho các API có nguoiNhanIds, nguoiPheDuyetId, nguoiKyId.

7.2.3. Lấy thông tin đơn vị
GET /internal/auth/units/{id}
Response:
{
"success": true,
"message": "Get internal unit successfully",
"data": {
"id": 1,
"maDonVi": "HC",
"tenDonVi": "Phòng Hành chính",
"donViChaId": null,
"suDung": true
}
}

7.2.4. Kiểm tra nhiều đơn vị tồn tại
POST /internal/auth/units/validate
Request:
{
"unitIds": [1, 2, 3]
}
Response:
{
"success": true,
"message": "Validate units successfully",
"data": {
"valid": true,
"invalidUnitIds": []
}
}

7.2.5. Lấy vai trò của user
GET /internal/auth/users/{id}/roles
Response:
{
"success": true,
"message": "Get user roles successfully",
"data": {
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
}

7.2.6. Kiểm tra quyền
POST /internal/auth/permissions/check
Request:
{
"userId": 1,
"maChucNang": "DOCUMENT_INCOMING",
"permission": "IsEdit"
}
Response:
{
"success": true,
"message": "Check permission successfully",
"data": {
"allowed": true,
"userId": 1,
"maChucNang": "DOCUMENT_INCOMING",
"permission": "IsEdit"
}
}

