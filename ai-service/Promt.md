Bạn là AI coding agent đang làm trong project QLDA, module hiện tại là ai-service.

Nhiệm vụ:
Hãy triển khai phần API và API-service cho ai-service theo đặc tả API AI AGENT đã có, bao gồm các nhóm API:
- Tóm tắt văn bản
- Phân loại văn bản
- Trích xuất metadata
- Semantic search
- Index / delete index văn bản
- Gợi ý hướng xử lý
- Gợi ý nội dung phản hồi
- Chatbot hỏi đáp
- Quản lý kết quả AI

Yêu cầu bắt buộc:
1. Trước khi code, hãy đọc kỹ các file cấu hình/instruction đã có trong project:
    - AGENTS.md
    - .agents/*
    - .codex/*
      Sau đó tuân thủ đúng phương pháp TDD được mô tả trong các file này.

2. Làm theo quy trình TDD:
    - Phân tích yêu cầu
    - Lập plan ngắn gọn
    - Viết test fail trước
    - Implement tối thiểu để test pass
    - Refactor
    - Chạy lại test
    - Báo cáo theo vòng RED → GREEN → REFACTOR

3. Không code lan man. Chỉ làm đúng phạm vi API và API-service của ai-service.

4. Thiết kế chuẩn theo Spring Boot:
    - Controller chỉ nhận request, validate, trả response
    - Service xử lý nghiệp vụ AI
    - API-service/client riêng để gọi sang service khác
    - DTO request/response rõ ràng
    - Repository chỉ xử lý DB
    - Exception handler dùng response chuẩn

5. Tất cả response phải theo format chung:
   Thành công:
   {
   "success": true,
   "message": "...",
   "data": {}
   }

   Lỗi:
   {
   "success": false,
   "message": "...",
   "errorCode": "..."
   }

6. Implement các public API dưới base path:
   /api/ai

Danh sách API cần có:
- POST /api/ai/summarize
- POST /api/ai/summarize/file
- POST /api/ai/classify
- POST /api/ai/classify/file
- POST /api/ai/metadata/extract
- POST /api/ai/metadata/extract/file
- POST /api/ai/search/semantic
- POST /api/ai/search/index-document
- DELETE /api/ai/search/index-document/{documentId}
- POST /api/ai/suggestions/handling
- POST /api/ai/suggestions/reply
- POST /api/ai/chatbot/ask
- GET /api/ai/results/documents/{documentId}
- GET /api/ai/results/{id}
- DELETE /api/ai/results/{id}

7. Kết quả AI sau khi xử lý phải được lưu vào bảng KetQuaAI:
    - SUMMARY
    - CLASSIFICATION
    - METADATA_EXTRACTION
    - SUGGESTION_HANDLING
    - SUGGESTION_REPLY
    - CHATBOT

8. Semantic search dùng bảng ai_document_chunk:
    - Chia text thành chunk
    - Tạo embedding
    - Lưu embedding + metadata
    - Search bằng vector similarity
    - Chatbot cũng phải tìm context từ ai_document_chunk trước khi sinh câu trả lời

9. File upload API dùng multipart/form-data.
   Với các API file:
    - Validate file
    - Reject định dạng không hợp lệ bằng INVALID_FILE_FORMAT
    - Extract text/OCR nếu cần
    - Sau đó gọi nghiệp vụ AI tương ứng

10. Phần API-service bắt buộc phải được tách riêng để gọi các service khác, không gọi trực tiếp lẫn trong business service.

Cần tạo DocumentInternalApiService hoặc client tương đương để ai-service gọi document-service qua internal API:

- GET /internal/documents/{id}
  Dùng để kiểm tra văn bản tồn tại, lấy metadata như trichYeu, loaiVanBanId, donViChuTriId, nguoiTaoId, hanXuLy.

- GET /internal/documents/{id}/content
  Dùng khi tóm tắt, phân loại, trích xuất metadata, gợi ý xử lý, chatbot cần context theo văn bản.

- GET /internal/documents/{id}/attachments
  Dùng khi OCR file PDF, tóm tắt từ file, trích xuất metadata từ file.

- PATCH /internal/documents/{id}/ocr-status
  Dùng khi OCR thành công để cập nhật daOCR = true bên document-service.

11. Giao tiếp nội bộ service-to-service phải dùng cơ chế xác thực riêng:
- Không dùng JWT Azure AD của người dùng cho service-to-service
- Dùng INTERNAL_API_KEY hoặc INTERNAL_SERVICE_TOKEN từ environment variables
- Mỗi request sang service khác phải gửi header:
  Authorization: Bearer <INTERNAL_SERVICE_TOKEN>
  X-Service-Name: ai-service

12. Cấu hình internal client qua application.yml hoặc environment variables:
- DOCUMENT_SERVICE_BASE_URL
- INTERNAL_SERVICE_TOKEN
- SERVICE_NAME=ai-service

13. Bắt buộc viết test cho API-service/client:
- Kiểm tra gọi đúng URL
- Kiểm tra có header Authorization Bearer token
- Kiểm tra có header X-Service-Name
- Kiểm tra mapping response đúng
- Kiểm tra lỗi document not found / internal error

14. Bắt buộc viết test cho controller/service:
- Validate request
- Success case
- Error case
- Save KetQuaAI đúng LoaiXuLyAI
- Semantic index tạo chunk đúng
- Delete index theo documentId
- Chatbot lấy chunk liên quan trước khi trả lời

15. Error codes cần dùng:
- AI_PROCESSING_FAILED
- DOCUMENT_NOT_FOUND
- AI_RESULT_NOT_FOUND
- OCR_FAILED
- SUMMARY_FAILED
- CLASSIFICATION_FAILED
- METADATA_EXTRACTION_FAILED
- SEMANTIC_SEARCH_FAILED
- CHATBOT_FAILED
- INVALID_FILE_FORMAT
- INTERNAL_SERVER_ERROR

16. Với phần AI thật, nếu project chưa có provider chính thức thì tạo interface:
- AiModelService
- EmbeddingService
- OcrService
  Sau đó implement mock/stub tạm thời để test pass, không hardcode logic vào controller.

17. Không để secret trong code.
    Không commit token thật.
    Không hardcode URL production.

18. Sau khi làm xong:
- Chạy toàn bộ test
- Báo cáo file đã tạo/sửa
- Báo cáo API đã hoàn thành
- Báo cáo test nào pass
- Nếu còn TODO, ghi rõ TODO nào và vì sao

Hãy bắt đầu bằng việc đọc cấu trúc project, đọc AGENTS.md, .agents, .codex, sau đó lập plan TDD trước khi code.