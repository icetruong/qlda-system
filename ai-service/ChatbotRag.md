# CHATBOT RAG TRONG HỆ THỐNG QUẢN LÝ VĂN BẢN

## 1. Mục tiêu

Chatbot RAG được xây dựng để hỗ trợ người dùng trong hệ thống quản lý văn bản.

Chatbot có 3 nhóm chức năng chính:

1. Tìm tài liệu bằng ngôn ngữ tự nhiên.
2. Hỏi số liệu hệ thống.
3. Hỏi hướng dẫn sử dụng hệ thống.

Trong đó, chức năng tìm tài liệu là RAG chính, sử dụng embedding và vector search.
Chức năng hỏi số liệu hệ thống không dùng vector search, mà sử dụng các API nội bộ hoặc SQL template cố định.

---

# 2. Nguyên tắc kiến trúc

Hệ thống sử dụng kiến trúc microservices.

Mỗi service chỉ được quản lý database của chính nó.

AI-service không được truy cập trực tiếp database của service khác.

Nếu AI-service cần dữ liệu thuộc service khác, AI-service phải gọi Internal API của service đó.

Ví dụ:

| Dữ liệu cần lấy | Service quản lý | Cách AI-service lấy dữ liệu |
|---|---|---|
| Văn bản | document-service | Gọi Internal API document-service |
| File đính kèm | document-service | Gọi Internal API document-service |
| Người dùng | auth-service | Gọi Internal API auth-service |
| Đơn vị | auth-service | Gọi Internal API auth-service |
| Quy trình xử lý | workflow-service | Gọi Internal API workflow-service |
| Kết quả AI | ai-service | Query DB của ai-service |
| Embedding/vector | ai-service | Query DB của ai-service |

Nguyên tắc này giúp:
- Tách biệt trách nhiệm từng service.
- Tránh phụ thuộc chặt giữa các service.
- Dễ scale.
- Dễ bảo trì.
- Không phá ownership dữ liệu.

---

# 3. Các chức năng của Chatbot RAG

## 3.1. Tìm tài liệu bằng ngôn ngữ tự nhiên

Đây là chức năng RAG chính.

Ví dụ người dùng hỏi:

```text
Tìm tài liệu về đào tạo người dùng
Hệ thống sẽ:
Tạo embedding từ câu hỏi.
Search vector trong bảng ai_document_chunk.
Lấy ra các chunk liên quan nhất.
Lấy metadata văn bản từ document-service nếu cần.
Build prompt.
Gọi LLM.
Trả kết quả cho người dùng.

3.2. Hỏi số liệu hệ thống
Chức năng này dùng để trả lời các câu hỏi dạng thống kê.
Ví dụ:
Tôi đã upload bao nhiêu văn bản?
Tôi có bao nhiêu văn bản sắp hết hạn?
Hệ thống có bao nhiêu người dùng?
Chức năng này không dùng vector search.
Hệ thống sẽ:
Phân loại intent câu hỏi.
Xác định metric cần truy vấn.
Kiểm tra quyền.
Gọi Internal API của service đang sở hữu dữ liệu.
Nhận kết quả.
Dùng LLM diễn giải kết quả thành câu trả lời dễ hiểu.
Lưu ý quan trọng:
Không cho LLM tự sinh SQL tự do.
Thay vào đó, backend phải dùng danh sách metric hoặc SQL template cố định.

3.3. Hỏi hướng dẫn sử dụng
Đây là chức năng optional.
Ví dụ:
Làm sao để tạo văn bản đến?
Hệ thống có thể xử lý bằng cách:
Index tài liệu hướng dẫn sử dụng vào ai_document_chunk.
Gắn metadata type = USER_GUIDE.
Khi người dùng hỏi hướng dẫn, chatbot search trong nhóm tài liệu hướng dẫn.
Trả lời dựa trên nội dung tìm được.

4. Chuẩn bị dữ liệu cho RAG
4.1. Nguồn dữ liệu dùng để index
AI-service cần lấy text từ các nguồn sau:
Nguồn dữ liệu
Service sở hữu
Cách lấy
VanBan.TrichYeu
document-service
Gọi /internal/documents/{id}
Nội dung văn bản
document-service
Gọi /internal/documents/{id}/content
File đính kèm
document-service
Gọi /internal/documents/{id}/attachments
OCR text
ai-service hoặc document-service tùy thiết kế
Nếu AI-service OCR thì lưu ở AI DB
Tài liệu hướng dẫn sử dụng
ai-service hoặc file seed riêng
Index vào ai_document_chunk


4.2. Bảng lưu embedding
AI-service quản lý bảng embedding riêng.
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ai_document_chunk (
   id BIGSERIAL PRIMARY KEY,
   van_ban_id BIGINT,
   tep_dinh_kem_id BIGINT,
   chunk_index INT,
   noi_dung TEXT NOT NULL,
   embedding VECTOR(768),
   metadata JSONB,
   ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_document_chunk_embedding
ON ai_document_chunk
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

CREATE INDEX idx_ai_document_chunk_van_ban
ON ai_document_chunk(van_ban_id);

CREATE INDEX idx_ai_document_chunk_metadata
ON ai_document_chunk USING GIN(metadata);

4.3. Ý nghĩa các cột
Cột
Ý nghĩa
id
ID chunk
van_ban_id
ID văn bản bên document-service
tep_dinh_kem_id
ID file đính kèm bên document-service
chunk_index
Thứ tự đoạn chunk
noi_dung
Nội dung text của chunk
embedding
Vector embedding
metadata
Thông tin phụ trợ
ngay_tao
Thời điểm tạo chunk


4.4. Metadata gợi ý
{
 "source": "TRICH_YEU",
 "documentType": "INCOMING",
 "soKyHieu": "123/CV-ABC",
 "trichYeu": "Tài liệu đào tạo người dùng hệ thống",
 "loaiVanBanId": 1,
 "donViChuTriId": 1,
 "type": "DOCUMENT"
}
Các giá trị source đề xuất:
Giá trị
Ý nghĩa
TRICH_YEU
Trích yếu văn bản
CONTENT
Nội dung văn bản
FILE
Nội dung file đính kèm
OCR
Nội dung OCR
USER_GUIDE
Tài liệu hướng dẫn sử dụng


5. Trong AI-service
Internal API
POST /internal/ai/index-document/{documentId}
Ví dụ docs chuẩn
5.1. Internal API index văn bản
POST /internal/ai/index-document/{documentId}
API này dùng để document-service yêu cầu AI-service index hoặc re-index văn bản.
Dùng khi:
Tạo văn bản mới
Upload file đính kèm
Cập nhật nội dung văn bản
OCR hoàn thành

Request Header
Authorization: Bearer <INTERNAL_SERVICE_TOKEN>
X-Service-Name: document-service

Path Variable
Biến
Ý nghĩa
documentId
ID văn bản cần index


Request
{
 "triggeredBy": "document-service"
}

Xử lý backend
AI-service sẽ:
Gọi document-service lấy metadata văn bản.
Gọi document-service lấy nội dung văn bản.
Gọi document-service lấy file đính kèm nếu cần.
Extract text hoặc OCR.
Chia chunk.
Tạo embedding.
Lưu vào ai_document_chunk.

Response
{
 "success": true,
 "message": "Index document successfully",
 "data": {
   "documentId": 1,
   "indexed": true,
   "totalChunks": 8
 }
}


6. Chức năng tìm tài liệu bằng RAG
6.1. Flow xử lý
Ví dụ người dùng hỏi:
Tìm tài liệu về đào tạo
Luồng xử lý:
User gửi câu hỏi
       ↓
AI-service detect intent = DOCUMENT_SEARCH
       ↓
Tạo embedding từ câu hỏi
       ↓
Search vector trong ai_document_chunk
       ↓
Lọc quyền truy cập
       ↓
Lấy top K chunk phù hợp
       ↓
Gọi document-service lấy thông tin văn bản nếu cần
       ↓
Build prompt với dữ liệu tìm được
       ↓
Gọi LLM
       ↓
Trả câu trả lời + danh sách nguồn

6.2. Query vector search mẫu
Vì ai_document_chunk thuộc AI-service, AI-service được query bảng này trực tiếp.
SELECT
   c.id AS chunk_id,
   c.van_ban_id,
   c.tep_dinh_kem_id,
   c.chunk_index,
   c.noi_dung,
   c.metadata,
   1 - (c.embedding <=> :queryEmbedding) AS score
FROM ai_document_chunk c
ORDER BY c.embedding <=> :queryEmbedding
LIMIT :topK;

6.3. Lọc quyền truy cập
AI-service không nên tự query bảng VanBan nếu bảng này thuộc document-service.
Có 2 cách đúng:
Cách 1: document-service cung cấp API kiểm tra quyền nhiều văn bản
AI-service search vector ra danh sách documentId, sau đó gọi document-service để lọc quyền.
POST /internal/documents/access-check
Request
{
 "userId": 2,
 "documentIds": [1, 2, 3, 4, 5]
}
Response
{
 "success": true,
 "message": "Check document access successfully",
 "data": {
   "allowedDocumentIds": [1, 3, 5]
 }
}

7. Chức năng hỏi số liệu hệ thống
7.1. Nguyên tắc
Chức năng hỏi số liệu không dùng vector search.
Không cho LLM tự sinh SQL tự do.
Backend phải dùng danh sách metric cố định.
Ví dụ:
Câu hỏi
Intent
Metric code
Service xử lý
Tôi đã upload bao nhiêu văn bản?
SYSTEM_STATISTIC
MY_UPLOADED_DOCUMENT_COUNT
document-service
Tôi có bao nhiêu văn bản sắp hết hạn?
SYSTEM_STATISTIC
MY_DUE_SOON_DOCUMENT_COUNT
workflow-service
Tôi có bao nhiêu văn bản quá hạn?
SYSTEM_STATISTIC
MY_OVERDUE_DOCUMENT_COUNT
workflow-service
Hệ thống có bao nhiêu người dùng?
SYSTEM_STATISTIC
TOTAL_USER_COUNT
auth-service
Hệ thống có bao nhiêu văn bản?
SYSTEM_STATISTIC
TOTAL_DOCUMENT_COUNT
document-service


7.2. Flow xử lý hỏi số liệu
User hỏi số liệu
       ↓
AI-service detect intent = SYSTEM_STATISTIC
       ↓
AI-service xác định metricCode
       ↓
Kiểm tra quyền hỏi metric
       ↓
Gọi Internal API của service sở hữu dữ liệu
       ↓
Nhận số liệu
       ↓
LLM diễn giải kết quả
       ↓
Trả câu trả lời

7.3. Ví dụ metric document-service
Người dùng hỏi: “Tôi đã upload bao nhiêu văn bản?”
AI-service gọi:
GET /internal/documents/statistics/my-uploaded-count?userId=2
Response:
{
 "success": true,
 "message": "Get my uploaded document count successfully",
 "data": {
   "userId": 2,
   "count": 12
 }
}
Chatbot trả lời:
Bạn đã upload 12 văn bản.

7.4. Ví dụ metric workflow-service
Người dùng hỏi: “Tôi có bao nhiêu văn bản sắp hết hạn?”
AI-service gọi:
GET /internal/workflows/statistics/my-due-soon-count?userId=2&days=3
Response:
{
 "success": true,
 "message": "Get due soon task count successfully",
 "data": {
   "userId": 2,
   "days": 3,
   "count": 5
 }
}
Chatbot trả lời:
Bạn có 5 văn bản sắp hết hạn trong 3 ngày tới.

7.5. Ví dụ metric auth-service
Admin hỏi: “Hệ thống có bao nhiêu người dùng?”
AI-service gọi:
GET /internal/auth/statistics/users/count
Response:
{
 "success": true,
 "message": "Get user count successfully",
 "data": {
   "count": 120
 }
}
Chatbot trả lời:
Hệ thống hiện có 120 người dùng.
Lưu ý:
Chỉ admin hoặc người có quyền quản trị mới được hỏi metric này.

8. Chức năng hỏi hướng dẫn sử dụng
8.1. Nguồn dữ liệu
Có thể index các tài liệu hướng dẫn như:
Hướng dẫn tạo văn bản đến.
Hướng dẫn xử lý văn bản.
Hướng dẫn trình ký.
Hướng dẫn tra cứu.
Hướng dẫn quản lý hồ sơ.
Các tài liệu này được lưu vào ai_document_chunk với metadata:
{
 "type": "huong_dan ",
 "module": "DOCUMENT",
 "title": "Hướng dẫn tạo văn bản đến"
}

8.2. Flow xử lý
User hỏi hướng dẫn
       ↓
AI-service detect intent = huong_dan 
       ↓
Vector search trong ai_document_chunk
       ↓
Lọc metadata type = huong_dan 
       ↓
Lấy top K chunk
       ↓
Build prompt
       ↓
LLM trả lời

9. API chatbot chính
9.1. Gửi câu hỏi cho chatbot
POST /api/ai/chatbot/ask
Request
{
 "userId": 2,
 "question": "Tìm tài liệu về đào tạo người dùng",
 "context": {
   "module": "DOCUMENT",
   "documentId": null
 }
}
Response với intent tìm tài liệu
{
 "success": true,
 "message": "Chatbot response successfully",
 "data": {
   "resultId": 1,
   "intent": "DOCUMENT_SEARCH",
   "question": "Tìm tài liệu về đào tạo người dùng",
   "answer": "Tôi tìm thấy 2 tài liệu liên quan đến đào tạo người dùng.",
   "sources": [
     {
       "documentId": 1,
       "chunkId": 10,
       "score": 0.92,
       "title": "Tài liệu hướng dẫn đào tạo người dùng hệ thống",
       "matchedText": "Tài liệu hướng dẫn đào tạo người dùng hệ thống quản lý văn bản."
     }
   ],
   "modelUsed": "gpt-4.1",
   "confidence": 91.0
 }
}
Response với intent hỏi số liệu
{
 "success": true,
 "message": "Chatbot response successfully",
 "data": {
   "resultId": 2,
   "intent": "SYSTEM_STATISTIC",
   "metricCode": "MY_UPLOADED_DOCUMENT_COUNT",
   "question": "Tôi đã upload bao nhiêu văn bản?",
   "answer": "Bạn đã upload 12 văn bản.",
   "value": 12,
   "modelUsed": "gpt-4.1",
   "confidence": 96.0
 }
}
Response với intent hỏi hướng dẫn
{
 "success": true,
 "message": "Chatbot response successfully",
 "data": {
   "resultId": 3,
   "intent": "USER_GUIDE",
   "question": "Làm sao để tạo văn bản đến?",
   "answer": "Bạn vào menu Văn bản đến, chọn Tạo mới, nhập thông tin văn bản và bấm Lưu.",
   "sources": [
     {
       "chunkId": 20,
       "title": "Hướng dẫn tạo văn bản đến",
       "reference": "USER_GUIDE"
     }
   ],
   "modelUsed": "gpt-4.1",
   "confidence": 90.0
 }
}

10. Intent detect
10.1. Danh sách intent
Intent
Ý nghĩa
DOCUMENT_SEARCH
Tìm tài liệu/văn bản
SYSTEM_STATISTIC
Hỏi số liệu hệ thống
USER_GUIDE
Hỏi hướng dẫn sử dụng
GENERAL_HELP
Câu hỏi khác


10.2. Ví dụ phân loại intent
Câu hỏi
Intent
Tìm văn bản về đào tạo
DOCUMENT_SEARCH
Cho tôi tài liệu liên quan Office 365
DOCUMENT_SEARCH
Tôi đã upload bao nhiêu văn bản?
SYSTEM_STATISTIC
Có bao nhiêu văn bản sắp hết hạn?
SYSTEM_STATISTIC
Làm sao tạo văn bản đến?
USER_GUIDE
Cách trình ký văn bản?
USER_GUIDE


11. Prompt chuẩn
11.1. System prompt
Bạn là trợ lý AI của hệ thống quản lý văn bản.

Chỉ trả lời dựa trên dữ liệu được cung cấp trong context.
Không tự bịa thông tin.
Nếu không tìm thấy dữ liệu phù hợp, hãy trả lời: "Không tìm thấy dữ liệu phù hợp."

Trả lời bằng tiếng Việt.
Trả lời rõ ràng, ngắn gọn, dễ hiểu.
Không hiển thị thông tin kỹ thuật nội bộ cho người dùng cuối.

11.2. Prompt cho tìm tài liệu
Nhiệm vụ:
Người dùng đang muốn tìm tài liệu/văn bản trong hệ thống.

Câu hỏi người dùng:
{question}

Dữ liệu tìm được:
{retrieved_documents}

Hãy trả lời:
- Tóm tắt ngắn gọn kết quả tìm được.
- Liệt kê các văn bản phù hợp nhất.
- Không nhắc đến vector, embedding, chunk.
- Nếu không có dữ liệu, trả lời "Không tìm thấy tài liệu phù hợp."

11.3. Prompt cho hỏi số liệu
Nhiệm vụ:
Người dùng đang hỏi số liệu hệ thống.

Câu hỏi người dùng:
{question}

Metric:
{metricCode}

Kết quả truy vấn:
{metricResult}

Hãy trả lời tự nhiên, rõ ràng, dễ hiểu.
Không tự suy diễn thêm ngoài dữ liệu được cung cấp.

11.4. Prompt cho hướng dẫn sử dụng
Nhiệm vụ:
Người dùng đang hỏi hướng dẫn sử dụng hệ thống.

Câu hỏi người dùng:
{question}

Tài liệu hướng dẫn tìm được:
{guide_context}

Hãy hướng dẫn từng bước ngắn gọn, dễ hiểu.
Nếu không có dữ liệu hướng dẫn, trả lời "Không tìm thấy hướng dẫn phù hợp."

12. Lưu kết quả chatbot
Mỗi lần chatbot trả lời, AI-service lưu kết quả vào bảng KetQuaAI.
Mapping database
Cột KetQuaAI
Giá trị
VanBanID
context.documentId, nếu có
NguoiYeuCauID
userId
LoaiXuLyAI
CHATBOT
NoiDungDauVao
question
KetQuaTraVe
answer
DoTinCay
confidence
ModelSuDung
modelUsed
GhiChu
intent, metricCode, sources


13. Internal API cần bổ sung cho các service
13.1. document-service
Kiểm tra quyền truy cập nhiều văn bản
POST /internal/documents/access-check
Request:
{
 "userId": 2,
 "documentIds": [1, 2, 3]
}
Response:
{
 "success": true,
 "message": "Check document access successfully",
 "data": {
   "allowedDocumentIds": [1, 3]
 }
}

Lấy thống kê số văn bản người dùng đã upload
GET /internal/documents/statistics/my-uploaded-count?userId=2
Response:
{
 "success": true,
 "message": "Get my uploaded document count successfully",
 "data": {
   "userId": 2,
   "count": 12
 }
}

Lấy thống kê tổng số văn bản
GET /internal/documents/statistics/total-count
Response:
{
 "success": true,
 "message": "Get total document count successfully",
 "data": {
   "count": 250
 }
}

13.2. workflow-service
Lấy số văn bản sắp hết hạn của người dùng
GET /internal/workflows/statistics/my-due-soon-count?userId=2&days=3
Response:
{
 "success": true,
 "message": "Get my due soon document count successfully",
 "data": {
   "userId": 2,
   "days": 3,
   "count": 5
 }
}

Lấy số văn bản quá hạn của người dùng
GET /internal/workflows/statistics/my-overdue-count?userId=2
Response:
{
 "success": true,
 "message": "Get my overdue document count successfully",
 "data": {
   "userId": 2,
   "count": 2
 }
}

13.3. auth-service
Lấy tổng số người dùng
GET /internal/auth/statistics/users/count
Response:
{
 "success": true,
 "message": "Get user count successfully",
 "data": {
   "count": 120
 }
}

Kiểm tra quyền dùng metric admin
POST /internal/auth/permissions/check
Request:
{
 "userId": 1,
 "maChucNang": "SYSTEM_STATISTIC",
 "permission": "IsView"
}
Response:
{
 "success": true,
 "message": "Check permission successfully",
 "data": {
   "allowed": true
 }
}

14. MVP cần làm
MVP chatbot RAG chỉ cần làm các chức năng sau:
STT
Chức năng
Bắt buộc
1
Index văn bản vào ai_document_chunk
Có
2
Tìm tài liệu theo nội dung tự nhiên
Có
3
Tìm văn bản theo từ khóa tự nhiên
Có
4
Hỏi “Tôi đã upload bao nhiêu văn bản?”
Có
5
Hỏi “Tôi có bao nhiêu văn bản sắp hết hạn?”
Có
6
Admin hỏi “Hệ thống có bao nhiêu người dùng?”
Có
7
Hỏi hướng dẫn sử dụng
Optional


15. Hiểu đúng về RAG
RAG không phải là đọc toàn bộ database.
RAG là:
Lấy dữ liệu liên quan
       ↓
Đưa dữ liệu đó vào prompt
       ↓
LLM trả lời dựa trên dữ liệu được cung cấp
Đối với hệ thống này:
Tìm tài liệu dùng vector search.
Hỏi số liệu dùng Internal API hoặc SQL template cố định.
Hỏi hướng dẫn dùng RAG trên tài liệu hướng dẫn.
Không cho LLM tự query SQL tùy ý.
Không cho AI-service query trực tiếp database của service khác.


