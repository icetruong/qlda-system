PHẦN IV. API AI AGENT
Phần này tương ứng chức năng 27 → 32 trong danh sách 43 chức năng của bạn.
1. Quy ước chung
   Base URL
   /api/ai
   Service phụ trách:
   ai-service
2. Authentication
   Tất cả API đều yêu cầu header:
   Authorization: Bearer <access_token>
3. Định dạng dữ liệu
   Request JSON
   Content-Type: application/json
   Upload file
   Content-Type: multipart/form-data
4. Cấu trúc Response chung
   Thành công
   {
   "success": true,
   "message": "Request processed successfully",
   "data": {}
   }
   Lỗi
   {
   "success": false,
   "message": "Invalid request",
   "errorCode": "INVALID_REQUEST"
   }
5. Error Codes
   Error Code
   Ý nghĩa
   AI_PROCESSING_FAILED
   Xử lý AI thất bại
   DOCUMENT_NOT_FOUND
   Không tìm thấy văn bản
   AI_RESULT_NOT_FOUND
   Không tìm thấy kết quả AI
   OCR_FAILED
   OCR thất bại
   SUMMARY_FAILED
   Tóm tắt thất bại
   CLASSIFICATION_FAILED
   Phân loại văn bản thất bại
   METADATA_EXTRACTION_FAILED
   Trích xuất metadata thất bại
   SEMANTIC_SEARCH_FAILED
   Tìm kiếm thông minh thất bại
   CHATBOT_FAILED
   Chatbot phản hồi thất bại
   INVALID_FILE_FORMAT
   Định dạng file không hợp lệ
   INTERNAL_SERVER_ERROR
   Lỗi hệ thống


6. API chi tiết

6.1. Tóm tắt văn bản
6.1.1. Tóm tắt văn bản theo nội dung
POST /api/ai/summarize
API này dùng để tóm tắt nội dung văn bản do client gửi lên.
Sau khi xử lý thành công, kết quả được lưu vào bảng KetQuaAI với:
LoaiXuLyAI = SUMMARY
Request
{
"documentId": 1,
"userId": 2,
"text": "Nội dung văn bản cần tóm tắt...",
"summaryType": "SHORT",
"language": "vi"
}
Quy ước summaryType
Giá trị
Ý nghĩa
SHORT
Tóm tắt ngắn
DETAILED
Tóm tắt chi tiết
BULLET
Tóm tắt dạng gạch đầu dòng

Response
{
"success": true,
"message": "Summarize document successfully",
"data": {
"resultId": 1,
"documentId": 1,
"summaryType": "SHORT",
"summary": "Văn bản đề cập đến việc triển khai hệ thống xử lý văn bản điện tử tích hợp Office 365.",
"modelUsed": "gpt-4.1",
"confidence": 91.5
}
}
Mapping database
Cột bảng KetQuaAI
Giá trị
VanBanID
documentId
NguoiYeuCauID
userId
LoaiXuLyAI
SUMMARY
NoiDungDauVao
text
KetQuaTraVe
summary
DoTinCay
confidence
ModelSuDung
modelUsed
GhiChu
summaryType, language nếu cần


6.1.2. Tóm tắt văn bản từ file
POST /api/ai/summarize/file
API này dùng để upload file, trích xuất nội dung và tóm tắt bằng AI.
Sau khi xử lý thành công, kết quả được lưu vào bảng KetQuaAI.
Request
Content-Type: multipart/form-data
Field
Kiểu dữ liệu
Bắt buộc
Ý nghĩa
documentId
Number
Có
ID văn bản
userId
Number
Có
ID người yêu cầu
summaryType
String
Có
SHORT, DETAILED, BULLET
language
String
Không
Ngôn ngữ, mặc định vi
file
File
Có
File cần tóm tắt

Response
{
"success": true,
"message": "Summarize file successfully",
"data": {
"resultId": 2,
"documentId": 1,
"fileName": "van-ban.pdf",
"summaryType": "SHORT",
"summary": "Văn bản trình bày nội dung triển khai hệ thống xử lý văn bản điện tử.",
"modelUsed": "gpt-4.1",
"confidence": 89.7
}
}

6.2. Phân loại văn bản
6.2.1. Phân loại văn bản theo nội dung
POST /api/ai/classify
API này dùng để phân loại văn bản theo danh sách loại văn bản được truyền vào.
Sau khi xử lý thành công, kết quả được lưu vào bảng KetQuaAI với:
LoaiXuLyAI = CLASSIFICATION
Request
{
"documentId": 1,
"userId": 2,
"text": "Nội dung văn bản cần phân loại...",
"categories": [
"CONG_VAN",
"QUYET_DINH",
"THONG_BAO",
"BAO_CAO",
"KE_HOACH"
],
"language": "vi"
}
Response
{
"success": true,
"message": "Classify document successfully",
"data": {
"resultId": 3,
"documentId": 1,
"category": "CONG_VAN",
"categoryName": "Công văn",
"confidence": 94.2,
"reason": "Nội dung có cấu trúc và ngôn ngữ hành chính tương ứng với công văn.",
"modelUsed": "gpt-4.1"
}
}
Mapping database
Cột bảng KetQuaAI
Giá trị
VanBanID
documentId
NguoiYeuCauID
userId
LoaiXuLyAI
CLASSIFICATION
NoiDungDauVao
text
KetQuaTraVe
category hoặc JSON kết quả phân loại
DoTinCay
confidence
ModelSuDung
modelUsed
GhiChu
reason nếu cần


6.2.2. Phân loại văn bản từ file
POST /api/ai/classify/file
API này dùng để upload file, trích xuất nội dung và phân loại văn bản.
Request
Content-Type: multipart/form-data
Field
Kiểu dữ liệu
Bắt buộc
Ý nghĩa
documentId
Number
Có
ID văn bản
userId
Number
Có
ID người yêu cầu
language
String
Không
Ngôn ngữ, mặc định vi
file
File
Có
File cần phân loại

Response
{
"success": true,
"message": "Classify file successfully",
"data": {
"resultId": 4,
"documentId": 1,
"fileName": "van-ban.pdf",
"category": "CONG_VAN",
"categoryName": "Công văn",
"confidence": 92.8,
"modelUsed": "gpt-4.1"
}
}

6.3. Trích xuất dữ liệu metadata
6.3.1. Trích xuất metadata từ nội dung văn bản
POST /api/ai/metadata/extract
API này dùng để trích xuất thông tin metadata từ nội dung văn bản.
Sau khi xử lý thành công, kết quả được lưu vào bảng KetQuaAI với:
LoaiXuLyAI = METADATA_EXTRACTION
Request
{
"documentId": 1,
"userId": 2,
"text": "Nội dung văn bản cần trích xuất...",
"fields": [
"soKyHieu",
"ngayVanBan",
"donViBanHanh",
"nguoiKy",
"doKhan",
"doMat",
"hanXuLy"
],
"language": "vi"
}
Response
{
"success": true,
"message": "Extract metadata successfully",
"data": {
"resultId": 5,
"documentId": 1,
"metadata": {
"soKyHieu": "123/CV-ABC",
"ngayVanBan": "2026-04-30",
"donViBanHanh": "Sở Thông tin và Truyền thông",
"nguoiKy": "Nguyễn Văn A",
"doKhan": "KHAN",
"doMat": "THUONG",
"hanXuLy": "2026-05-10"
},
"confidence": 90.6,
"modelUsed": "gpt-4.1"
}
}
Lưu ý lưu database
Do cột KetQuaTraVe trong bảng KetQuaAI là TEXT, metadata cần được lưu dưới dạng JSON string.
Ví dụ:
"{\"soKyHieu\":\"123/CV-ABC\",\"ngayVanBan\":\"2026-04-30\"}"

6.3.2. Trích xuất metadata từ file
POST /api/ai/metadata/extract/file
API này dùng để upload file, OCR hoặc trích xuất text, sau đó lấy metadata bằng AI.
Request
Content-Type: multipart/form-data
Field
Kiểu dữ liệu
Bắt buộc
Ý nghĩa
documentId
Number
Có
ID văn bản
userId
Number
Có
ID người yêu cầu
language
String
Không
Ngôn ngữ, mặc định vi
file
File
Có
File cần trích xuất

Response
{
"success": true,
"message": "Extract metadata from file successfully",
"data": {
"resultId": 6,
"documentId": 1,
"fileName": "van-ban.pdf",
"metadata": {
"soKyHieu": "123/CV-ABC",
"ngayVanBan": "2026-04-30",
"donViBanHanh": "Sở Thông tin và Truyền thông",
"nguoiKy": "Nguyễn Văn A"
},
"confidence": 88.9,
"modelUsed": "gpt-4.1"
}
}

6.4. Tìm kiếm thông minh
6.4.1. Tìm kiếm semantic
POST /api/ai/search/semantic
API này dùng để tìm kiếm văn bản theo ngữ nghĩa.
Cơ chế:
Tạo embedding cho keyword
So sánh với cột embedding trong bảng ai_document_chunk
Trả về các chunk phù hợp nhất
Request
{
"keyword": "văn bản liên quan đến triển khai Office 365",
"userId": 2,
"filters": {
"documentId": null,
"loaiVanBanId": 1,
"donViId": 1,
"fromDate": "2026-01-01",
"toDate": "2026-04-30"
},
"page": 0,
"size": 10
}
Response
{
"success": true,
"message": "Semantic search successfully",
"data": {
"content": [
{
"documentId": 1,
"chunkId": 12,
"chunkIndex": 0,
"score": 0.92,
"matchedText": "triển khai hệ thống xử lý văn bản điện tử tích hợp Office 365",
"metadata": {
"soKyHieu": "123/CV-ABC",
"loaiVanBanId": 1,
"donViId": 1,
"type": "office365"
}
}
],
"page": 0,
"size": 10,
"totalElements": 1
}
}

6.4.2. Tạo index semantic cho văn bản
POST /api/ai/search/index-document
API này dùng để đưa nội dung văn bản vào kho tìm kiếm semantic.
Cơ chế:
Nhận nội dung văn bản
Chia nội dung thành nhiều chunk
Tạo embedding cho từng chunk
Lưu từng chunk vào bảng ai_document_chunk
Request
{
"documentId": 1,
"attachmentId": null,
"text": "Nội dung văn bản cần đưa vào kho tìm kiếm semantic...",
"metadata": {
"soKyHieu": "123/CV-ABC",
"trichYeu": "Văn bản triển khai hệ thống xử lý văn bản điện tử",
"loaiVanBanId": 1,
"donViId": 1
}
}
Response
{
"success": true,
"message": "Index document successfully",
"data": {
"documentId": 1,
"attachmentId": null,
"totalChunks": 8,
"indexed": true
}
}
Mapping database
Cột bảng ai_document_chunk
Giá trị
van_ban_id
documentId
tep_dinh_kem_id
attachmentId
chunk_index
Thứ tự chunk
noi_dung
Nội dung chunk
embedding
Vector embedding
metadata
Metadata JSON
ngay_tao
Thời điểm tạo


6.4.3. Xóa index semantic của văn bản
DELETE /api/ai/search/index-document/{documentId}
API này dùng để xóa toàn bộ chunk semantic search của một văn bản.
Response
{
"success": true,
"message": "Delete document index successfully",
"data": {
"documentId": 1,
"deleted": true
}
}

6.5. Gợi ý xử lý và phản hồi
6.5.1. Gợi ý hướng xử lý văn bản
POST /api/ai/suggestions/handling
API này dùng để AI gợi ý hướng xử lý văn bản.
Sau khi xử lý thành công, kết quả được lưu vào bảng KetQuaAI với:
LoaiXuLyAI = SUGGESTION_HANDLING
Request
{
"documentId": 1,
"userId": 2,
"text": "Nội dung văn bản cần xử lý...",
"context": {
"loaiVanBan": "CONG_VAN",
"doKhan": "KHAN",
"donViChuTri": "Phòng Hành chính"
}
}
Response
{
"success": true,
"message": "Generate handling suggestion successfully",
"data": {
"resultId": 7,
"documentId": 1,
"suggestions": [
{
"action": "CHUYEN_XU_LY",
"description": "Chuyển văn bản cho Phòng Hành chính xử lý trong vòng 24 giờ.",
"priority": "HIGH"
},
{
"action": "GUI_THONG_BAO",
"description": "Gửi thông báo nhắc việc cho chuyên viên phụ trách.",
"priority": "MEDIUM"
}
],
"confidence": 87.3,
"modelUsed": "gpt-4.1"
}
}
Lưu ý lưu database
Vì KetQuaTraVe là TEXT, danh sách suggestions nên lưu dưới dạng JSON string.

6.5.2. Gợi ý nội dung phản hồi
POST /api/ai/suggestions/reply
API này dùng để AI gợi ý nội dung phản hồi văn bản.
Sau khi xử lý thành công, kết quả được lưu vào bảng KetQuaAI với:
LoaiXuLyAI = SUGGESTION_REPLY
Request
{
"documentId": 1,
"userId": 2,
"text": "Nội dung văn bản cần phản hồi...",
"replyStyle": "HANH_CHINH",
"language": "vi"
}
Response
{
"success": true,
"message": "Generate reply suggestion successfully",
"data": {
"resultId": 8,
"documentId": 1,
"suggestedReply": "Kính gửi..., căn cứ nội dung văn bản, đơn vị xin phản hồi như sau...",
"replyStyle": "HANH_CHINH",
"modelUsed": "gpt-4.1",
"confidence": 86.5
}
}

6.6. Chatbot hỗ trợ người dùng
6.6.1. Gửi câu hỏi cho chatbot
POST /api/ai/chatbot/ask
API này dùng để người dùng gửi câu hỏi cho chatbot.
Chatbot sử dụng dữ liệu từ bảng ai_document_chunk để tìm thông tin liên quan và sinh câu trả lời.
Hệ thống không lưu lịch sử hội thoại. Mỗi câu hỏi được xử lý độc lập.
Sau khi xử lý thành công, kết quả được lưu vào bảng KetQuaAI với:
LoaiXuLyAI = CHATBOT
Request
{
"userId": 2,
"question": "Làm sao để tạo văn bản đến?",
"context": {
"module": "DOCUMENT",
"documentId": null
}
}
Response
{
"success": true,
"message": "Chatbot response successfully",
"data": {
"resultId": 9,
"question": "Làm sao để tạo văn bản đến?",
"answer": "Bạn vào menu Văn bản đến, chọn Tạo mới, sau đó nhập thông tin văn bản và bấm Lưu.",
"modelUsed": "gpt-4.1",
"confidence": 88.5,
"sources": [
{
"chunkId": 1,
"documentId": 1,
"chunkIndex": 0,
"reference": "ai_document_chunk",
"matchedText": "Tài liệu hướng dẫn đào tạo người dùng hệ thống quản lý văn bản."
}
]
}
}
Mapping database
Cột bảng KetQuaAI
Giá trị
VanBanID
context.documentId nếu có
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
module hoặc sources nếu cần


6.7. Quản lý kết quả AI
6.7.1. Xem danh sách kết quả AI theo văn bản
GET /api/ai/results/documents/{documentId}
API này dùng để lấy danh sách kết quả AI đã xử lý theo văn bản.
Query Params
Tham số
Bắt buộc
Ý nghĩa
loaiXuLyAI
Không
Lọc theo loại xử lý AI
page
Không
Trang hiện tại
size
Không
Số lượng bản ghi mỗi trang

Ví dụ
GET /api/ai/results/documents/1?loaiXuLyAI=SUMMARY&page=0&size=10
Response
{
"success": true,
"message": "Get AI results successfully",
"data": {
"content": [
{
"id": 1,
"documentId": 1,
"userId": 2,
"loaiXuLyAI": "SUMMARY",
"ketQuaTraVe": "Văn bản đề cập đến việc triển khai hệ thống xử lý văn bản điện tử.",
"confidence": 91.5,
"modelUsed": "gpt-4.1",
"processedAt": "2026-04-30T10:00:00"
}
],
"page": 0,
"size": 10,
"totalElements": 1
}
}

6.7.2. Xem chi tiết kết quả AI
GET /api/ai/results/{id}
API này dùng để xem chi tiết một kết quả AI đã lưu.
Response
{
"success": true,
"message": "Get AI result detail successfully",
"data": {
"id": 1,
"documentId": 1,
"userId": 2,
"loaiXuLyAI": "SUMMARY",
"noiDungDauVao": "Nội dung văn bản cần tóm tắt...",
"ketQuaTraVe": "Văn bản đề cập đến việc triển khai hệ thống xử lý văn bản điện tử.",
"confidence": 91.5,
"modelUsed": "gpt-4.1",
"processedAt": "2026-04-30T10:00:00",
"ghiChu": null
}
}

6.7.3. Xóa kết quả AI
DELETE /api/ai/results/{id}
API này dùng để xóa một kết quả AI đã lưu trong bảng KetQuaAI.
Response
{
"success": true,
"message": "Delete AI result successfully",
"data": {
"id": 1,
"deleted": true
}
}

7. Danh sách API tổng hợp
   Tóm tắt văn bản
   POST /api/ai/summarize
   POST /api/ai/summarize/file
   Phân loại văn bản
   POST /api/ai/classify
   POST /api/ai/classify/file
   Trích xuất metadata
   POST /api/ai/metadata/extract
   POST /api/ai/metadata/extract/file
   Tìm kiếm thông minh
   POST   /api/ai/search/semantic
   POST   /api/ai/search/index-document
   DELETE /api/ai/search/index-document/{documentId}
   Gợi ý xử lý và phản hồi
   POST /api/ai/suggestions/handling
   POST /api/ai/suggestions/reply
   Chatbot hỗ trợ người dùng
   POST /api/ai/chatbot/ask
   Quản lý kết quả AI
   GET    /api/ai/results/documents/{documentId}
   GET    /api/ai/results/{id}
   DELETE /api/ai/results/{id}

8. Các API đã loại bỏ
   Các API sau không sử dụng vì kết quả AI được lưu tự động vào bảng KetQuaAI sau khi xử lý:
   POST /api/ai/results/summary/save
   POST /api/ai/results/classification/save
   POST /api/ai/results/metadata/save
   POST /api/ai/results/suggestion/save
   Các API sau không sử dụng vì hệ thống không lưu lịch sử hội thoại chatbot:
   GET    /api/ai/chatbot/conversations/{conversationId}
   DELETE /api/ai/chatbot/conversations/{conversationId}

9. Quy ước loại xử lý AI
   Giá trị cột LoaiXuLyAI trong bảng KetQuaAI:
   Giá trị
   Ý nghĩa
   SUMMARY
   Tóm tắt văn bản
   CLASSIFICATION
   Phân loại văn bản
   METADATA_EXTRACTION
   Trích xuất metadata
   SUGGESTION_HANDLING
   Gợi ý hướng xử lý
   SUGGESTION_REPLY
   Gợi ý nội dung phản hồi
   CHATBOT
   Chatbot hỗ trợ người dùng


10. Ghi chú triển khai database
    Bảng lưu kết quả AI
    CREATE TABLE KetQuaAI (
    ID BIGSERIAL PRIMARY KEY,
    VanBanID BIGINT,
    NguoiYeuCauID BIGINT,
    LoaiXuLyAI VARCHAR(100) NOT NULL,
    NoiDungDauVao TEXT,
    KetQuaTraVe TEXT,
    DoTinCay DECIMAL(5,2),
    ModelSuDung VARCHAR(100),
    ThoiGianXuLy TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    GhiChu VARCHAR(500)
    );
    Bảng lưu dữ liệu semantic search
    CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ai_document_chunk (
id BIGSERIAL PRIMARY KEY,
van_ban_id BIGINT NOT NULL,
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
11. Luồng xử lý chung
    Luồng xử lý AI
    Người dùng gửi request
    ↓
    ai-service nhận dữ liệu
    ↓
    AI xử lý nội dung
    ↓
    Trả kết quả cho frontend
    ↓
    Lưu kết quả vào bảng KetQuaAI
    Luồng semantic search
    Văn bản được gửi để index
    ↓
    Chia nội dung thành nhiều chunk
    ↓
    Tạo embedding cho từng chunk
    ↓
    Lưu vào bảng ai_document_chunk
    ↓
    Người dùng tìm kiếm
    ↓
    Tạo embedding cho từ khóa
    ↓
    So khớp vector với ai_document_chunk
    ↓
    Trả về các đoạn nội dung phù hợp nhất
    Luồng chatbot
    Người dùng gửi câu hỏi
    ↓
    Tạo embedding từ câu hỏi
    ↓
    Tìm chunk liên quan trong ai_document_chunk
    ↓
    AI sinh câu trả lời dựa trên chunk tìm được
    ↓
    Lưu kết quả hỏi đáp vào KetQuaAI
    ↓
    Trả câu trả lời cho người dùng
