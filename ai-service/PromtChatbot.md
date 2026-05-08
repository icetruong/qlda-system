Bạn là AI coding agent trong project QLDA, module hiện tại là ai-service.

Nhiệm vụ:
Triển khai cụ thể API chatbot:
POST /api/ai/chatbot/ask

Toàn bộ yêu cầu nghiệp vụ đã được ghi trong file ChatbotRag.md. Hãy đọc kỹ file này trước khi code.

Yêu cầu bắt buộc:
1. Trước tiên đọc các file:
    - AGENTS.md
    - .agents/*
    - .codex/*
    - ChatbotRag.md

2. Tuân thủ TDD tuyệt đối:
    - Lập plan ngắn gọn
    - Viết test fail trước
    - Implement tối thiểu để test pass
    - Refactor
    - Chạy lại test
    - Báo cáo RED → GREEN → REFACTOR

3. Chỉ xử lý phạm vi:
   POST /api/ai/chatbot/ask

Không làm lan sang các API AI khác nếu không cần.

Request:
{
"userId": 2,
"question": "Tìm tài liệu về đào tạo người dùng",
"context": {
"module": "DOCUMENT",
"documentId": null
}
}

Response chung:
{
"success": true,
"message": "Chatbot response successfully",
"data": { ... }
}

Khi lỗi:
{
"success": false,
"message": "...",
"errorCode": "CHATBOT_FAILED"
}

Kiến trúc cần triển khai:
- ChatbotController
- ChatbotService
- ChatbotIntentDetector
- ChatbotPromptBuilder
- ChatbotResultMapper
- AiResult/KetQuaAI service hoặc repository
- AiDocumentChunk repository/service
- Internal API service/client cho document-service, workflow-service, auth-service
- LLM service interface
- Embedding service interface

Không được xử lý logic trực tiếp trong controller.

Các intent cần hỗ trợ:
1. DOCUMENT_SEARCH
2. SYSTEM_STATISTIC
3. USER_GUIDE
4. GENERAL_HELP

Luồng xử lý chung:
1. Validate request:
    - userId bắt buộc
    - question bắt buộc, không blank
    - context có thể null
    - context.documentId optional

2. Detect intent từ question:
    - “Tìm văn bản...”, “Tìm tài liệu...”, “Cho tôi tài liệu liên quan...” => DOCUMENT_SEARCH
    - “Tôi đã upload bao nhiêu văn bản?” => SYSTEM_STATISTIC / MY_UPLOADED_DOCUMENT_COUNT
    - “Tôi có bao nhiêu văn bản sắp hết hạn?” => SYSTEM_STATISTIC / MY_DUE_SOON_DOCUMENT_COUNT
    - “Tôi có bao nhiêu văn bản quá hạn?” => SYSTEM_STATISTIC / MY_OVERDUE_DOCUMENT_COUNT
    - “Hệ thống có bao nhiêu người dùng?” => SYSTEM_STATISTIC / TOTAL_USER_COUNT
    - “Làm sao tạo văn bản đến?”, “Cách trình ký văn bản?” => USER_GUIDE
    - Không khớp => GENERAL_HELP

3. Với DOCUMENT_SEARCH:
    - Tạo embedding từ question
    - Search vector trong bảng ai_document_chunk
    - Lấy top K chunk liên quan
    - Không query trực tiếp DB của document-service
    - Nếu cần metadata văn bản thì gọi document-service internal API
    - Nếu cần lọc quyền thì gọi:
      POST /internal/documents/access-check
    - Build prompt theo ChatbotRag.md
    - Gọi LLM sinh answer
    - Trả response gồm:
      resultId, intent, question, answer, sources, modelUsed, confidence

4. Với SYSTEM_STATISTIC:
    - Không dùng vector search
    - Không cho LLM tự sinh SQL
    - Chỉ dùng metric cố định
    - Map metric:
      MY_UPLOADED_DOCUMENT_COUNT:
      GET /internal/documents/statistics/my-uploaded-count?userId={userId}

      MY_DUE_SOON_DOCUMENT_COUNT:
      GET /internal/workflows/statistics/my-due-soon-count?userId={userId}&days=3

      MY_OVERDUE_DOCUMENT_COUNT:
      GET /internal/workflows/statistics/my-overdue-count?userId={userId}

      TOTAL_DOCUMENT_COUNT:
      GET /internal/documents/statistics/total-count

      TOTAL_USER_COUNT:
      Trước khi gọi, kiểm tra quyền:
      POST /internal/auth/permissions/check
      body:
      {
      "userId": userId,
      "maChucNang": "SYSTEM_STATISTIC",
      "permission": "IsView"
      }

      Nếu allowed = true thì gọi:
      GET /internal/auth/statistics/users/count

    - Sau khi nhận số liệu, build prompt diễn giải tự nhiên
    - Trả response gồm:
      resultId, intent, metricCode, question, answer, value, modelUsed, confidence

5. Với USER_GUIDE:
    - Tạo embedding từ question
    - Search trong ai_document_chunk
    - Chỉ lấy chunk có metadata type = USER_GUIDE hoặc huong_dan
    - Build prompt hướng dẫn sử dụng
    - Gọi LLM
    - Trả response gồm:
      resultId, intent, question, answer, sources, modelUsed, confidence

6. Với GENERAL_HELP:
    - Trả lời an toàn theo system prompt
    - Không bịa dữ liệu
    - Nếu không có context phù hợp, trả:
      "Không tìm thấy dữ liệu phù hợp."

7. Prompt system bắt buộc:
   Bạn là trợ lý AI của hệ thống quản lý văn bản.
   Chỉ trả lời dựa trên dữ liệu được cung cấp trong context.
   Không tự bịa thông tin.
   Nếu không tìm thấy dữ liệu phù hợp, hãy trả lời: "Không tìm thấy dữ liệu phù hợp."
   Trả lời bằng tiếng Việt.
   Trả lời rõ ràng, ngắn gọn, dễ hiểu.
   Không hiển thị thông tin kỹ thuật nội bộ cho người dùng cuối.

8. Lưu kết quả vào bảng KetQuaAI sau mỗi lần trả lời:
    - VanBanID = context.documentId nếu có
    - NguoiYeuCauID = userId
    - LoaiXuLyAI = CHATBOT
    - NoiDungDauVao = question
    - KetQuaTraVe = answer
    - DoTinCay = confidence
    - ModelSuDung = modelUsed
    - GhiChu = JSON string chứa intent, metricCode nếu có, sources nếu có

9. Internal API service-to-service:
   Tất cả request sang document-service, workflow-service, auth-service phải gửi:
   Authorization: Bearer <INTERNAL_SERVICE_TOKEN>
   X-Service-Name: ai-service

   Không dùng JWT Azure AD của user cho service-to-service.
   Token lấy từ environment variable.

10. Cần cấu hình:
- DOCUMENT_SERVICE_BASE_URL
- WORKFLOW_SERVICE_BASE_URL
- AUTH_SERVICE_BASE_URL
- INTERNAL_SERVICE_TOKEN
- SERVICE_NAME=ai-service
- CHATBOT_TOP_K=5

11. Test bắt buộc:
    Controller test:
- request hợp lệ trả 200
- question blank trả lỗi validation
- userId null trả lỗi validation
- service throw exception trả CHATBOT_FAILED

Service test:
- DOCUMENT_SEARCH gọi embedding service
- DOCUMENT_SEARCH search ai_document_chunk
- DOCUMENT_SEARCH có gọi access-check nếu có documentId
- DOCUMENT_SEARCH lưu KetQuaAI với LoaiXuLyAI = CHATBOT
- SYSTEM_STATISTIC câu “Tôi đã upload bao nhiêu văn bản?” gọi đúng document-service metric
- SYSTEM_STATISTIC câu “Tôi có bao nhiêu văn bản sắp hết hạn?” gọi đúng workflow-service metric
- SYSTEM_STATISTIC câu “Hệ thống có bao nhiêu người dùng?” phải check permission trước
- USER_GUIDE chỉ search chunk metadata type USER_GUIDE/huong_dan
- GENERAL_HELP không tự bịa dữ liệu
- mọi response đều có resultId sau khi lưu DB

Internal client test:
- gọi đúng URL
- có Authorization Bearer token
- có X-Service-Name
- map response đúng
- xử lý lỗi internal API đúng

12. Không hardcode answer trong controller.
    Nếu chưa có LLM provider thật, tạo interface ChatbotLlmService và mock/stub implementation để test pass.

13. Không hardcode secret.
    Không query trực tiếp database của service khác.
    Không để LLM tự sinh SQL.
    Không expose thông tin vector, embedding, chunk cho người dùng cuối.

Sau khi hoàn thành:
- Chạy toàn bộ test liên quan chatbot
- Báo cáo file đã tạo/sửa
- Báo cáo test đã pass
- Báo cáo API POST /api/ai/chatbot/ask đã xử lý được intent nào
- Ghi rõ TODO nếu phần nào cần service khác cung cấp thêm internal API