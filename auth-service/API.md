PHẦN I. API QUẢN TRỊ, BẢO MẬT VÀ HẠ TẦNG CLOUD
1. Quy ước chung
   Base URL
   /api/auth
   Lý do chọn:
   /api/auth
   vì phần I chủ yếu thuộc auth-service, gồm người dùng, phân quyền, đăng nhập, audit log và cấu hình bảo mật.
2. Authentication
   Tất cả API, trừ API đăng nhập, đều yêu cầu header:
   Authorization: Bearer <access_token>
   Access token được lấy từ API đăng nhập.
   Ví dụ:
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   Các API không cần token:
   POST /api/auth/login/azure
3. Định dạng dữ liệu
   Request
   Content-Type: application/json
   Response
   Tất cả API trả về JSON.
4. Cấu trúc Response
   Response thành công
   {
   "success": true,
   "message": "Request processed successfully",
   "data": {}
   }
   Response lỗi
   {
   "success": false,
   "message": "Invalid request",
   "errorCode": "INVALID_REQUEST"
   }
5. Error Codes
   Error Code
   Ý nghĩa
   INVALID_REQUEST
   Dữ liệu gửi lên không hợp lệ
   UNAUTHORIZED
   Chưa đăng nhập hoặc token không hợp lệ
   FORBIDDEN
   Không có quyền thực hiện chức năng
   USER_NOT_FOUND
   Không tìm thấy người dùng
   ROLE_NOT_FOUND
   Không tìm thấy nhóm quyền
   PERMISSION_NOT_FOUND
   Không tìm thấy quyền
   DUPLICATE_USERNAME
   Tên đăng nhập đã tồn tại
   DUPLICATE_EMAIL
   Email đã tồn tại
   INVALID_LOGIN
   Sai thông tin đăng nhập
   AZURE_AUTH_FAILED
   Xác thực Azure AD thất bại
   AUDIT_LOG_NOT_FOUND
   Không tìm thấy nhật ký hệ thống
   INTERNAL_SERVER_ERROR
   Lỗi hệ thống


6. API chi tiết
   6.1. Quản lý người dùng, phân quyền
   Chức năng này phục vụ quản lý tài khoản người dùng, phân quyền, gán nhóm quyền và khóa/mở khóa tài khoản theo yêu cầu chức năng số 1 trong danh sách 43 chức năng.
   6.1.1. Lấy danh sách người dùng
   GET /api/auth/users
   Query Params
   page=0
   size=10
   keyword=nguyen
   donViId=1
   trangThai=1
   Response
   {
   "success": true,
   "message": "Get users successfully",
   "data": {
   "content": [
   {
   "id": 1,
   "username": "nva",
   "hoTen": "Nguyễn Văn A",
   "email": "nva@company.com",
   "dienThoai": "0901234567",
   "donViId": 1,
   "tenDonVi": "Phòng Hành chính",
   "chucVu": "Chuyên viên",
   "nhomQuyenId": 2,
   "tenNhomQuyen": "Chuyên viên",
   "trangThai": 1
   }
   ],
   "page": 0,
   "size": 10,
   "totalElements": 1,
   "totalPages": 1
   }
   }
   6.1.2. Lấy chi tiết người dùng
   GET /api/auth/users/{id}
   Response
   {
   "success": true,
   "message": "Get user detail successfully",
   "data": {
   "id": 1,
   "username": "nva",
   "hoTen": "Nguyễn Văn A",
   "email": "nva@company.com",
   "dienThoai": "0901234567",
   "donViId": 1,
   "chucVu": "Chuyên viên",
   "nhomQuyenId": 2,
   "azureAdId": "azure-user-id",
   "trangThai": 1
   }
   }
   6.1.3. Thêm mới người dùng
   POST /api/auth/users
   Request
   {
   "username": "nva",
   "hoTen": "Nguyễn Văn A",
   "email": "nva@company.com",
   "dienThoai": "0901234567",
   "donViId": 1,
   "chucVu": "Chuyên viên",
   "nhomQuyenId": 2,
   "azureAdId": "azure-user-id"
   }
   Response
   {
   "success": true,
   "message": "Create user successfully",
   "data": {
   "id": 1
   }
   }
   6.1.4. Cập nhật thông tin người dùng
   PUT /api/auth/users/{id}
   Request
   {
   "hoTen": "Nguyễn Văn A",
   "email": "nva@company.com",
   "dienThoai": "0901234567",
   "donViId": 1,
   "chucVu": "Trưởng phòng",
   "nhomQuyenId": 3,
   "trangThai": 1
   }
   Response
   {
   "success": true,
   "message": "Update user successfully",
   "data": {
   "id": 1
   }
   }
   6.1.5. Khóa / mở khóa tài khoản
   PATCH /api/auth/users/{id}/status
   Request
   {
   "trangThai": 0
   }
   Quy ước:
   0 = Khóa
   1 = Hoạt động
   Response
   {
   "success": true,
   "message": "Update user status successfully",
   "data": {
   "id": 1,
   "trangThai": 0
   }
   }
   6.1.6. Gán nhóm quyền cho người dùng
   PATCH /api/auth/users/{id}/role
   Request
   {
   "nhomQuyenId": 2
   }
   Response
   {
   "success": true,
   "message": "Assign role successfully",
   "data": {
   "userId": 1,
   "nhomQuyenId": 2
   }
   }
   6.1.7. Xóa người dùng
   DELETE /api/auth/users/{id}
   Nên xử lý xóa mềm:
   trangThai = -1
   Response
   {
   "success": true,
   "message": "Delete user successfully",
   "data": {
   "id": 1
   }
   }
   6.2. Xác thực Azure AD
   Chức năng này phục vụ đăng nhập bằng Office 365, xác thực qua Azure AD, đồng bộ thông tin người dùng và đăng xuất theo yêu cầu chức năng số 2.
   6.2.1. Đăng nhập thường
   Request
   {
   "username": "admin",
   "password": "123456"
   }
   Response
   {
   "success": true,
   "message": "Login successfully",
   "data": {
   "accessToken": "jwt-access-token",
   "refreshToken": "jwt-refresh-token",
   "tokenType": "Bearer",
   "expiresIn": 3600,
   "user": {
   "id": 1,
   "username": "admin",
   "hoTen": "Quản trị hệ thống",
   "email": "admin@company.com",
   "roles": ["ADMIN"]
   }
   }
   }

6.2.2. Đăng nhập bằng Azure AD
POST /api/auth/login/azure
Request
{
"authorizationCode": "code-from-microsoft",
"redirectUri": "http://localhost:3000/callback"
}
Response
{
"success": true,
"message": "Azure login successfully",
"data": {
"accessToken": "jwt-access-token",
"refreshToken": "jwt-refresh-token",
"tokenType": "Bearer",
"expiresIn": 3600,
"user": {
"id": 1,
"username": "nva",
"hoTen": "Nguyễn Văn A",
"email": "nva@company.com",
"roles": ["CHUYEN_VIEN"]
}
}
}

6.2.3. Lấy thông tin người dùng hiện tại
GET /api/auth/me
Response
{
"success": true,
"message": "Get current user successfully",
"data": {
"id": 1,
"username": "nva",
"hoTen": "Nguyễn Văn A",
"email": "nva@company.com",
"donViId": 1,
"nhomQuyenId": 2,
"roles": ["CHUYEN_VIEN"]
}
}

6.2.4. Refresh token
POST /api/auth/refresh-token
Request
{
"refreshToken": "jwt-refresh-token"
}
Response
{
"success": true,
"message": "Refresh token successfully",
"data": {
"accessToken": "new-jwt-access-token",
"refreshToken": "new-jwt-refresh-token",
"tokenType": "Bearer",
"expiresIn": 3600
}
}

6.2.5. Đồng bộ người dùng Azure AD
POST /api/auth/users/sync-azure
Request
{
"azureAdIds": [
"azure-user-id-1",
"azure-user-id-2"
]
}
Response
{
"success": true,
"message": "Sync Azure users successfully",
"data": {
"totalSynced": 2
}
}

6.2.6. Đăng xuất
POST /api/auth/logout
Request
{
"refreshToken": "jwt-refresh-token"
}
Response
{
"success": true,
"message": "Logout successfully",
"data": {}
}
6.3. Audit log hệ thống
Chức năng này phục vụ xem log, tìm kiếm log, xem chi tiết hành động và xuất log theo yêu cầu chức năng số 3.

6.3.1. Lấy danh sách audit log
GET /api/auth/audit-logs
Query Params
page=0
size=20
userId=1
hanhDong=LOGIN
doiTuong=VanBan
fromDate=2026-01-01
toDate=2026-01-31
Response
{
"success": true,
"message": "Get audit logs successfully",
"data": {
"content": [
{
"id": 1,
"nguoiDungId": 1,
"hoTen": "Nguyễn Văn A",
"hanhDong": "LOGIN",
"doiTuong": "NguoiDung",
"doiTuongId": 1,
"diaChiIp": "127.0.0.1",
"thoiGianThucHien": "2026-04-30T10:00:00",
"trangThai": 1
}
],
"page": 0,
"size": 20,
"totalElements": 1,
"totalPages": 1
}
}
6.3.2. Xem chi tiết audit log
GET /api/auth/audit-logs/{id}
Response
{
"success": true,
"message": "Get audit log detail successfully",
"data": {
"id": 1,
"nguoiDungId": 1,
"hanhDong": "LOGIN",
"doiTuong": "NguoiDung",
"doiTuongId": 1,
"noiDungChiTiet": "Người dùng đăng nhập hệ thống",
"diaChiIp": "127.0.0.1",
"thoiGianThucHien": "2026-04-30T10:00:00",
"trangThai": 1
}
}
6.3.3. Xuất audit log
GET /api/auth/audit-logs/export
Query Params
format=excel
fromDate=2026-01-01
toDate=2026-01-31
Response
{
"success": true,
"message": "Export audit logs successfully",
"data": {
"fileUrl": "/files/exports/audit-log-20260430.xlsx"
}
}
6.4. Sao lưu và phục hồi dữ liệu
Chức năng này phục vụ tạo backup, xem danh sách backup, restore và xóa backup theo yêu cầu chức năng số 4.
Vì bạn không muốn thêm bảng BackupHistory, phần này có thể làm dạng API mô phỏng hoặc gọi script backup thật.
6.4.1. Tạo backup
POST /api/auth/backups
Request
{
"backupType": "FULL",
"description": "Backup dữ liệu trước khi cập nhật hệ thống"
}
Response
{
"success": true,
"message": "Create backup successfully",
"data": {
"fileName": "backup_20260430.sql",
"fileUrl": "/backups/backup_20260430.sql"
}
}

6.4.2. Xem danh sách backup
GET /api/auth/backups
Response
{
"success": true,
"message": "Get backup list successfully",
"data": [
{
"fileName": "backup_20260430.sql",
"fileSize": 2048000,
"createdAt": "2026-04-30T10:00:00"
}
]
}

6.4.3. Restore dữ liệu
POST /api/auth/backups/restore
Request
{
"fileName": "backup_20260430.sql",
"confirmRestore": true
}
Response
{
"success": true,
"message": "Restore database successfully",
"data": {
"fileName": "backup_20260430.sql"
}
}

6.4.4. Xóa backup
DELETE /api/auth/backups/{fileName}
Response
{
"success": true,
"message": "Delete backup successfully",
"data": {
"fileName": "backup_20260430.sql"
}
}

6.5. Cấu hình hệ thống Office 365
Chức năng này phục vụ kết nối hệ thống với Office 365, cấu hình SharePoint, OneDrive, Teams và kiểm tra trạng thái kết nối theo yêu cầu chức năng số 5.
Vì không thêm bảng Office365Config, nên thông tin cấu hình nên để trong:
application.yml
.env
Docker environment variables

6.5.1. Xem trạng thái cấu hình Office 365
GET /api/auth/office365/config/status
Response
{
"success": true,
"message": "Get Office 365 config status successfully",
"data": {
"tenantIdConfigured": true,
"clientIdConfigured": true,
"clientSecretConfigured": true,
"sharePointConfigured": true,
"teamsConfigured": true,
"outlookConfigured": true
}
}

6.5.2. Kiểm tra kết nối Office 365
GET /api/auth/office365/connection/check
Response
{
"success": true,
"message": "Check Office 365 connection successfully",
"data": {
"azureAd": true,
"sharePoint": true,
"oneDrive": true,
"teams": true,
"outlook": true
}
}

6.6. Thiết lập chính sách bảo mật
Chức năng này phục vụ thiết lập quyền truy cập, cấu hình chính sách bảo mật, quản lý IP và giới hạn truy cập hệ thống theo yêu cầu chức năng số 6.
Vì bạn không muốn thêm bảng, phần này có thể đọc cấu hình từ application.yml.

6.6.1. Xem chính sách bảo mật
GET /api/auth/security-policies
Response
{
"success": true,
"message": "Get security policies successfully",
"data": {
"sessionTimeoutMinutes": 30,
"maxLoginAttempts": 5,
"requireAuthentication": true,
"enableCors": true,
"enableIpWhitelist": false
}
}

6.6.2. Kiểm tra quyền truy cập chức năng
POST /api/auth/security-policies/check-permission
Request
{
"userId": 1,
"maChucNang": "DOCUMENT_APPROVE",
"permission": "APPROVE"
}
Response
{
"success": true,
"message": "Check permission successfully",
"data": {
"allowed": true
}
}

6.7. Quản lý cấu hình hệ thống
Chức năng này phục vụ quản lý cấu hình hệ thống theo yêu cầu chức năng số 7.
Vì không thêm bảng SystemConfig, phần này nên làm dạng API đọc cấu hình hệ thống hiện tại.

6.7.1. Xem cấu hình hệ thống
GET /api/auth/system-configs
Response
{
"success": true,
"message": "Get system configs successfully",
"data": {
"appName": "QLVB System",
"environment": "dev",
"maxUploadFileSize": "20MB",
"jwtExpiration": 3600,
"backupEnabled": true
}
}

6.7.2. Kiểm tra trạng thái hệ thống
GET /api/auth/system/health
Response
{
"success": true,
"message": "System is running",
"data": {
"service": "auth-service",
"status": "UP",
"database": "UP",
"timestamp": "2026-04-30T10:00:00"
}
}

Tóm tắt API

POST   /api/auth/login/azure
POST   /api/auth/logout
POST   /api/auth/refresh-token
GET    /api/auth/me

GET    /api/auth/users
GET    /api/auth/users/{id}
POST   /api/auth/users
PUT    /api/auth/users/{id}
PATCH  /api/auth/users/{id}/status
PATCH  /api/auth/users/{id}/role
DELETE /api/auth/users/{id}

POST   /api/auth/users/sync-azure

GET    /api/auth/audit-logs
GET    /api/auth/audit-logs/{id}
GET    /api/auth/audit-logs/export

POST   /api/auth/backups
GET    /api/auth/backups
POST   /api/auth/backups/restore
DELETE /api/auth/backups/{fileName}

GET    /api/auth/office365/config/status
GET    /api/auth/office365/connection/check

GET    /api/auth/security-policies
POST   /api/auth/security-policies/check-permission

GET    /api/auth/system-configs
GET    /api/auth/system/health
