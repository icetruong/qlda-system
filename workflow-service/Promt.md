Bạn đang code trong workflow-service của hệ thống QLVB.

Hãy đọc trước:
1. AGENTS.md nếu có
2. .agents/skills nếu có
3. Skill tdd-workflow nếu có
4. API_WORKFLOW.md
5. pom.xml / build.gradle
6. application.yml / application.properties
7. Cấu trúc package hiện tại

BẮT BUỘC làm theo TDD theo quy trình everything-claude-code:
- Không code implementation trước test
- Viết test trước
- Chạy test để thấy test fail đúng lý do
- Sau đó mới implement code
- Chạy lại test cho pass
- Refactor nếu cần
- Không báo hoàn thành nếu chưa chạy test/compile

==================================================
MỤC TIÊU
==================================================

Implement FULL API trong docs/API_WORKFLOW.md cho workflow-service theo TDD.

Base URL:
- /api/workflows

Tất cả API yêu cầu Bearer Token.

Database hiện có:
- QuyTrinh
- BuocQuyTrinh
- XuLyVanBan

Không tự ý thêm bảng/cột mới.

==================================================
QUY TRÌNH TDD BẮT BUỘC
==================================================

Làm theo vòng lặp Red - Green - Refactor cho từng module:

1. Workflow + Steps
2. Approval
3. Transfer / Processing
4. Status / Timeline
5. SLA
6. Reminder
7. Delegation skeleton
8. Notification skeleton
9. Security

Với mỗi module:
A. Viết unit test/service test trước
B. Viết controller test bằng MockMvc hoặc WebTestClient
C. Chạy test và xác nhận fail
D. Implement code tối thiểu để pass
E. Chạy lại test
F. Refactor
G. Commit/checkpoint nội bộ nếu workflow hỗ trợ

Không được implement hàng loạt khi chưa có test.

==================================================
TEST YÊU CẦU
==================================================

Dùng test stack hiện có trong project. Nếu chưa có thì thêm phù hợp:
- spring-boot-starter-test
- spring-security-test
- H2 hoặc Testcontainers PostgreSQL nếu project đã dùng
- Mockito / JUnit 5
- MockMvc cho controller test

Tối thiểu phải có:

1. Unit test Service:
- WorkflowServiceTest
- WorkflowStepServiceTest
- ProcessingServiceTest
- ApprovalServiceTest
- SlaServiceTest
- ReminderServiceTest

2. Controller test:
- WorkflowControllerTest
- ProcessingControllerTest
- ApprovalControllerTest
- SlaControllerTest

3. Repository test nếu phù hợp:
- QuyTrinhRepositoryTest
- BuocQuyTrinhRepositoryTest
- XuLyVanBanRepositoryTest

4. Security test:
- Request không có token phải 401
- Request có JWT mock hợp lệ được xử lý
- /api/workflows/** không được permitAll

Nếu OAuth2 Resource Server gây khó trong test:
- Dùng @WithMockUser hoặc mock JwtAuthenticationToken bằng spring-security-test
- Không tắt security toàn bộ test một cách bừa bãi

==================================================
ENTITY / REPOSITORY
==================================================

Nếu chưa có Entity/Repository thì tạo theo schema:

CREATE TABLE QuyTrinh (
ID SERIAL PRIMARY KEY,
MaQuyTrinh VARCHAR(50) NOT NULL,
TenQuyTrinh VARCHAR(255) NOT NULL,
LoaiVanBanID INT,
MoTa VARCHAR(1000),
SoBuoc INT,
SuDung BOOLEAN DEFAULT TRUE,
CONSTRAINT uq_quytrinh_maquytrinh UNIQUE (MaQuyTrinh)
);

CREATE TABLE BuocQuyTrinh (
ID BIGSERIAL PRIMARY KEY,
QuyTrinhID INT NOT NULL,
TenBuoc VARCHAR(255) NOT NULL,
ThuTuBuoc INT NOT NULL,
VaiTroXuLy VARCHAR(100),
ThoiGianXuLy INT,
BatBuocPheDuyet BOOLEAN DEFAULT FALSE,
GhiChu VARCHAR(500),
CONSTRAINT fk_buocquytrinh_quytrinh FOREIGN KEY (QuyTrinhID) REFERENCES QuyTrinh(ID)
);

CREATE TABLE XuLyVanBan (
ID BIGSERIAL PRIMARY KEY,
VanBanID BIGINT NOT NULL,
BuocQuyTrinhID BIGINT,
NguoiGuiID BIGINT,
NguoiNhanID BIGINT,
DonViXuLyID INT,
HanhDongXuLy VARCHAR(100),
YKienXuLy VARCHAR(1000),
NgayNhan TIMESTAMP,
HanXuLy TIMESTAMP,
NgayHoanThanh TIMESTAMP,
TyLeHoanThanh INT,
TrangThaiXuLy INT,
TepKetQua VARCHAR(500),
CONSTRAINT fk_xuly_buoc FOREIGN KEY (BuocQuyTrinhID) REFERENCES BuocQuyTrinh(ID),
CONSTRAINT chk_xuly_tylehoanthanh CHECK (
TyLeHoanThanh IS NULL OR (TyLeHoanThanh >= 0 AND TyLeHoanThanh <= 100)
)
);

Mapping:
- SERIAL -> Integer
- BIGSERIAL -> Long
- TIMESTAMP -> LocalDateTime
- BOOLEAN -> Boolean
- Giữ nguyên tên bảng/cột bằng @Table/@Column
- Không tạo JPA relation sang auth-service/document-service
- LoaiVanBanID, VanBanID, NguoiGuiID, NguoiNhanID, DonViXuLyID là tham chiếu mềm

Quan hệ nội bộ:
- BuocQuyTrinh ManyToOne QuyTrinh
- XuLyVanBan ManyToOne BuocQuyTrinh
- FetchType.LAZY
- Không CascadeType.REMOVE

Repository cần có:
- QuyTrinhRepository extends JpaRepository<QuyTrinh, Integer>, JpaSpecificationExecutor<QuyTrinh>
- BuocQuyTrinhRepository extends JpaRepository<BuocQuyTrinh, Long>
- XuLyVanBanRepository extends JpaRepository<XuLyVanBan, Long>, JpaSpecificationExecutor<XuLyVanBan>

==================================================
API CẦN TEST VÀ IMPLEMENT
==================================================

Implement đúng docs/API_WORKFLOW.md:

Workflow:
- POST /api/workflows
- PUT /api/workflows/{id}
- GET /api/workflows
- GET /api/workflows/{id}
- DELETE /api/workflows/{id}

Steps:
- POST /api/workflows/{workflowId}/steps
- PUT /api/workflows/{workflowId}/steps/{stepId}
- DELETE /api/workflows/{workflowId}/steps/{stepId}
- PATCH /api/workflows/{workflowId}/steps/{stepId}/sla

Approvals:
- GET /api/workflows/approvals/pending
- POST /api/workflows/approvals/{processingId}/comment
- POST /api/workflows/approvals/{processingId}/approve
- POST /api/workflows/approvals/{processingId}/reject

Processing:
- GET /api/workflows/documents/{documentId}/status
- GET /api/workflows/documents/{documentId}/timeline
- GET /api/workflows/processings/{processingId}
- POST /api/workflows/documents/{documentId}/transfer
- POST /api/workflows/processings/{processingId}/receive
- POST /api/workflows/processings/{processingId}/complete

SLA:
- GET /api/workflows/sla
- GET /api/workflows/sla/violations

Reminders:
- POST /api/workflows/reminders/check-deadlines
- POST /api/workflows/reminders/send

Delegations:
- POST /api/workflows/delegations
- GET /api/workflows/delegations
- DELETE /api/workflows/delegations/{id}

Notifications:
- POST /api/workflows/notifications
- POST /api/workflows/notifications/{id}/send
- GET /api/workflows/notifications
- PATCH /api/workflows/notifications/{id}/read
- DELETE /api/workflows/notifications/{id}

==================================================
BUSINESS RULES CẦN TEST
==================================================

Workflow:
- Tạo workflow thành công
- Không cho trùng maQuyTrinh
- Delete workflow là soft delete: SuDung = false
- List filter keyword, loaiVanBanId, suDung
- Detail trả kèm steps

Steps:
- Tạo step tăng QuyTrinh.SoBuoc
- Xóa step cập nhật lại SoBuoc
- Không tạo step nếu workflow không tồn tại
- Không update step sai workflowId
- SLA cập nhật BuocQuyTrinh.ThoiGianXuLy

Processing:
- Transfer tạo XuLyVanBan với TrangThaiXuLy = 1, TyLeHoanThanh = 0
- Receive giữ TrangThaiXuLy = 1
- Complete set TrangThaiXuLy = 2, TyLeHoanThanh = 100 hoặc giá trị request
- Không cho TyLeHoanThanh ngoài 0..100

Approval:
- Comment cập nhật YKienXuLy
- Approve set TrangThaiXuLy = 2, NgayHoanThanh = now
- Reject set TrangThaiXuLy = 3, YKienXuLy = lyDoTuChoi

Status/Timeline:
- Status lấy processing mới nhất theo VanBanID
- isOverdue đúng khi HanXuLy < now và chưa hoàn thành
- Timeline order theo NgayNhan hoặc ID

SLA:
- Violations gồm các processing quá hạn
- soGioTre tính đúng
- Filter donViId nếu có

Reminder:
- totalNearDeadline tính đúng theo beforeHours
- totalOverdue tính đúng theo checkDate
- send reminders trả totalSent = số processingIds

Skeleton/mock:
- Delegation hiện chưa có bảng, test response đúng format
- Notification hiện chưa có bảng, test response đúng format
- Không tự tạo bảng mới

==================================================
SECURITY PRODUCTION
==================================================

workflow-service là resource server, không tạo JWT.

Bắt buộc:
- Dùng spring-boot-starter-oauth2-resource-server
- Verify JWT bằng PUBLIC KEY
- RS256
- Không dùng HS256
- Không hardcode secret/key
- Không permitAll /api/workflows/**
- Bật @EnableMethodSecurity nếu dùng @PreAuthorize

Config mẫu:

spring:
security:
oauth2:
resourceserver:
jwt:
public-key-location: classpath:public.pem

JWT claim:
- userId
- username
- roles

Map roles:
- roles: ["ADMIN"] -> ROLE_ADMIN

Tạo SecurityUtils nếu cần:
- getCurrentUserId()
- getCurrentUsername()
- getCurrentRoles()

==================================================
COMMON CODE
==================================================

Tạo nếu chưa có:
- ApiResponse<T>
- PageResponse<T>
- ErrorCode
- AppException
- GlobalExceptionHandler

Không trả Entity trực tiếp.
DTO riêng cho request/response.

==================================================
CÁCH LÀM BẮT BUỘC
==================================================

Bước 1:
Đọc docs/API_WORKFLOW.md và cấu trúc project.

Bước 2:
Liệt kê test plan trước:
- Test class nào
- Test case nào
- API nào test trước

Bước 3:
Viết test trước cho module Workflow + Steps.

Bước 4:
Chạy test để thấy fail.

Bước 5:
Implement code cho Workflow + Steps pass.

Bước 6:
Lặp lại với từng module:
- Approval
- Processing
- SLA
- Reminder
- Delegation skeleton
- Notification skeleton
- Security

Bước 7:
Chạy toàn bộ test:
- ./mvnw test
  hoặc:
- mvn test
  hoặc:
- ./gradlew test

Bước 8:
Nếu chưa có test infrastructure hoặc test quá nặng, ít nhất phải chạy:
- ./mvnw compile
  hoặc:
- mvn compile

==================================================
BÁO CÁO CUỐI
==================================================

Báo cáo bằng tiếng Việt:

1. Đã đọc file nào
2. Test plan đã tạo
3. Test class đã viết
4. Test nào fail ban đầu
5. Test nào đã pass sau implementation
6. File đã tạo
7. File đã sửa
8. API implement thật
9. API skeleton/mock/TODO
10. Security đã cấu hình ra sao
11. Lệnh test/build đã chạy
12. Kết quả test/build
13. Lỗi còn tồn tại nếu có

==================================================
NGUYÊN TẮC CẤM
==================================================

- Không code implementation trước test
- Không bỏ qua test
- Không báo hoàn thành khi chưa chạy test/compile
- Không thêm bảng/cột mới
- Không sửa tên bảng/cột
- Không tạo JWT trong workflow-service
- Không dùng HS256
- Không hardcode key/secret
- Không permitAll toàn bộ API
- Không trả Entity trực tiếp
- Không code tất cả trong một file
- Không tạo JPA relationship sang service khác