Bạn đang code trong document-service.

Hiện trạng:
- Public API /api/documents/** đã có service/controller.
- Internal API /internal/documents/** đã có.
- Các client AuthServiceClient, WorkflowServiceClient, AiServiceClient đã có interface/implementation nhưng chưa được dùng trong flow nghiệp vụ.
- NotificationEventPublisher / Kafka publisher đã có nhưng chưa được gọi trong các flow nghiệp vụ.
- DocumentWorkflowServiceImpl hiện mới xử lý nội bộ DB + trả response, chưa kết nối service-to-service.

Mục tiêu:
Kết nối lại các flow nghiệp vụ public API với các client service-to-service và Kafka event theo API-service.md.

KHÔNG làm lại từ đầu.
KHÔNG rewrite toàn bộ service.
KHÔNG sửa public API contract nếu không cần.
Chỉ bổ sung integration vào flow hiện có.

==================================================
BẮT BUỘC: TDD
==================================================

Làm theo Red → Green → Refactor:

1. Viết test trước cho flow integration.
2. Chạy test để fail.
3. Implement code tối thiểu để pass.
4. Chạy lại test.
5. Refactor nếu cần.

Không báo hoàn thành nếu chưa chạy test hoặc compile.

==================================================
CÁC FLOW CẦN KẾT NỐI
==================================================

1. Tạo văn bản đến
   API:
   POST /api/documents/incoming

Cần bổ sung:
- Nếu request có donViChuTriId thì gọi AuthServiceClient validate/get unit.
- Sau khi lưu VanBan thành công, gọi WorkflowServiceClient.startWorkflow(documentId, ...)
- Nếu start workflow thành công, có thể cập nhật trạng thái workflow nếu project đã có field/hook phù hợp.
- Publish Kafka event DOCUMENT_CREATED nếu xác định được người nhận; nếu chưa có người nhận thì không publish hoặc publish theo rule hiện có.
- Không rollback tạo văn bản chỉ vì publish notification thất bại, trừ khi logic hiện có yêu cầu.

Test cần có:
- create incoming calls AuthServiceClient.
- create incoming calls WorkflowServiceClient.startWorkflow.
- create incoming saves document even when notification publisher fails.

2. Tạo văn bản đi
   API:
   POST /api/documents/outgoing

Cần bổ sung:
- Validate donViChuTriId qua AuthServiceClient.
- Sau khi lưu document, gọi WorkflowServiceClient.startWorkflow nếu API-service.md yêu cầu văn bản đi đi theo quy trình.
- Không gọi service thật trong test, mock client.

3. Chuyển xử lý văn bản đến
   API:
   POST /api/documents/incoming/{id}/transfer

Cần bổ sung:
- Validate nguoiNhanId qua AuthServiceClient.
- Validate donViXuLyId qua AuthServiceClient.
- Gọi WorkflowServiceClient.transferDocument(documentId, ...)
- Cập nhật trạng thái VanBan = 2 nếu flow hiện tại đang làm như vậy.
- Publish Kafka event DOCUMENT_TRANSFERRED hoặc event tương ứng nếu document-service đang phát event.
- Event metadata gồm documentId, soKyHieu, trichYeu, nguoiNhanId, donViXuLyId, hanXuLy.

Test cần có:
- transfer calls AuthServiceClient validate user/unit.
- transfer calls WorkflowServiceClient.transferDocument.
- transfer publishes notification event.
- transfer still persists document state correctly.

4. Trình ký văn bản nháp
   API:
   POST /api/documents/drafts/{id}/submit-signing

Cần bổ sung:
- Validate nguoiKyId qua AuthServiceClient.
- Có thể gọi AuthServiceClient.getUserRoles hoặc checkPermission nếu đã có method.
- Gọi WorkflowServiceClient.submitApproval(documentId, ...)
- Cập nhật trạng thái VanBan = 3.
- Publish Kafka event DOCUMENT_SUBMITTED_SIGNING hoặc DOCUMENT_APPROVAL_REQUESTED nếu đang dùng chung event.

Test cần có:
- submit signing validates signer.
- submit signing calls workflow submitApproval.
- submit signing publishes event.

5. Gửi phê duyệt văn bản đi
   API:
   POST /api/documents/outgoing/{id}/submit-approval

Cần bổ sung:
- Validate nguoiPheDuyetId qua AuthServiceClient.
- Gọi WorkflowServiceClient.submitApproval(documentId, ...)
- Cập nhật trạng thái VanBan = 3.
- Publish Kafka event DOCUMENT_APPROVAL_REQUESTED.

6. OCR process
   API:
   POST /api/documents/{id}/ocr/process

Cần bổ sung:
- Gọi AiServiceClient.processOcr(...)
- Khi AI trả về thành công, cập nhật DaOCR nếu logic hiện tại cho phép.
- Nếu chi tiết OCR không có cột lưu ở document-service thì không thêm cột, chỉ trả response theo public API và TODO.
- Nếu AI lỗi thì trả OCR_FAILED hoặc lỗi phù hợp.

Test cần có:
- ocr process calls AiServiceClient.
- ai error maps to OCR_FAILED.
- success updates daOCR if current logic supports it.

7. Phát hành văn bản
   API:
   POST /api/documents/{id}/publish

Cần bổ sung:
- Cập nhật NgayPhatHanh, TrangThai = 5 như hiện tại.
- Publish Kafka event DOCUMENT_PUBLISHED.
- Nếu request/logic có nguoiNhanIds hoặc donViNhanIds ở flow gửi văn bản thì dùng danh sách đó.
- Nếu chỉ publish không có người nhận thì ghi TODO hoặc không publish.

8. Gửi văn bản
   API:
   POST /api/documents/{id}/send

Cần bổ sung:
- Validate nguoiNhanIds qua AuthServiceClient.validateUsers.
- Validate donViNhanIds qua AuthServiceClient.validateUnits.
- Publish Kafka event DOCUMENT_SENT hoặc DOCUMENT_PUBLISHED với nguoiNhanIds.
- Không gọi notification-service REST trực tiếp.
- Chỉ publish Kafka event.

==================================================
KHÔNG LÀM
==================================================

- Không gọi notification-service bằng REST.
- Không query database service khác.
- Không tạo JPA entity cho service khác.
- Không thêm bảng/cột mới.
- Không thay đổi API response nếu không cần.
- Không rewrite toàn bộ DocumentWorkflowServiceImpl.
- Không hardcode URL service hoặc token.
- Không dùng user access token cho service-to-service.

==================================================
CLIENT USAGE
==================================================

AuthServiceClient dùng cho:
- validate/get user
- validate/get unit
- check permission nếu method đã có

WorkflowServiceClient dùng cho:
- startWorkflow
- transferDocument
- submitApproval
- getWorkflowStatus nếu detail API cần enrich trạng thái workflow

AiServiceClient dùng cho:
- processOcr
- summarize/classify/extract metadata nếu public API hiện tại có flow tương ứng

NotificationEventPublisher dùng cho:
- DOCUMENT_CREATED
- DOCUMENT_TRANSFERRED
- DOCUMENT_APPROVAL_REQUESTED
- DOCUMENT_PUBLISHED
- DOCUMENT_SENT nếu có

Kafka topic:
notification-events

Event phải có:
- eventId
- eventType
- sourceService = document-service
- nguoiNhanIds
- tieuDe
- noiDung
- loaiThongBao
- kenhGui
- referenceType = DOCUMENT
- referenceId = documentId
- metadata
- createdAt

==================================================
TEST PLAN BẮT BUỘC
==================================================

Viết hoặc sửa test trước:

1. DocumentWorkflowServiceIntegrationTest hoặc DocumentWorkflowServiceImplTest

Test cases:
- createIncomingDocument_shouldValidateUnitAndStartWorkflow
- createOutgoingDocument_shouldValidateUnitAndStartWorkflow
- transferIncomingDocument_shouldValidateReceiverAndUnitAndCallWorkflowAndPublishEvent
- submitDraftSigning_shouldValidateSignerAndCallWorkflowAndPublishEvent
- submitOutgoingApproval_shouldValidateApproverAndCallWorkflowAndPublishEvent
- processOcr_shouldCallAiServiceAndUpdateOcrStatus
- publishDocument_shouldPublishNotificationEvent
- sendDocument_shouldValidateReceiversAndPublishNotificationEvent

2. Client interaction tests:
   Dùng Mockito verify:
- authServiceClient.validateUsers(...)
- authServiceClient.validateUnits(...)
- workflowServiceClient.startWorkflow(...)
- workflowServiceClient.transferDocument(...)
- workflowServiceClient.submitApproval(...)
- aiServiceClient.processOcr(...)
- notificationEventPublisher.publish(...)

3. Failure tests:
- Auth validation fail thì business flow fail với lỗi phù hợp.
- Workflow client fail thì trả lỗi phù hợp và không publish event.
- AI OCR fail thì trả OCR_FAILED.
- Kafka publish fail không rollback nghiệp vụ chính, chỉ log warning, trừ khi project đang quy định khác.

==================================================
TDD FLOW CỤ THỂ
==================================================

Bước 1:
Đọc DocumentWorkflowServiceImpl, các client interface/implementation, NotificationEventPublisher, các DTO request/response hiện có.

Bước 2:
Viết test cho create incoming integration trước.

Bước 3:
Chạy test fail.

Bước 4:
Inject AuthServiceClient, WorkflowServiceClient, AiServiceClient, NotificationEventPublisher vào service cần thiết.

Bước 5:
Implement create incoming integration tối thiểu để pass.

Bước 6:
Lặp lại cho:
- outgoing create
- transfer
- submit signing
- submit approval
- OCR process
- publish
- send

Bước 7:
Chạy toàn bộ test:
- ./mvnw test
  hoặc
- mvn test

Nếu test infra chưa đủ:
- ./mvnw compile
  hoặc
- mvn compile

==================================================
YÊU CẦU CODE STYLE
==================================================

- Giữ nguyên cấu trúc hiện có.
- Thêm private helper methods nếu cần:
    + validateUser
    + validateUsers
    + validateUnit
    + validateUnits
    + publishDocumentEvent
    + startWorkflowIfRequired
- Không làm method quá dài.
- Không swallow exception im lặng.
- Có log rõ ràng khi client call hoặc publish event fail.
- Mapping DTO rõ ràng, không trả Entity trực tiếp.

==================================================
BÁO CÁO CUỐI
==================================================

Báo cáo bằng tiếng Việt:
1. Đã đọc file nào
2. Test đã thêm/sửa
3. Flow nào đã kết nối AuthServiceClient
4. Flow nào đã kết nối WorkflowServiceClient
5. Flow nào đã kết nối AiServiceClient
6. Flow nào đã publish Kafka notification event
7. Flow nào còn TODO
8. File đã sửa
9. Lệnh test/build đã chạy
10. Kết quả test/build
11. Lỗi còn tồn tại nếu có