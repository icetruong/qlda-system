PHẦN II. API NGHIỆP VỤ VĂN BẢN
1. Quy ước chung
   Base URL
   /api/documents
   Service phụ trách:
   document-service

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
   DOCUMENT_NOT_FOUND
   Không tìm thấy văn bản
   DOCUMENT_TYPE_NOT_FOUND
   Không tìm thấy loại văn bản
   ATTACHMENT_NOT_FOUND
   Không tìm thấy tệp đính kèm
   TEMPLATE_NOT_FOUND
   Không tìm thấy mẫu văn bản
   CASE_FILE_NOT_FOUND
   Không tìm thấy hồ sơ công việc
   DUPLICATE_DOCUMENT_NUMBER
   Số văn bản đã tồn tại
   FILE_UPLOAD_FAILED
   Upload file thất bại
   OCR_FAILED
   OCR thất bại
   FORBIDDEN
   Không có quyền thao tác
   INTERNAL_SERVER_ERROR
   Lỗi hệ thống


6. API chi tiết
   6.1. Tiếp nhận văn bản đến
   6.1.1. Tạo văn bản đến
   POST /api/documents/incoming
   Request
   {
   "soKyHieu": "123/CV-ABC",
   "trichYeu": "Văn bản về việc triển khai hệ thống xử lý văn bản điện tử",
   "loaiVanBanId": 1,
   "donViBanHanh": "Sở Thông tin và Truyền thông",
   "nguoiKy": "Nguyễn Văn A",
   "ngayVanBan": "2026-04-30",
   "ngayTiepNhan": "2026-04-30",
   "doMat": "THUONG",
   "doKhan": "KHAN",
   "donViChuTriId": 1,
   "hanXuLy": "2026-05-10T17:00:00"
   }
   Response
   {
   "success": true,
   "message": "Create incoming document successfully",
   "data": {
   "id": 1,
   "soKyHieu": "123/CV-ABC",
   "trichYeu": "Văn bản về việc triển khai hệ thống xử lý văn bản điện tử",
   "loaiVanBan": 1,
   "trangThai": 0,
   "ngayTao": "2026-04-30T10:00:00"
   }
   }
   6.1.2. Cập nhật văn bản đến
   PUT /api/documents/incoming/{id}
   Request
   {
   "soKyHieu": "123/CV-ABC",
   "trichYeu": "Cập nhật nội dung trích yếu văn bản",
   "loaiVanBanId": 1,
   "donViBanHanh": "Sở Thông tin và Truyền thông",
   "nguoiKy": "Nguyễn Văn A",
   "ngayVanBan": "2026-04-30",
   "ngayTiepNhan": "2026-04-30",
   "doMat": "THUONG",
   "doKhan": "KHAN",
   "donViChuTriId": 1,
   "hanXuLy": "2026-05-15T17:00:00",
   "trangThai": 1
   }
   Response
   {
   "success": true,
   "message": "Update incoming document successfully",
   "data": {
   "id": 1,
   "ngayCapNhat": "2026-04-30T11:00:00"
   }
   }
   6.1.3. Xem danh sách văn bản đến
   GET /api/documents/incoming
   Query Params
   page=0
   size=10
   keyword=hệ thống
   loaiVanBanId=1
   donViChuTriId=1
   trangThai=1
   fromDate=2026-04-01
   toDate=2026-04-30
   Response
   {
   "success": true,
   "message": "Get incoming documents successfully",
   "data": {
   "content": [
   {
   "id": 1,
   "soKyHieu": "123/CV-ABC",
   "trichYeu": "Văn bản về việc triển khai hệ thống xử lý văn bản điện tử",
   "tenLoaiVanBan": "Công văn",
   "donViBanHanh": "Sở Thông tin và Truyền thông",
   "ngayTiepNhan": "2026-04-30",
   "hanXuLy": "2026-05-10T17:00:00",
   "trangThai": 1
   }
   ],
   "page": 0,
   "size": 10,
   "totalElements": 1,
   "totalPages": 1
   }
   }
   6.1.4. Xem chi tiết văn bản đến
   GET /api/documents/incoming/{id}
   Response
   {
   "success": true,
   "message": "Get incoming document detail successfully",
   "data": {
   "id": 1,
   "soKyHieu": "123/CV-ABC",
   "trichYeu": "Văn bản về việc triển khai hệ thống xử lý văn bản điện tử",
   "loaiVanBanId": 1,
   "tenLoaiVanBan": "Công văn",
   "donViBanHanh": "Sở Thông tin và Truyền thông",
   "nguoiKy": "Nguyễn Văn A",
   "ngayVanBan": "2026-04-30",
   "ngayTiepNhan": "2026-04-30",
   "doMat": "THUONG",
   "doKhan": "KHAN",
   "donViChuTriId": 1,
   "hanXuLy": "2026-05-10T17:00:00",
   "trangThai": 1,
   "daOCR": false,
   "daKySo": false,
   "attachments": []
   }
   }
   6.1.5. Chuyển xử lý văn bản đến
   POST /api/documents/incoming/{id}/transfer
   Request
   {
   "nguoiNhanId": 2,
   "donViXuLyId": 1,
   "noiDungChuyen": "Chuyển chuyên viên xử lý văn bản",
   "hanXuLy": "2026-05-10T17:00:00"
   }
   Response
   {
   "success": true,
   "message": "Transfer document successfully",
   "data": {
   "documentId": 1,
   "nguoiNhanId": 2,
   "donViXuLyId": 1,
   "trangThai": 2
   }
   }
   6.2. Số hóa tài liệu OCR
   6.2.1. Upload tài liệu OCR
   POST /api/documents/{id}/ocr/upload
   Request
   multipart/form-data

file: document.pdf
Response
{
"success": true,
"message": "Upload OCR file successfully",
"data": {
"documentId": 1,
"fileName": "document.pdf",
"fileUrl": "/uploads/document.pdf"
}
}
6.2.2. Thực hiện OCR
POST /api/documents/{id}/ocr/process
Request
{
"fileUrl": "/uploads/document.pdf",
"language": "vie"
}
Response
{
"success": true,
"message": "OCR processed successfully",
"data": {
"documentId": 1,
"ocrText": "Nội dung văn bản sau khi OCR...",
"confidence": 92.5
}
}
6.2.3. Lưu kết quả OCR
POST /api/documents/{id}/ocr/save
Request
{
"ocrText": "Nội dung văn bản sau khi OCR...",
"confidence": 92.5
}
Response
{
"success": true,
"message": "Save OCR result successfully",
"data": {
"documentId": 1,
"daOCR": true
}
}
6.3. Soạn thảo văn bản điện tử
6.3.1. Tạo văn bản nháp
POST /api/documents/drafts
Request
{
"trichYeu": "Dự thảo văn bản triển khai kế hoạch",
"loaiVanBanId": 1,
"donViChuTriId": 1,
"noiDung": "Nội dung dự thảo văn bản..."
}
Response
{
"success": true,
"message": "Create draft document successfully",
"data": {
"id": 10,
"trichYeu": "Dự thảo văn bản triển khai kế hoạch",
"trangThai": 0,
"ngayTao": "2026-04-30T10:00:00"
}
}
6.3.2. Cập nhật văn bản nháp
PUT /api/documents/drafts/{id}
Request
{
"trichYeu": "Cập nhật dự thảo văn bản",
"noiDung": "Nội dung mới của văn bản",
"loaiVanBanId": 1,
"donViChuTriId": 1
}
Response
{
"success": true,
"message": "Update draft document successfully",
"data": {
"id": 10,
"ngayCapNhat": "2026-04-30T11:00:00"
}
}
6.3.3. Gửi góp ý văn bản
POST /api/documents/drafts/{id}/comments/request
Request
{
"nguoiNhanIds": [2, 3],
"noiDung": "Nhờ góp ý dự thảo văn bản"
}
Response
{
"success": true,
"message": "Send comment request successfully",
"data": {
"documentId": 10,
"nguoiNhanIds": [2, 3]
}
}
6.3.4. Trình ký văn bản
POST /api/documents/drafts/{id}/submit-signing
Request
{
"nguoiKyId": 4,
"noiDungTrinhKy": "Trình lãnh đạo ký văn bản"
}
Response
{
"success": true,
"message": "Submit document for signing successfully",
"data": {
"documentId": 10,
"nguoiKyId": 4,
"trangThai": 3
}
}
6.4. Quản lý Template văn bản
6.4.1. Tạo template
POST /api/documents/templates
Request
{
"maTemplate": "CV001",
"tenTemplate": "Mẫu công văn",
"loaiVanBanId": 1,
"noiDungMau": "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM...",
"tepMau": "/templates/cong-van.docx",
"suDung": true
}
Response
{
"success": true,
"message": "Create template successfully",
"data": {
"id": 1,
"maTemplate": "CV001"
}
}
6.4.2. Cập nhật template
PUT /api/documents/templates/{id}
Request
{
"tenTemplate": "Mẫu công văn cập nhật",
"loaiVanBanId": 1,
"noiDungMau": "Nội dung mẫu đã cập nhật",
"tepMau": "/templates/cong-van-new.docx",
"suDung": true
}
Response
{
"success": true,
"message": "Update template successfully",
"data": {
"id": 1
}
}
6.4.3. Xóa template
DELETE /api/documents/templates/{id}
Response
{
"success": true,
"message": "Delete template successfully",
"data": {
"id": 1
}
}
6.4.4. Xem danh sách template
GET /api/documents/templates
Query Params
page=0
size=10
keyword=công văn
loaiVanBanId=1
suDung=true
Response
{
"success": true,
"message": "Get templates successfully",
"data": {
"content": [
{
"id": 1,
"maTemplate": "CV001",
"tenTemplate": "Mẫu công văn",
"loaiVanBanId": 1,
"tenLoaiVanBan": "Công văn",
"suDung": true
}
],
"page": 0,
"size": 10,
"totalElements": 1,
"totalPages": 1
}
}
6.4.5. Xem chi tiết template
GET /api/documents/templates/{id}
Response
{
"success": true,
"message": "Get template detail successfully",
"data": {
"id": 1,
"maTemplate": "CV001",
"tenTemplate": "Mẫu công văn",
"loaiVanBanId": 1,
"noiDungMau": "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM...",
"tepMau": "/templates/cong-van.docx",
"suDung": true
}
}
6.5. Sử dụng Template
6.5.1. Áp dụng template vào văn bản
POST /api/documents/templates/{templateId}/apply
Request
{
"documentId": 1,
"replaceData": {
"tenDonVi": "Phòng Hành chính",
"ngay": "30",
"thang": "04",
"nam": "2026"
}
}
Response
{
"success": true,
"message": "Apply template successfully",
"data": {
"documentId": 1,
"templateId": 1,
"content": "Nội dung văn bản sau khi áp dụng template"
}
}
6.5.2. Tạo văn bản từ template
POST /api/documents/from-template
Request
{
"templateId": 1,
"trichYeu": "Văn bản tạo từ mẫu",
"loaiVanBanId": 1,
"donViChuTriId": 1,
"replaceData": {
"tenDonVi": "Phòng Hành chính",
"nam": "2026"
}
}
Response
{
"success": true,
"message": "Create document from template successfully",
"data": {
"documentId": 20,
"templateId": 1,
"trangThai": 0
}
}
6.6. Phát hành văn bản
6.6.1. Ký số văn bản
POST /api/documents/{id}/digital-sign
Request
{
"nguoiKyId": 4,
"signatureType": "SIMULATED",
"ghiChu": "Ký số văn bản"
}
Response
{
"success": true,
"message": "Digital sign document successfully",
"data": {
"documentId": 1,
"nguoiKyId": 4,
"daKySo": true,
"signedAt": "2026-04-30T10:00:00"
}
}
6.6.2. Phát hành văn bản
POST /api/documents/{id}/publish
Request
{
"ngayPhatHanh": "2026-04-30",
"noiDungPhatHanh": "Phát hành văn bản chính thức"
}
Response
{
"success": true,
"message": "Publish document successfully",
"data": {
"documentId": 1,
"ngayPhatHanh": "2026-04-30",
"trangThai": 5
}
}
6.6.3. Gửi văn bản
POST /api/documents/{id}/send
Request
{
"nguoiNhanIds": [2, 3],
"donViNhanIds": [1, 2],
"kenhGui": "EMAIL",
"noiDung": "Gửi văn bản đã phát hành"
}
Response
{
"success": true,
"message": "Send document successfully",
"data": {
"documentId": 1,
"kenhGui": "EMAIL",
"totalReceivers": 4
}
}
6.7. Quản lý văn bản đi
6.7.1. Tạo văn bản đi
POST /api/documents/outgoing
Request
{
"soKyHieu": "01/CV-QLVB",
"trichYeu": "Văn bản đi về triển khai hệ thống",
"loaiVanBanId": 1,
"nguoiKy": "Nguyễn Văn A",
"ngayVanBan": "2026-04-30",
"doMat": "THUONG",
"doKhan": "BINH_THUONG",
"donViChuTriId": 1
}
Response
{
"success": true,
"message": "Create outgoing document successfully",
"data": {
"id": 2,
"soKyHieu": "01/CV-QLVB",
"loaiVanBan": 2,
"trangThai": 0
}
}
6.7.2. Cập nhật văn bản đi
PUT /api/documents/outgoing/{id}
Request
{
"soKyHieu": "01/CV-QLVB",
"trichYeu": "Cập nhật văn bản đi",
"loaiVanBanId": 1,
"nguoiKy": "Nguyễn Văn A",
"ngayVanBan": "2026-04-30",
"doMat": "THUONG",
"doKhan": "BINH_THUONG",
"donViChuTriId": 1,
"trangThai": 1
}
Response
{
"success": true,
"message": "Update outgoing document successfully",
"data": {
"id": 2,
"ngayCapNhat": "2026-04-30T11:00:00"
}
}
6.7.3. Gửi phê duyệt văn bản đi
POST /api/documents/outgoing/{id}/submit-approval
Request
{
"nguoiPheDuyetId": 4,
"noiDungTrinh": "Trình lãnh đạo phê duyệt văn bản đi"
}
Response
{
"success": true,
"message": "Submit outgoing document for approval successfully",
"data": {
"documentId": 2,
"nguoiPheDuyetId": 4,
"trangThai": 3
}
}
6.7.4. Xem danh sách văn bản đi
GET /api/documents/outgoing
Query Params
page=0
size=10
keyword=kế hoạch
loaiVanBanId=1
trangThai=1
fromDate=2026-04-01
toDate=2026-04-30
Response
{
"success": true,
"message": "Get outgoing documents successfully",
"data": {
"content": [
{
"id": 2,
"soKyHieu": "01/CV-QLVB",
"trichYeu": "Văn bản đi về triển khai hệ thống",
"tenLoaiVanBan": "Công văn",
"ngayVanBan": "2026-04-30",
"trangThai": 1
}
],
"page": 0,
"size": 10,
"totalElements": 1,
"totalPages": 1
}
}
6.7.5. Xem chi tiết văn bản đi
GET /api/documents/outgoing/{id}
Response
{
"success": true,
"message": "Get outgoing document detail successfully",
"data": {
"id": 2,
"soKyHieu": "01/CV-QLVB",
"trichYeu": "Văn bản đi về triển khai hệ thống",
"loaiVanBanId": 1,
"nguoiKy": "Nguyễn Văn A",
"ngayVanBan": "2026-04-30",
"doMat": "THUONG",
"doKhan": "BINH_THUONG",
"donViChuTriId": 1,
"trangThai": 1,
"daKySo": false,
"attachments": []
}
}
6.8. Đánh số văn bản tự động
6.8.1. Sinh số văn bản tự động
POST /api/documents/numbering/generate
Request
{
"loaiVanBanId": 1,
"donViId": 1,
"nam": 2026
}
Response
{
"success": true,
"message": "Generate document number successfully",
"data": {
"soKyHieu": "01/CV-QLVB/2026"
}
}
6.8.2. Kiểm tra trùng số văn bản
GET /api/documents/numbering/check
Query Params
soKyHieu=01/CV-QLVB/2026
Response
{
"success": true,
"message": "Check document number successfully",
"data": {
"soKyHieu": "01/CV-QLVB/2026",
"exists": false
}
}
6.8.3. Gán số vào văn bản
PATCH /api/documents/{id}/number
Request
{
"soKyHieu": "01/CV-QLVB/2026"
}
Response
{
"success": true,
"message": "Assign document number successfully",
"data": {
"documentId": 1,
"soKyHieu": "01/CV-QLVB/2026"
}
}
6.9. Quản lý hồ sơ công việc
6.9.1. Tạo hồ sơ công việc
POST /api/documents/case-files
Request
{
"maHoSo": "HS001",
"tenHoSo": "Hồ sơ triển khai hệ thống QLVB",
"vanBanId": 1,
"nguoiPhuTrachId": 2,
"donViId": 1,
"trangThai": 1,
"ghiChu": "Hồ sơ công việc năm 2026"
}
Response
{
"success": true,
"message": "Create case file successfully",
"data": {
"id": 1,
"maHoSo": "HS001"
}
}
6.9.2. Cập nhật hồ sơ công việc
PUT /api/documents/case-files/{id}
Request
{
"tenHoSo": "Hồ sơ triển khai hệ thống QLVB cập nhật",
"nguoiPhuTrachId": 2,
"donViId": 1,
"trangThai": 1,
"ghiChu": "Cập nhật thông tin hồ sơ"
}
Response
{
"success": true,
"message": "Update case file successfully",
"data": {
"id": 1
}
}
6.9.3. Gắn văn bản vào hồ sơ
POST /api/documents/case-files/{id}/documents
Request
{
"vanBanId": 1
}
Response
{
"success": true,
"message": "Attach document to case file successfully",
"data": {
"caseFileId": 1,
"vanBanId": 1
}
}
6.9.4. Xem danh sách hồ sơ
GET /api/documents/case-files
Query Params
page=0
size=10
keyword=triển khai
donViId=1
nguoiPhuTrachId=2
trangThai=1
Response
{
"success": true,
"message": "Get case files successfully",
"data": {
"content": [
{
"id": 1,
"maHoSo": "HS001",
"tenHoSo": "Hồ sơ triển khai hệ thống QLVB",
"nguoiPhuTrachId": 2,
"donViId": 1,
"trangThai": 1
}
],
"page": 0,
"size": 10,
"totalElements": 1,
"totalPages": 1
}
}
6.9.5. Xem chi tiết hồ sơ
GET /api/documents/case-files/{id}
Response
{
"success": true,
"message": "Get case file detail successfully",
"data": {
"id": 1,
"maHoSo": "HS001",
"tenHoSo": "Hồ sơ triển khai hệ thống QLVB",
"nguoiPhuTrachId": 2,
"donViId": 1,
"trangThai": 1,
"ngayMoHoSo": "2026-04-30T10:00:00",
"ngayDongHoSo": null,
"ghiChu": "Hồ sơ công việc năm 2026",
"documents": [
{
"id": 1,
"soKyHieu": "123/CV-ABC",
"trichYeu": "Văn bản về việc triển khai hệ thống"
}
]
}
}
6.9.6. Xóa hồ sơ
DELETE /api/documents/case-files/{id}
Response
{
"success": true,
"message": "Delete case file successfully",
"data": {
"id": 1
}
}
6.10. Phân loại và lập danh mục hồ sơ
6.10.1. Phân loại hồ sơ
PATCH /api/documents/case-files/{id}/classification
Request
{
"nhomHoSo": "VAN_BAN_HANH_CHINH",
"ghiChu": "Phân loại hồ sơ hành chính"
}
Response
{
"success": true,
"message": "Classify case file successfully",
"data": {
"caseFileId": 1,
"nhomHoSo": "VAN_BAN_HANH_CHINH"
}
}
6.10.2. Tìm kiếm hồ sơ theo nhóm
GET /api/documents/case-files/classification
Query Params
nhomHoSo=VAN_BAN_HANH_CHINH
page=0
size=10
Response
{
"success": true,
"message": "Search case files by classification successfully",
"data": {
"content": [
{
"id": 1,
"maHoSo": "HS001",
"tenHoSo": "Hồ sơ triển khai hệ thống QLVB",
"nhomHoSo": "VAN_BAN_HANH_CHINH"
}
],
"page": 0,
"size": 10,
"totalElements": 1,
"totalPages": 1
}
}
6.11. Quản lý phiên bản văn bản
6.11.1. Tạo phiên bản mới
POST /api/documents/{id}/versions
Request
{
"versionName": "v1.1",
"noiDungThayDoi": "Cập nhật nội dung văn bản",
"fileUrl": "/files/vanban_v1_1.docx"
}
Response
{
"success": true,
"message": "Create document version successfully",
"data": {
"documentId": 1,
"versionName": "v1.1",
"createdAt": "2026-04-30T10:00:00"
}
}
6.11.2. Xem lịch sử phiên bản
GET /api/documents/{id}/versions
Response
{
"success": true,
"message": "Get document versions successfully",
"data": [
{
"versionName": "v1.0",
"fileUrl": "/files/vanban_v1_0.docx",
"createdAt": "2026-04-29T10:00:00"
},
{
"versionName": "v1.1",
"fileUrl": "/files/vanban_v1_1.docx",
"createdAt": "2026-04-30T10:00:00"
}
]
}
6.11.3. So sánh phiên bản
GET /api/documents/{id}/versions/compare
Query Params
fromVersion=v1.0
toVersion=v1.1
Response
{
"success": true,
"message": "Compare document versions successfully",
"data": {
"documentId": 1,
"fromVersion": "v1.0",
"toVersion": "v1.1",
"differences": [
{
"field": "noiDung",
"oldValue": "Nội dung cũ",
"newValue": "Nội dung mới"
}
]
}
}
6.11.4. Khôi phục phiên bản cũ
POST /api/documents/{id}/versions/restore
Request
{
"versionName": "v1.0"
}
Response
{
"success": true,
"message": "Restore document version successfully",
"data": {
"documentId": 1,
"restoredVersion": "v1.0"
}
}
6.11.5. Xóa phiên bản
DELETE /api/documents/{id}/versions/{versionName}
Response
{
"success": true,
"message": "Delete document version successfully",
"data": {
"documentId": 1,
"versionName": "v1.1"
}
}
7. API tệp đính kèm
   7.1. Upload file đính kèm
   POST /api/documents/{id}/attachments
   Request
   multipart/form-data

file: van-ban.pdf
Response
{
"success": true,
"message": "Upload attachment successfully",
"data": {
"id": 1,
"documentId": 1,
"tenTep": "van-ban.pdf",
"duongDanTep": "/uploads/van-ban.pdf",
"loaiTep": "pdf",
"kichThuoc": 204800
}
}
7.2. Xem danh sách file đính kèm
GET /api/documents/{id}/attachments
Response
{
"success": true,
"message": "Get attachments successfully",
"data": [
{
"id": 1,
"tenTep": "van-ban.pdf",
"duongDanTep": "/uploads/van-ban.pdf",
"loaiTep": "pdf",
"kichThuoc": 204800,
"ngayTaiLen": "2026-04-30T10:00:00"
}
]
}
7.3. Tải file đính kèm
GET /api/documents/attachments/{attachmentId}/download
Response
{
"success": true,
"message": "Get attachment download link successfully",
"data": {
"attachmentId": 1,
"downloadUrl": "/uploads/van-ban.pdf"
}
}
7.4. Xóa file đính kèm
DELETE /api/documents/attachments/{attachmentId}
Response
{
"success": true,
"message": "Delete attachment successfully",
"data": {
"attachmentId": 1
}
}
8. API loại văn bản
   8.1. Tạo loại văn bản
   POST /api/documents/types
   Request
   {
   "maLoaiVanBan": "CV",
   "tenLoaiVanBan": "Công văn",
   "moTa": "Loại văn bản công văn",
   "suDung": true
   }
   Response
   {
   "success": true,
   "message": "Create document type successfully",
   "data": {
   "id": 1,
   "maLoaiVanBan": "CV"
   }
   }
   8.2. Cập nhật loại văn bản
   PUT /api/documents/types/{id}
   Request
   {
   "tenLoaiVanBan": "Công văn cập nhật",
   "moTa": "Mô tả loại văn bản",
   "suDung": true
   }
   Response
   {
   "success": true,
   "message": "Update document type successfully",
   "data": {
   "id": 1
   }
   }
   8.3. Xem danh sách loại văn bản
   GET /api/documents/types
   Query Params
   keyword=công văn
   suDung=true
   Response
   {
   "success": true,
   "message": "Get document types successfully",
   "data": [
   {
   "id": 1,
   "maLoaiVanBan": "CV",
   "tenLoaiVanBan": "Công văn",
   "moTa": "Loại văn bản công văn",
   "suDung": true
   }
   ]
   }
   8.4. Xem chi tiết loại văn bản
   GET /api/documents/types/{id}
   Response
   {
   "success": true,
   "message": "Get document type detail successfully",
   "data": {
   "id": 1,
   "maLoaiVanBan": "CV",
   "tenLoaiVanBan": "Công văn",
   "moTa": "Loại văn bản công văn",
   "suDung": true
   }
   }
   8.5. Xóa loại văn bản
   DELETE /api/documents/types/{id}
   Response
   {
   "success": true,
   "message": "Delete document type successfully",
   "data": {
   "id": 1
   }
   }

Tóm tắt API

POST   /api/documents/incoming
PUT    /api/documents/incoming/{id}
GET    /api/documents/incoming
GET    /api/documents/incoming/{id}
POST   /api/documents/incoming/{id}/transfer

POST   /api/documents/{id}/ocr/upload
POST   /api/documents/{id}/ocr/process
POST   /api/documents/{id}/ocr/save

POST   /api/documents/drafts
PUT    /api/documents/drafts/{id}
POST   /api/documents/drafts/{id}/comments/request
POST   /api/documents/drafts/{id}/submit-signing

POST   /api/documents/templates
PUT    /api/documents/templates/{id}
DELETE /api/documents/templates/{id}
GET    /api/documents/templates
GET    /api/documents/templates/{id}

POST   /api/documents/templates/{templateId}/apply
POST   /api/documents/from-template

POST   /api/documents/{id}/digital-sign
POST   /api/documents/{id}/publish
POST   /api/documents/{id}/send

POST   /api/documents/outgoing
PUT    /api/documents/outgoing/{id}
POST   /api/documents/outgoing/{id}/submit-approval
GET    /api/documents/outgoing
GET    /api/documents/outgoing/search

POST   /api/documents/numbering/generate
GET    /api/documents/numbering/check
PATCH  /api/documents/{id}/number

POST   /api/documents/case-files
PUT    /api/documents/case-files/{id}
POST   /api/documents/case-files/{id}/documents
GET    /api/documents/case-files
GET    /api/documents/case-files/{id}
DELETE /api/documents/case-files/{id}

PATCH  /api/documents/case-files/{id}/classification
GET    /api/documents/case-files/classification

POST   /api/documents/{id}/versions
GET    /api/documents/{id}/versions
GET    /api/documents/{id}/versions/compare
POST   /api/documents/{id}/versions/restore
DELETE /api/documents/{id}/versions/{versionName}

POST   /api/documents/{id}/attachments
GET    /api/documents/{id}/attachments
GET    /api/documents/attachments/{attachmentId}/download
DELETE /api/documents/attachments/{attachmentId}

POST   /api/documents/types
PUT    /api/documents/types/{id}
GET    /api/documents/types
GET    /api/documents/types/{id}
DELETE /api/documents/types/{id}
