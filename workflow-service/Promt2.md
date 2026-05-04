Bạn đang code trong workflow-service của hệ thống QLVB.

Hãy đọc trước:
1. AGENTS.md nếu có
2. .agents/skills nếu có
3. Skill tdd-workflow nếu có
4. API-service.md
5. pom.xml / build.gradle
6. application.yml / application.properties
7. Cấu trúc package hiện tại

Mục tiêu:
Chỉ bổ sung phần service-to-service theo API-service.md cho workflow-service.

Không sửa lại public API hiện có nếu không cần.
Không làm lại toàn bộ /api/workflows/**.
Chỉ thêm:
1. Internal API của workflow-service: /internal/workflows/**
2. Client để workflow-service gọi service khác:
    - document-service
    - auth-service
3. Kafka publisher để gửi notification event
4. Cơ chế xác thực nội bộ cho /internal/**
5. Test theo TDD

==================================================
BẮT BUỘC: TDD
==================================================

Làm theo Red → Green → Refactor:

- Viết test trước
- Chạy test để thấy fail đúng lý do
- Sau đó mới implement code
- Chạy lại test cho pass
- Refactor nếu cần
- Không báo hoàn thành nếu chưa chạy test hoặc compile

==================================================
INTERNAL API WORKFLOW-SERVICE
==================================================

Base path:
- /internal/workflows

Implement các endpoint trong API-service.md:

1. POST /internal/workflows/documents/{documentId}/start
- Khởi tạo workflow cho văn bản
- Tạo XuLyVanBan đầu tiên
- TrangThaiXuLy = 1
- Gọi document-service để kiểm tra văn bản tồn tại nếu cần
- Gọi document-service cập nhật workflow status nếu phù hợp

2. POST /internal/workflows/documents/{documentId}/transfer
- Tạo bản ghi XuLyVanBan mới
- TrangThaiXuLy = 1
- Validate người gửi/người nhận/đơn vị qua auth-service
- Cập nhật assignee sang document-service
- Publish Kafka event WORKFLOW_TRANSFERRED

3. POST /internal/workflows/documents/{documentId}/submit-approval
- Tạo bản ghi XuLyVanBan cho người phê duyệt
- TrangThaiXuLy = 1
- Validate người trình/người phê duyệt qua auth-service
- Cập nhật workflow status sang document-service
- Publish Kafka event WORKFLOW_APPROVAL_REQUESTED

4. GET /internal/workflows/documents/{documentId}/status
- Lấy bản ghi xử lý mới nhất theo documentId
- Trả currentStep, trangThaiXuLy, tyLeHoanThanh, hanXuLy, isOverdue

5. GET /internal/workflows/documents/{documentId}/timeline
- Lấy danh sách XuLyVanBan theo documentId
- Sắp xếp theo NgayNhan hoặc ID
- Kèm tenBuoc nếu có BuocQuyTrinh

6. GET /internal/workflows/statistics
- Thống kê totalTasks, completedTasks, processingTasks, overdueTasks

7. GET /internal/workflows/progress
- Thống kê tiến độ workflow
- Filter fromDate, toDate, donViId, nguoiXuLyId

8. GET /internal/workflows/sla/violations
- Lấy danh sách quá hạn SLA
- Tính soGioTre

Yêu cầu:
- Dùng Entity/Repository hiện có của workflow-service
- Không thêm bảng/cột mới
- Không đổi schema
- Không trả Entity trực tiếp
- Response dùng ApiResponse chung nếu project đã có

==================================================
WORKFLOW-SERVICE GỌI DOCUMENT-SERVICE
==================================================

Tạo DocumentServiceClient:

- getDocumentById(Long id)
- updateDocumentStatus(Long id, request)
- updateDocumentAssignee(Long id, request)
- updateDocumentWorkflowStatus(Long id, request)

Dùng khi:
- Start workflow: kiểm tra document tồn tại
- Transfer: cập nhật người xử lý hiện tại
- Submit approval: cập nhật workflow status
- Approve/reject public API nếu code hiện có cần cập nhật document status

Không query database document-service.
Không tạo Entity JPA cho document-service.

==================================================
WORKFLOW-SERVICE GỌI AUTH-SERVICE
==================================================

Tạo AuthServiceClient:

- getUserById(Long id)
- validateUsers(List<Long> userIds)
- getUnitById(Integer id)
- validateUnits(List<Integer> unitIds)
- getUserRoles(Long id)
- checkPermission(Long userId, String maChucNang, String permission)

Dùng khi:
- Validate nguoiGuiId
- Validate nguoiNhanId
- Validate nguoiPheDuyetId
- Validate donViXuLyId
- Check quyền approve/transfer nếu nghiệp vụ yêu cầu

==================================================
KAFKA NOTIFICATION EVENTS
==================================================

workflow-service không gọi trực tiếp notification-service bằng REST.

Khi có sự kiện workflow, publish event vào Kafka topic:

notification-events

Tạo:
- NotificationEventPublisher
- NotificationEvent DTO
- Event metadata DTO nếu cần

Event types:
- WORKFLOW_TRANSFERRED
- WORKFLOW_APPROVAL_REQUESTED
- WORKFLOW_SLA_VIOLATED

Config:
app:
kafka:
notification-topic: ${NOTIFICATION_EVENTS_TOPIC:notification-events}

Nếu project chưa có Kafka dependency:
- Thêm dependency phù hợp nếu đang dùng Spring Kafka
- Nếu chưa muốn tích hợp Kafka thật thì tạo publisher interface + implementation skeleton
- Trong test mock publisher
- Không làm fail business chính nếu publish event lỗi, trừ khi docs yêu cầu

==================================================
SERVICE-TO-SERVICE CONFIG
==================================================

Ưu tiên dùng WebClient.
Nếu project đã dùng OpenFeign thì dùng OpenFeign.

Không hardcode URL service.
Đọc từ application.yml/env:

services:
document-service:
base-url: ${DOCUMENT_SERVICE_URL:http://localhost:8082}
auth-service:
base-url: ${AUTH_SERVICE_URL:http://localhost:8081}

internal:
auth:
service-name: workflow-service
service-token: ${INTERNAL_SERVICE_TOKEN:change-me-in-dev}
allowed-services:
- document-service
- report-service
- support-service

Khi workflow-service gọi service khác:
- Gửi header nội bộ theo API-service.md
- Service name là workflow-service
- Token đọc từ config/env
- Không hardcode token
- Không dùng access token người dùng cho service-to-service
- Viết interceptor/filter cho client để tự gắn header

Khi service khác gọi /internal/workflows/**:
- Validate header nội bộ theo config
- Kiểm tra service name nằm trong allowed-services
- Nếu thiếu/sai header thì trả lỗi phù hợp
- Filter chỉ áp dụng cho /internal/**

==================================================
DATABASE WORKFLOW-SERVICE
==================================================

Dùng các bảng hiện có:
- QuyTrinh
- BuocQuyTrinh
- XuLyVanBan

Không thêm bảng/cột mới.

Mapping:
- SERIAL -> Integer
- BIGSERIAL -> Long
- TIMESTAMP -> LocalDateTime
- BOOLEAN -> Boolean
- Giữ nguyên tên bảng/cột bằng @Table và @Column
- Không đổi sang snake_case
- Không tạo JPA relation sang service khác

==================================================
KIẾN TRÚC CODE
==================================================

Theo cấu trúc hiện tại. Nếu chưa có thì dùng:

- controller.internal
- service
- service.impl
- repository
- entity
- dto.internal.request
- dto.internal.response
- client
- client.dto
- event
- event.publisher
- security
- config
- exception
- common
- mapper
- specification

Bắt buộc:
- Controller -> Service -> Repository
- Client layer riêng cho service-to-service
- Publisher layer riêng cho Kafka event
- DTO riêng
- Không trả Entity trực tiếp
- ApiResponse<T>
- PageResponse<T>
- ErrorCode
- AppException
- GlobalExceptionHandler
- Validate bằng jakarta.validation
- Pageable cho API list nếu endpoint có phân trang
- Specification cho filter phức tạp

==================================================
TEST BẮT BUỘC
==================================================

Viết test trước implementation.

1. InternalWorkflowSecurityTest:
- thiếu header nội bộ thì bị từ chối
- header sai thì bị từ chối
- service name không hợp lệ thì bị từ chối
- header hợp lệ thì cho qua

2. InternalWorkflowControllerTest:
- POST /internal/workflows/documents/{documentId}/start
- POST /internal/workflows/documents/{documentId}/transfer
- POST /internal/workflows/documents/{documentId}/submit-approval
- GET /internal/workflows/documents/{documentId}/status
- GET /internal/workflows/documents/{documentId}/timeline
- GET /internal/workflows/statistics
- GET /internal/workflows/progress
- GET /internal/workflows/sla/violations

3. InternalWorkflowServiceTest:
- start workflow success
- transfer success
- submit approval success
- get status success
- get timeline success
- statistics success
- progress success
- sla violations success
- document not found từ DocumentServiceClient thì trả lỗi phù hợp
- auth validate user/unit fail thì trả lỗi phù hợp

4. Client tests:
- DocumentServiceClientTest
- AuthServiceClientTest

Yêu cầu client test:
- Không gọi service thật
- Mock WebClient hoặc dùng MockWebServer
- Verify client gửi đúng header nội bộ
- Verify parse response đúng

5. NotificationEventPublisherTest:
- transfer publish WORKFLOW_TRANSFERRED
- submit approval publish WORKFLOW_APPROVAL_REQUESTED
- SLA violation publish WORKFLOW_SLA_VIOLATED nếu có flow check SLA
- Nếu Kafka skeleton thì test publisher interface được gọi đúng

6. Integration flow tests bằng mock:
- transfer document:
    + gọi AuthServiceClient validateUsers/validateUnits
    + gọi DocumentServiceClient updateDocumentAssignee
    + lưu XuLyVanBan
    + publish event
- submit approval:
    + gọi AuthServiceClient validateUsers
    + gọi DocumentServiceClient updateDocumentWorkflowStatus
    + lưu XuLyVanBan
    + publish event

==================================================
TDD FLOW
==================================================

Bước 1:
Đọc API-service.md và cấu trúc project.

Bước 2:
Viết test plan.

Bước 3:
Viết test cho Internal API + Internal Security.

Bước 4:
Chạy test để thấy fail.

Bước 5:
Implement Internal API + internal filter.

Bước 6:
Chạy test pass.

Bước 7:
Viết test cho client gọi document/auth.

Bước 8:
Chạy test fail.

Bước 9:
Implement client layer.

Bước 10:
Chạy test pass.

Bước 11:
Viết test Kafka publisher.

Bước 12:
Implement publisher hoặc skeleton publisher.

Bước 13:
Chạy test pass.

Bước 14:
Chạy toàn bộ test:
- ./mvnw test
  hoặc
- mvn test

Nếu test infra chưa đủ thì chạy:
- ./mvnw compile
  hoặc
- mvn compile

==================================================
SKELETON / TODO
==================================================

Nếu project chưa có Kafka thật:
- Tạo publisher interface
- Tạo implementation skeleton
- Ghi TODO tích hợp Kafka broker thật
- Test bằng mock publisher

Nếu document-service/auth-service chưa chạy:
- Client vẫn có interface/method
- Test bằng mock
- Không gọi service thật trong test

==================================================
CẤM
==================================================

- Không sửa public API nếu không cần
- Không làm lại toàn bộ /api/workflows/**
- Không thêm bảng/cột mới
- Không đổi tên bảng/cột
- Không hardcode URL service
- Không hardcode giá trị bảo mật
- Không query database service khác
- Không tạo Entity cho service khác
- Không gọi trực tiếp notification-service bằng REST
- Không trả Entity trực tiếp
- Không tắt security toàn cục
- Không bỏ qua test

==================================================
BÁO CÁO CUỐI
==================================================

Báo cáo bằng tiếng Việt:
1. Đã đọc file nào
2. Test plan đã tạo
3. Test class đã viết
4. Internal API đã implement
5. Client gọi service khác đã implement
6. Kafka/event publisher đã implement hoặc skeleton
7. Phần nào skeleton/TODO
8. File đã tạo/sửa
9. Lệnh test/build đã chạy
10. Kết quả test/build
11. Lỗi còn tồn tại nếu có