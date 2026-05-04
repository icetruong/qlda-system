Bạn đang code trong workflow-service.

Hiện trạng:
- API public /api/workflows/** đã có controller + service.
- Service hiện tại chủ yếu xử lý DB nội bộ.
- Đã có các client:
    - DocumentServiceClient
    - AuthServiceClient
- Đã có NotificationEventPublisher (Kafka) nhưng chưa dùng.
- Internal API đã gọi service khác, nhưng public API thì chưa.

Mục tiêu:
Kết nối các API public /api/workflows/** với:
- DocumentServiceClient
- AuthServiceClient
- NotificationEventPublisher

Không viết lại toàn bộ code.
Chỉ bổ sung logic gọi service và publish event vào flow hiện có.

==================================================
YÊU CẦU CHUNG
==================================================

- Không thay đổi API response hiện tại
- Không thay đổi database schema
- Không gọi trực tiếp DB của service khác
- Không hardcode config
- Không bỏ qua lỗi quan trọng
- Nếu publish event lỗi thì chỉ log, không làm fail nghiệp vụ chính

==================================================
CÁC FLOW CẦN BỔ SUNG
==================================================

1. Transfer document
   API:
   POST /api/workflows/documents/{documentId}/transfer

Bổ sung:
- Gọi DocumentServiceClient để kiểm tra document tồn tại
- Gọi AuthServiceClient để validate user và đơn vị
- Sau khi lưu XuLyVanBan:
    - Gọi updateDocumentAssignee
    - Gọi updateDocumentWorkflowStatus
- Publish event "WORKFLOW_TRANSFERRED"

--------------------------------------------------

2. Approve document
   API:
   POST /api/workflows/approvals/{processingId}/approve

Bổ sung:
- Gọi DocumentServiceClient để kiểm tra document
- Cập nhật trạng thái xử lý
- Gọi updateDocumentStatus
- Gọi updateDocumentWorkflowStatus
- Publish event "WORKFLOW_APPROVED"

--------------------------------------------------

3. Reject document
   API:
   POST /api/workflows/approvals/{processingId}/reject

Bổ sung:
- Gọi DocumentServiceClient
- Update trạng thái
- Gọi updateDocumentStatus
- Publish event "WORKFLOW_REJECTED"

--------------------------------------------------

4. Complete processing
   API:
   POST /api/workflows/processings/{processingId}/complete

Bổ sung:
- Update trạng thái xử lý
- Gọi updateDocumentWorkflowStatus
- Nếu hoàn thành toàn bộ:
    - Gọi updateDocumentStatus
- Publish event "WORKFLOW_COMPLETED"

--------------------------------------------------

5. Pending approvals (list)
   API:
   GET /api/workflows/approvals/pending

Bổ sung:
- Enrich dữ liệu:
    - Gọi DocumentServiceClient để lấy soKyHieu, trichYeu
    - Gọi AuthServiceClient để lấy tên người gửi
- Nếu lỗi enrich → vẫn trả dữ liệu cơ bản

--------------------------------------------------

6. Timeline
   API:
   GET /api/workflows/documents/{documentId}/timeline

Bổ sung:
- Gọi DocumentServiceClient để check tồn tại
- Enrich tên người xử lý từ AuthServiceClient

--------------------------------------------------

7. SLA / Reminder
   API:
   POST /api/workflows/reminders/send

Bổ sung:
- Duyệt các processing cần nhắc
- Publish event "WORKFLOW_SLA_VIOLATED"

==================================================
EVENT
==================================================

Khi publish event:
- Dùng NotificationEventPublisher
- Không gọi notification-service trực tiếp
- Event gồm:
    - eventType
    - sourceService = workflow-service
    - nguoiNhanIds
    - tieuDe
    - noiDung
    - referenceId
    - metadata

==================================================
TEST (TDD)
==================================================

Viết test trước:

1. transfer:
- verify gọi DocumentServiceClient
- verify gọi AuthServiceClient
- verify publish event

2. approve:
- verify update document
- verify publish event

3. reject:
- verify update document
- verify publish event

4. complete:
- verify update workflow status
- verify publish event

5. pending list:
- verify enrich document
- verify enrich user

6. timeline:
- verify enrich user

Mock toàn bộ client:
- DocumentServiceClient
- AuthServiceClient
- NotificationEventPublisher

Không gọi service thật trong test.

==================================================
CÁCH LÀM
==================================================

Bước 1:
Đọc WorkflowApiServiceImpl

Bước 2:
Viết test cho transfer trước

Bước 3:
Chạy test fail

Bước 4:
Inject client + publisher vào service

Bước 5:
Implement transfer flow

Bước 6:
Lặp lại cho các flow còn lại

Bước 7:
Chạy toàn bộ test

==================================================
BÁO CÁO
==================================================

Trả lời bằng tiếng Việt:

1. Đã sửa những file nào
2. Flow nào đã gọi DocumentServiceClient
3. Flow nào đã gọi AuthServiceClient
4. Flow nào đã publish event
5. Test đã viết
6. Kết quả test
7. Phần còn TODO nếu có