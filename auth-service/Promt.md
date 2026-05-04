Bạn đang làm việc trong auth-service của hệ thống QLVB.

Hãy đọc trước:
1. AGENTS.md nếu có
2. .agents/skills nếu có
3. API.md
4. API-service.md
5. pom.xml hoặc build.gradle
6. application.yml / application.properties
7. Cấu trúc package hiện tại

Yêu cầu làm theo TDD:
- Viết test trước
- Chạy test để thấy test fail đúng lý do
- Sau đó mới implement code
- Chạy lại test cho pass
- Refactor nếu cần
- Không báo hoàn thành nếu chưa chạy test hoặc compile

Mục tiêu:
Sửa lại auth-service theo API.md mới và bổ sung Internal API theo API-service.md.

Auth-service phụ trách:
- Quản lý người dùng
- Quản lý đơn vị
- Quản lý nhóm quyền
- Quản lý chức năng
- Quản lý phân quyền
- Đăng nhập Azure AD
- Refresh token
- Logout
- Current user
- Cấu hình hệ thống
- Office365 config status
- Security policies
- Backup API dạng skeleton nếu chưa có hạ tầng thật
- Audit log API dạng skeleton nếu chưa có bảng log

Nguyên tắc kiến trúc:
- auth-service không gọi ngược document-service
- auth-service không gọi ngược workflow-service
- auth-service không gọi ngược report-service
- auth-service chỉ cung cấp dữ liệu định danh và phân quyền cho các service khác
- Không thêm bảng hoặc cột mới nếu chưa được yêu cầu
- Không trả Entity trực tiếp ra API
- Dùng DTO riêng cho request/response

Public API:
Implement theo API.md với base path /api/auth.

Các API chính:
- POST /api/auth/login/azure
- POST /api/auth/logout
- POST /api/auth/refresh-token
- GET /api/auth/me

- GET /api/auth/users
- GET /api/auth/users/{id}
- POST /api/auth/users
- PUT /api/auth/users/{id}
- PATCH /api/auth/users/{id}/status
- PATCH /api/auth/users/{id}/role
- DELETE /api/auth/users/{id}

- POST /api/auth/users/sync-azure

- GET /api/auth/audit-logs
- GET /api/auth/audit-logs/{id}
- GET /api/auth/audit-logs/export

- POST /api/auth/backups
- GET /api/auth/backups
- POST /api/auth/backups/restore
- DELETE /api/auth/backups/{fileName}

- GET /api/auth/office365/config/status
- GET /api/auth/office365/connection/check

- GET /api/auth/security-policies
- POST /api/auth/security-policies/check-permission

- GET /api/auth/system-configs
- GET /api/auth/system/health

Internal API:
Implement theo API-service.md với base path /internal/auth.

Các endpoint:
- GET /internal/auth/users/{id}
- POST /internal/auth/users/validate
- GET /internal/auth/units/{id}
- POST /internal/auth/units/validate
- GET /internal/auth/users/{id}/roles
- POST /internal/auth/permissions/check

Internal API dùng cho backend service gọi nhau.
Yêu cầu:
- Các request nội bộ phải có header định danh service.
- Validate header theo cấu hình trong application.yml/env.
- Danh sách service được phép gọi đọc từ config.
- Không hardcode giá trị bảo mật trong code.
- Nếu header thiếu hoặc không hợp lệ thì trả lỗi phù hợp.
- Viết filter riêng cho /internal/**.

Config gợi ý:
internal:
auth:
service-token: ${INTERNAL_SERVICE_TOKEN:change-me-in-dev}
allowed-services:
- document-service
- workflow-service
- report-service
- notification-service
- support-service

Security:
- /api/auth/login/azure được public
- Các API /api/auth/** còn lại cần authenticated
- /internal/** dùng filter riêng theo cấu hình internal.auth
- Không tắt security toàn cục
- Không hardcode secret/key
- Không dùng cấu hình bảo mật cố định trong code

Database:
Dùng các bảng hiện có:
- DonVi
- NhomQuyen
- NguoiDung
- ChucNang
- PhanQuyen

Nếu Entity/Repository chưa có thì tạo.
Nếu đã có thì kiểm tra mapping đúng.

Mapping:
- SERIAL -> Integer
- BIGSERIAL -> Long
- TIMESTAMP -> LocalDateTime
- BOOLEAN -> Boolean
- Giữ nguyên tên bảng/cột bằng @Table và @Column
- Không đổi sang snake_case
- Không thêm field ngoài schema

Kiến trúc code:
- controller
- controller.internal
- service
- service.impl
- repository
- entity
- dto.request
- dto.response
- dto.internal.request
- dto.internal.response
- security
- config
- exception
- common
- mapper
- specification

Tạo hoặc dùng lại:
- ApiResponse
- PageResponse
- ErrorCode
- AppException
- GlobalExceptionHandler

Test bắt buộc viết trước:
1. InternalAuthSecurityTest
- Thiếu header nội bộ thì bị từ chối
- Header sai thì bị từ chối
- Service name không nằm trong allowed list thì bị từ chối
- Header hợp lệ thì cho qua

2. InternalAuthControllerTest
- GET /internal/auth/users/{id}
- POST /internal/auth/users/validate
- GET /internal/auth/units/{id}
- POST /internal/auth/units/validate
- GET /internal/auth/users/{id}/roles
- POST /internal/auth/permissions/check

3. InternalAuthServiceTest
- get user success
- user not found
- validate users trả invalidUserIds
- get unit success
- unit not found
- validate units trả invalidUnitIds
- get roles and permissions
- check permission true/false

4. UserServiceTest
- create user success
- duplicate username
- duplicate email
- update user
- soft delete user: trangThai = -1
- assign role

5. UserControllerTest
- list users
- detail user
- create user
- update user
- update status
- assign role
- delete user

6. AuthServiceTest
- Azure login skeleton/mock success
- refresh token
- logout
- current user

7. ConfigServiceTest
- office365 config status
- security policies
- system configs
- health

TDD flow:
1. Đọc API.md và API-service.md
2. Viết test plan
3. Viết test Internal API trước
4. Chạy test để thấy fail
5. Implement Internal API
6. Chạy test pass
7. Viết test Public API
8. Chạy test fail
9. Implement Public API
10. Chạy test pass
11. Chạy toàn bộ test

Lệnh cuối:
- ./mvnw test
  hoặc
- mvn test

Nếu test chưa chạy được thì ít nhất phải chạy:
- ./mvnw compile
  hoặc
- mvn compile

Báo cáo cuối bằng tiếng Việt:
- File đã đọc
- Test plan
- Test class đã viết
- Test nào fail ban đầu
- Test nào pass sau implement
- File đã tạo
- File đã sửa
- API đã implement
- API skeleton/TODO
- Security đã cấu hình
- Lệnh test/build đã chạy
- Kết quả
- Lỗi còn tồn tại nếu có