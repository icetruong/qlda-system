Bạn đang code trong document-service của hệ thống QLVB.

Hãy đọc trước:
1. AGENTS.md nếu có
2. .agents/skills nếu có
3. Skill tdd-workflow nếu có
4. API-service.md
5. pom.xml / build.gradle
6. application.yml / application.properties
7. Cấu trúc package hiện tại

Mục tiêu:
Chỉ bổ sung phần service-to-service theo API-service.md cho document-service.

Không sửa lại public API theo API.md.
Không refactor lớn public API hiện có nếu không cần.
Không làm lại các controller /api/documents/** đã có.
Chỉ thêm:
1. Internal API của document-service: /internal/documents/**
2. Client để document-service gọi các service khác:
    - auth-service
    - workflow-service
    - ai-service
    - notification-service
3. Cơ chế xác thực nội bộ cho /internal/**
4. Test theo TDD

==================================================
BẮT BUỘC: TDD
==================================================

Làm theo Red → Green → Refactor:

- Viết test trước
- Chạy test để thấy fail
- Sau đó mới implement code
- Chạy lại test cho pass
- Refactor nếu cần
- Không báo hoàn thành nếu chưa chạy test hoặc compile

==================================================
INTERNAL API DOCUMENT-SERVICE
==================================================

Base path:
- /internal/documents

Implement các endpoint trong API-service.md:

- GET /internal/documents/{id}
- GET /internal/documents/{id}/content
- GET /internal/documents/{id}/attachments
- PATCH /internal/documents/{id}/status
- PATCH /internal/documents/{id}/assignee
- PATCH /internal/documents/{id}/workflow-status
- POST /internal/documents/{id}/ai-results
- PATCH /internal/documents/{id}/ocr-status
- GET /internal/documents/statistics
- GET /internal/documents/overdue

Yêu cầu:
- Dùng Entity/Repository hiện có của document-service
- Không thêm bảng/cột mới
- Không đổi schema
- Nếu DB chưa có field để lưu workflowStatus/currentStep/processingId/ocrText/aiResult thì làm skeleton/TODO rõ ràng
- Không trả Entity trực tiếp
- Response dùng ApiResponse chung nếu project đã có

==================================================
DOCUMENT-SERVICE GỌI SERVICE KHÁC
==================================================

Tạo client layer:

1. AuthServiceClient:
- GET /internal/auth/users/{id}
- POST /internal/auth/users/validate
- GET /internal/auth/units/{id}
- POST /internal/auth/units/validate
- GET /internal/auth/users/{id}/roles
- POST /internal/auth/permissions/check

2. WorkflowServiceClient:
- POST /internal/workflows/documents/{documentId}/start
- POST /internal/workflows/documents/{documentId}/transfer
- POST /internal/workflows/documents/{documentId}/submit-approval
- GET /internal/workflows/documents/{documentId}/status
- GET /internal/workflows/documents/{documentId}/timeline

3. AiServiceClient:
- POST /internal/ai/ocr
- POST /internal/ai/summarize
- POST /internal/ai/classify
- POST /internal/ai/metadata/extract
- POST /internal/ai/suggestions

4. NotificationServiceClient:
- POST /internal/notifications/send
- POST /internal/notifications/bulk-send

Ưu tiên dùng WebClient.
Nếu project đã dùng OpenFeign thì dùng OpenFeign.

Không hardcode URL service.
Đọc từ application.yml/env:

services:
auth-service:
base-url: ${AUTH_SERVICE_URL:http://localhost:8081}
workflow-service:
base-url: ${WORKFLOW_SERVICE_URL:http://localhost:8083}
ai-service:
base-url: ${AI_SERVICE_URL:http://localhost:8084}
notification-service:
base-url: ${NOTIFICATION_SERVICE_URL:http://localhost:8085}

==================================================
XÁC THỰC NỘI BỘ
==================================================

Khi document-service gọi service khác:
- Gửi header nội bộ theo API-service.md
- Service name là document-service
- Token đọc từ env/config
- Không hardcode token
- Không dùng user access token cho service-to-service

Khi service khác gọi /internal/documents/**:
- Validate header nội bộ theo config
- Kiểm tra service name nằm trong allowed-services
- Nếu thiếu/sai header thì trả lỗi phù hợp
- Filter chỉ áp dụng cho /internal/**

Config gợi ý:

internal:
auth:
service-name: document-service
service-token: ${INTERNAL_SERVICE_TOKEN:change-me-in-dev}
allowed-services:
- workflow-service
- ai-service
- report-service
- support-service
- notification-service

==================================================
TEST BẮT BUỘC
==================================================

Viết test trước implementation:

1. InternalDocumentSecurityTest:
- thiếu header nội bộ thì bị từ chối
- header sai thì bị từ chối
- service name không hợp lệ thì bị từ chối
- header hợp lệ thì cho qua

2. InternalDocumentControllerTest:
- GET /internal/documents/{id}
- GET /internal/documents/{id}/content
- GET /internal/documents/{id}/attachments
- PATCH /internal/documents/{id}/status
- PATCH /internal/documents/{id}/assignee
- PATCH /internal/documents/{id}/workflow-status
- POST /internal/documents/{id}/ai-results
- PATCH /internal/documents/{id}/ocr-status
- GET /internal/documents/statistics
- GET /internal/documents/overdue

3. InternalDocumentServiceTest:
- get internal document success
- document not found
- get content success
- get attachments success
- update status success
- update OCR status success
- statistics success
- overdue success

4. Client tests:
- AuthServiceClientTest
- WorkflowServiceClientTest
- AiServiceClientTest
- NotificationServiceClientTest

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
Viết test cho Internal API + Internal Security.

Bước 4:
Chạy test để thấy fail.

Bước 5:
Implement Internal API + internal filter.

Bước 6:
Chạy test pass.

Bước 7:
Viết test cho client gọi auth/workflow/ai/notification.

Bước 8:
Chạy test fail.

Bước 9:
Implement client layer.

Bước 10:
Chạy test pass.

Bước 11:
Chạy toàn bộ test:
- ./mvnw test
  hoặc
- mvn test

Nếu test infra chưa đủ thì chạy:
- ./mvnw compile
  hoặc
- mvn compile

==================================================
CẤM
==================================================

- Không sửa public API theo API.md
- Không làm lại /api/documents/**
- Không thêm bảng/cột mới
- Không đổi tên bảng/cột
- Không hardcode URL service
- Không hardcode giá trị bảo mật
- Không query database service khác
- Không tạo Entity cho service khác
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
6. Phần nào skeleton/TODO
7. File đã tạo/sửa
8. Lệnh test/build đã chạy
9. Kết quả test/build
10. Lỗi còn tồn tại nếu có