Bạn đang code trong notification-service của hệ thống QLVB.

Hãy đọc trước:
1. AGENTS.md nếu có
2. .agents/skills nếu có
3. Skill tdd-workflow nếu có
4. API-service.md
5. API_NOTIFICATION.md hoặc API_REPORT.md nếu có
6. pom.xml / build.gradle
7. application.yml / application.properties
8. Cấu trúc package hiện tại

Mục tiêu:
Implement notification-service theo TDD.

Service này sẽ kiêm luôn report-service, gồm 2 nhóm chức năng:
1. Notification: consume Kafka event, tạo thông báo, gửi SYSTEM/EMAIL/TEAMS dạng skeleton nếu chưa có tích hợp thật
2. Report: cung cấp API báo cáo/dashboard bằng cách gọi Internal API của document-service, workflow-service, auth-service

Không tách report-service thành project riêng ở bước này.

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
DATABASE
==================================================

Dùng bảng notification hiện có nếu project đã có.

Nếu schema hiện tại có bảng ThongBao thì mapping:

CREATE TABLE ThongBao (
ID BIGSERIAL PRIMARY KEY,
TieuDe VARCHAR(255) NOT NULL,
NoiDung TEXT NOT NULL,
NguoiNhanID BIGINT,
VanBanID BIGINT,
LoaiThongBao VARCHAR(100),
KenhGui VARCHAR(50),
DaDoc BOOLEAN DEFAULT FALSE,
NgayGui TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
NgayDoc TIMESTAMP
);

Mapping:
- BIGSERIAL -> Long
- TIMESTAMP -> LocalDateTime
- BOOLEAN -> Boolean
- TEXT -> String
- Giữ nguyên tên bảng/cột bằng @Table và @Column
- Không đổi sang snake_case
- Không tạo JPA entity cho service khác
- Không query database của service khác

Nếu chưa có bảng riêng lưu event đã xử lý:
- Không tự ý thêm bảng/cột mới
- Tạo EventDeduplicationService skeleton/in-memory để chống xử lý trùng trong runtime
- Ghi TODO cần bảng processed_event nếu muốn chống trùng bền vững

Nếu chưa có bảng trạng thái gửi:
- Không thêm bảng
- Log trạng thái gửi hoặc skeleton
- Ghi TODO rõ ràng

==================================================
NOTIFICATION API PUBLIC
==================================================

Nếu service đã có public API notification thì giữ nguyên, không refactor lớn.

Nếu chưa có thì implement tối thiểu:

- POST /api/notifications
- GET /api/notifications
- PATCH /api/notifications/{id}/read
- DELETE /api/notifications/{id}
- POST /api/notifications/{id}/send

Yêu cầu:
- Không trả Entity trực tiếp
- Dùng DTO
- Dùng ApiResponse/PageResponse nếu project đã có
- Pageable cho list
- DaDoc mặc định false
- Mark read set DaDoc = true, NgayDoc = now
- Send là skeleton nếu chưa có email/teams thật

==================================================
REPORT API TRONG NOTIFICATION-SERVICE
==================================================

notification-service sẽ kiêm luôn report-service.

Base path:
- /api/reports

Implement các API:
- GET /api/reports/dashboard
- GET /api/reports/documents/statistics
- GET /api/reports/workflows/progress
- GET /api/reports/overdue-documents
- GET /api/reports/export

Report không có bảng riêng.
Không tạo Entity report.
Report lấy dữ liệu bằng cách gọi Internal API của service khác.

==================================================
REPORT GỌI DOCUMENT-SERVICE
==================================================

Tạo DocumentServiceClient:

1. getDocumentStatistics(fromDate, toDate, donViId, groupBy)
   Gọi:
   GET /internal/documents/statistics

Response data gồm:
- totalDocuments
- incomingDocuments
- outgoingDocuments
- items

2. getOverdueDocuments(donViId, nguoiXuLyId, page, size)
   Gọi:
   GET /internal/documents/overdue

Response data gồm:
- content
- page
- size
- totalElements

==================================================
REPORT GỌI WORKFLOW-SERVICE
==================================================

Tạo WorkflowServiceClient:

1. getWorkflowStatistics(fromDate, toDate, donViId)
   Gọi:
   GET /internal/workflows/statistics

Response:
- totalTasks
- completedTasks
- processingTasks
- overdueTasks

2. getWorkflowProgress(fromDate, toDate, donViId, nguoiXuLyId)
   Gọi:
   GET /internal/workflows/progress

Response:
- totalTasks
- completedTasks
- processingTasks
- items

3. getSlaViolations(fromDate, toDate, donViId)
   Gọi:
   GET /internal/workflows/sla/violations

==================================================
REPORT GỌI AUTH-SERVICE
==================================================

Tạo AuthServiceClient:

- getUserById(Long id)
  Gọi:
  GET /internal/auth/users/{id}

- getUnitById(Integer id)
  Gọi:
  GET /internal/auth/units/{id}

Dùng để enrich báo cáo:
- nguoiXuLyId -> nguoiXuLy
- donViId -> tenDonVi

Nếu auth-service chưa sẵn sàng:
- Client vẫn có interface/method
- Test mock client
- Không gọi service thật trong test
- Nếu enrich fail thì vẫn trả báo cáo cơ bản và ghi log/TODO, trừ khi nghiệp vụ yêu cầu fail

==================================================
SERVICE-TO-SERVICE CONFIG
==================================================

Ưu tiên dùng WebClient.
Nếu project đang dùng OpenFeign thì dùng OpenFeign.

Không hardcode URL service.
Đọc từ application.yml/env:

services:
document-service:
base-url: ${DOCUMENT_SERVICE_URL:http://localhost:8082}
workflow-service:
base-url: ${WORKFLOW_SERVICE_URL:http://localhost:8083}
auth-service:
base-url: ${AUTH_SERVICE_URL:http://localhost:8081}

internal:
auth:
service-name: notification-service
service-token: ${INTERNAL_SERVICE_TOKEN:change-me-in-dev}

Khi notification-service gọi service khác:
- Gửi header nội bộ theo API-service.md
- Service name là notification-service
- Token đọc từ config/env
- Không hardcode token
- Không dùng access token người dùng cho service-to-service
- Viết interceptor/filter cho client để tự gắn header

==================================================
KAFKA NOTIFICATION EVENT
==================================================

notification-service consume Kafka event từ topic:

- notification-events

Dead-letter topic:
- notification-events-dlq

Tạo:
- NotificationEventConsumer
- NotificationEvent DTO
- NotificationEventHandler
- EventDeduplicationService
- NotificationDeliveryService
- SystemNotificationSender
- EmailNotificationSender skeleton
- TeamsNotificationSender skeleton

Event mẫu:
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

Xử lý event:
1. Consume event
2. Validate eventId, eventType, nguoiNhanIds, tieuDe, noiDung
3. Kiểm tra eventId để tránh xử lý trùng
4. Tạo một thông báo cho từng nguoiNhanId
5. Lưu vào DB bảng ThongBao
6. Gửi theo kenhGui:
    - SYSTEM: coi như đã lưu DB là thành công
    - EMAIL: skeleton/TODO nếu chưa cấu hình mail
    - TEAMS: skeleton/TODO nếu chưa cấu hình Teams
7. Nếu xử lý lỗi thì publish hoặc route sang DLQ nếu project có Kafka DLQ config
8. Không làm mất event silently

Nếu project chưa có Kafka dependency:
- Thêm Spring Kafka nếu phù hợp
- Hoặc tạo consumer/handler skeleton để test được business logic
- Ghi TODO tích hợp Kafka broker thật

Config gợi ý:

app:
kafka:
notification-topic: ${NOTIFICATION_EVENTS_TOPIC:notification-events}
notification-dlq-topic: ${NOTIFICATION_EVENTS_DLQ_TOPIC:notification-events-dlq}

spring:
kafka:
consumer:
group-id: notification-service
auto-offset-reset: earliest

==================================================
REPORT LOGIC
==================================================

1. GET /api/reports/dashboard
- Gọi document-service lấy document statistics
- Gọi workflow-service lấy workflow statistics
- Tính:
    + totalDocuments
    + incomingDocuments
    + outgoingDocuments
    + completedDocuments
    + processingDocuments
    + overdueDocuments
    + completionRate
    + overdueRate

2. GET /api/reports/documents/statistics
- Gọi document-service /internal/documents/statistics
- groupBy: status, type, unit, month
- Validate groupBy
- Trả đúng response format

3. GET /api/reports/workflows/progress
- Gọi workflow-service /internal/workflows/progress
- Enrich user name bằng auth-service nếu có nguoiXuLyId
- Nếu enrich fail thì giữ userId và ghi log/TODO

4. GET /api/reports/overdue-documents
- Gọi document-service /internal/documents/overdue
- Có thể kết hợp workflow-service SLA violations nếu cần
- Trả PageResponse đúng format

5. GET /api/reports/export
- Validate reportType: dashboard, document_statistics, workflow_progress, overdue_documents
- Validate format: excel, pdf
- Nếu chưa có thư viện export thì skeleton tạo fileName/fileUrl giả lập hợp lý
- Ghi TODO tích hợp export Excel/PDF thật
- Không tạo bảng export

==================================================
SECURITY
==================================================

Public API:
- /api/notifications/**
- /api/reports/**

Các API này dùng security hiện có của service.
Không tắt security toàn cục.

Internal client:
- Khi gọi service khác phải gắn header nội bộ từ config
- Không hardcode giá trị bảo mật
- Không dùng access token người dùng cho service-to-service

notification-service không cần cung cấp Internal API cho service khác, trừ khi project hiện có yêu cầu riêng.

==================================================
KIẾN TRÚC CODE
==================================================

Theo cấu trúc hiện tại. Nếu chưa có thì dùng:

- controller
- service
- service.impl
- repository
- entity
- dto.request
- dto.response
- dto.kafka
- client
- client.dto
- event
- event.consumer
- event.handler
- sender
- config
- security
- exception
- common
- mapper
- specification

Bắt buộc:
- Controller -> Service -> Repository
- Client layer riêng cho report service-to-service
- Kafka consumer/handler riêng
- Sender layer riêng
- DTO riêng
- Không trả Entity trực tiếp
- ApiResponse<T>
- PageResponse<T>
- ErrorCode
- AppException
- GlobalExceptionHandler
- Validate bằng jakarta.validation
- Pageable cho API list

==================================================
TEST BẮT BUỘC
==================================================

Viết test trước implementation.

1. NotificationEventHandlerTest:
- handle single receiver event success
- handle multiple receivers event success
- duplicate eventId thì không tạo lại thông báo
- invalid event thiếu eventId thì reject
- invalid event thiếu nguoiNhanIds thì reject
- SYSTEM channel tạo notification thành công
- EMAIL channel gọi EmailNotificationSender
- TEAMS channel gọi TeamsNotificationSender
- sender lỗi thì không làm mất toàn bộ event, có log hoặc DLQ behavior nếu có

2. NotificationEventConsumerTest:
- consume valid event gọi handler
- handler lỗi thì route DLQ hoặc gọi DlqPublisher nếu có skeleton
- parse JSON lỗi thì handle an toàn

3. NotificationServiceTest:
- create notification success
- list notification by nguoiNhanId
- mark read success
- delete success
- notification not found

4. NotificationControllerTest:
- POST /api/notifications success
- GET /api/notifications success
- PATCH read success
- DELETE success
- request chưa authenticated bị từ chối nếu security đang bật

5. ReportServiceTest:
- dashboard success với mock DocumentServiceClient + WorkflowServiceClient
- completionRate tính đúng
- overdueRate tính đúng
- document statistics success
- invalid groupBy -> INVALID_REQUEST
- workflow progress success
- enrich user name bằng AuthServiceClient
- overdue documents success
- export success skeleton
- service client error -> xử lý lỗi phù hợp

6. ReportControllerTest:
- GET /api/reports/dashboard success
- GET /api/reports/documents/statistics success
- GET /api/reports/workflows/progress success
- GET /api/reports/overdue-documents success
- GET /api/reports/export success

7. Client tests:
- DocumentServiceClientTest
- WorkflowServiceClientTest
- AuthServiceClientTest

Yêu cầu client test:
- Không gọi service thật
- Mock WebClient hoặc dùng MockWebServer
- Verify client gửi đúng header nội bộ
- Verify parse response đúng

==================================================
TDD FLOW
==================================================

Bước 1:
Đọc API-service.md và cấu trúc project.

Bước 2:
Viết test plan.

Bước 3:
Viết test Notification Event Handler trước.

Bước 4:
Chạy test để thấy fail.

Bước 5:
Implement DTO, handler, deduplication, sender skeleton.

Bước 6:
Chạy test pass.

Bước 7:
Viết test Kafka Consumer.

Bước 8:
Implement consumer/DLQ skeleton.

Bước 9:
Chạy test pass.

Bước 10:
Viết test Notification API.

Bước 11:
Implement Notification API.

Bước 12:
Chạy test pass.

Bước 13:
Viết test ReportService và clients.

Bước 14:
Implement client layer gọi document/workflow/auth.

Bước 15:
Implement Report API.

Bước 16:
Chạy toàn bộ test:
- ./mvnw test
  hoặc
- mvn test

Nếu test infra chưa đủ:
- ./mvnw compile
  hoặc
- mvn compile

==================================================
SKELETON / TODO
==================================================

Nếu chưa có tích hợp thật:
- EMAIL sender: skeleton/TODO
- TEAMS sender: skeleton/TODO
- Kafka DLQ publisher: skeleton/TODO
- Export Excel/PDF: skeleton/TODO
- Event dedup bền vững bằng DB: TODO
- Enrich user/unit từ auth-service nếu service chưa sẵn sàng: client + mock test

Không thêm bảng/cột mới nếu chưa được yêu cầu.

==================================================
CẤM
==================================================

- Không code implementation trước test
- Không bỏ qua test
- Không báo hoàn thành khi chưa test/compile
- Không tách report-service thành project riêng
- Không tạo bảng report
- Không query database của service khác
- Không tạo JPA Entity cho service khác
- Không gọi trực tiếp database document/workflow/auth
- Không hardcode URL service
- Không hardcode giá trị bảo mật
- Không dùng access token người dùng cho service-to-service
- Không trả Entity trực tiếp
- Không tắt security toàn cục
- Không làm mất Kafka event silently

==================================================
BÁO CÁO CUỐI
==================================================

Báo cáo bằng tiếng Việt:
1. Đã đọc file nào
2. Test plan đã tạo
3. Test class đã viết
4. Kafka consumer/event handler đã implement
5. Notification API đã implement
6. Report API đã implement
7. Client gọi document/workflow/auth đã implement
8. Phần nào skeleton/TODO
9. File đã tạo/sửa
10. Lệnh test/build đã chạy
11. Kết quả test/build
12. Lỗi còn tồn tại nếu có