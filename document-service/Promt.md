Bạn đang code trong document-service của hệ thống QLVB - xử lý văn bản điện tử tích hợp Office 365.

Hãy đọc các file sau trước khi code:
1. AGENTS.md nếu có
2. .agents/skills nếu có
3. API_DOCUMENT.md
4. pom.xml / build.gradle
5. application.yml / application.properties
6. Cấu trúc package hiện tại của project

Áp dụng quy trình everything-claude-code:
- Đọc context trước
- Lập plan trước khi code
- Code từng phần nhỏ
- Tự kiểm tra compile/test sau khi code
- Không code lan man ngoài phạm vi docs/API_DOCUMENT.md
- Không tự ý xóa code cũ nếu không cần thiết
- Không tự ý thêm bảng database mới nếu chưa được yêu cầu

==================================================
MỤC TIÊU
==================================================

Code FULL API cho document-service theo docs/API_DOCUMENT.md.

Base URL:
- /api/documents

Tất cả API đều yêu cầu Bearer Token.

Các nhóm API cần implement:
1. Văn bản đến
2. OCR
3. Văn bản nháp
4. Template văn bản
5. Áp dụng template
6. Ký số / phát hành / gửi văn bản
7. Văn bản đi
8. Đánh số văn bản tự động
9. Hồ sơ công việc
10. Phân loại hồ sơ
11. Phiên bản văn bản
12. Tệp đính kèm
13. Loại văn bản

==================================================
DATABASE HIỆN CÓ
==================================================

Database document-service hiện có 5 bảng:

1. LoaiVanBan
2. VanBan
3. TepDinhKem
4. TemplateVanBan
5. HoSoCongViec

Nếu Entity/Repository chưa có thì tạo mới.
Nếu đã có thì kiểm tra và sửa mapping cho đúng schema.

Schema:

CREATE TABLE LoaiVanBan (
ID SERIAL PRIMARY KEY,
MaLoaiVanBan VARCHAR(50) NOT NULL,
TenLoaiVanBan VARCHAR(255) NOT NULL,
MoTa VARCHAR(500),
SuDung BOOLEAN DEFAULT TRUE,

    CONSTRAINT uq_loaivanban_maloaivanban UNIQUE (MaLoaiVanBan)
);

CREATE TABLE VanBan (
ID BIGSERIAL PRIMARY KEY,
SoKyHieu VARCHAR(100),
TrichYeu VARCHAR(1000) NOT NULL,
LoaiVanBanID INT,
PhanLoaiVanBan INT,
DonViBanHanh VARCHAR(255),
NguoiKy VARCHAR(255),
NgayVanBan TIMESTAMP,
NgayTiepNhan TIMESTAMP,
DoMat VARCHAR(50),
DoKhan VARCHAR(50),

    NguoiTaoID BIGINT,
    DonViChuTriID INT,

    HanXuLy TIMESTAMP,
    TrangThai INT,
    DuongDanSharePoint VARCHAR(500),
    DuongDanOneDrive VARCHAR(500),
    DaOCR BOOLEAN DEFAULT FALSE,
    DaKySo BOOLEAN DEFAULT FALSE,
    NgayPhatHanh TIMESTAMP,
    NgayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NgayCapNhat TIMESTAMP,
    DaXoa BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_vanban_loaivanban FOREIGN KEY (LoaiVanBanID) REFERENCES LoaiVanBan(ID)
);

CREATE TABLE TepDinhKem (
ID BIGSERIAL PRIMARY KEY,
VanBanID BIGINT NOT NULL,
TenTep VARCHAR(255) NOT NULL,
DuongDanTep VARCHAR(1000),
LoaiTep VARCHAR(50),
KichThuoc BIGINT,

    NguoiTaiLenID BIGINT,

    NgayTaiLen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tepdinhkem_vanban FOREIGN KEY (VanBanID) REFERENCES VanBan(ID)
);

CREATE TABLE TemplateVanBan (
ID SERIAL PRIMARY KEY,
MaTemplate VARCHAR(50) NOT NULL,
TenTemplate VARCHAR(255) NOT NULL,
LoaiVanBanID INT,
NoiDungMau TEXT,
TepMau VARCHAR(500),

    NguoiTaoID BIGINT,

    SuDung BOOLEAN DEFAULT TRUE,

    CONSTRAINT uq_template_matemplate UNIQUE (MaTemplate),
    CONSTRAINT fk_template_loaivanban FOREIGN KEY (LoaiVanBanID) REFERENCES LoaiVanBan(ID)
);

CREATE TABLE HoSoCongViec (
ID BIGSERIAL PRIMARY KEY,
MaHoSo VARCHAR(100) NOT NULL,
TenHoSo VARCHAR(500) NOT NULL,
VanBanID BIGINT,

    NguoiPhuTrachID BIGINT,
    DonViID INT,

    TrangThai INT,
    NgayMoHoSo TIMESTAMP,
    NgayDongHoSo TIMESTAMP,
    GhiChu VARCHAR(1000),

    CONSTRAINT uq_hoso_mahoso UNIQUE (MaHoSo),
    CONSTRAINT fk_hoso_vanban FOREIGN KEY (VanBanID) REFERENCES VanBan(ID)
);

==================================================
QUY TẮC ENTITY MAPPING
==================================================

- Dùng Jakarta Persistence: jakarta.persistence.*
- PostgreSQL
- SERIAL -> Integer
- BIGSERIAL -> Long
- TIMESTAMP -> LocalDateTime
- BOOLEAN -> Boolean
- TEXT -> String
- Giữ nguyên tên bảng bằng @Table(name = "...")
- Giữ nguyên tên cột bằng @Column(name = "...")
- Không đổi tên bảng/cột sang snake_case
- Không thêm field ngoài schema
- Không tạo quan hệ JPA sang auth-service

Các ID tham chiếu mềm sang auth-service chỉ lưu dạng Long/Integer:
- NguoiTaoID
- DonViChuTriID
- NguoiTaiLenID
- NguoiPhuTrachID
- DonViID

Quan hệ nội bộ:
- VanBan ManyToOne LoaiVanBan qua LoaiVanBanID
- TepDinhKem ManyToOne VanBan qua VanBanID
- TemplateVanBan ManyToOne LoaiVanBan qua LoaiVanBanID
- HoSoCongViec ManyToOne VanBan qua VanBanID

Yêu cầu:
- ManyToOne dùng FetchType.LAZY
- Không dùng CascadeType.REMOVE
- Không trả Entity trực tiếp ra API

==================================================
REPOSITORY CẦN CÓ
==================================================

Tạo các repository sau nếu chưa có:

1. LoaiVanBanRepository extends JpaRepository<LoaiVanBan, Integer>
   Methods:
- Optional<LoaiVanBan> findByMaLoaiVanBan(String maLoaiVanBan);
- boolean existsByMaLoaiVanBan(String maLoaiVanBan);
- List<LoaiVanBan> findBySuDung(Boolean suDung);

2. VanBanRepository extends JpaRepository<VanBan, Long>
   Methods:
- boolean existsBySoKyHieuAndDaXoaFalse(String soKyHieu);
- Optional<VanBan> findByIdAndDaXoaFalse(Long id);
- Page<VanBan> findByPhanLoaiVanBanAndDaXoaFalse(Integer phanLoaiVanBan, Pageable pageable);
- List<VanBan> findBySoKyHieuAndDaXoaFalse(String soKyHieu);

Nếu cần filter phức tạp thì dùng JpaSpecificationExecutor<VanBan>.

3. TepDinhKemRepository extends JpaRepository<TepDinhKem, Long>
   Methods:
- List<TepDinhKem> findByVanBan_Id(Long vanBanId);
- Optional<TepDinhKem> findByIdAndVanBan_Id(Long id, Long vanBanId);

4. TemplateVanBanRepository extends JpaRepository<TemplateVanBan, Integer>
   Methods:
- Optional<TemplateVanBan> findByMaTemplate(String maTemplate);
- boolean existsByMaTemplate(String maTemplate);
- Optional<TemplateVanBan> findByIdAndSuDungTrue(Integer id);

Nếu cần filter thì dùng JpaSpecificationExecutor<TemplateVanBan>.

5. HoSoCongViecRepository extends JpaRepository<HoSoCongViec, Long>
   Methods:
- Optional<HoSoCongViec> findByMaHoSo(String maHoSo);
- boolean existsByMaHoSo(String maHoSo);

Nếu cần filter thì dùng JpaSpecificationExecutor<HoSoCongViec>.

==================================================
KIẾN TRÚC CODE
==================================================

Tạo cấu trúc theo project hiện tại. Nếu chưa có thì dùng cấu trúc:

- controller
- service
- service.impl
- repository
- entity
- dto.request
- dto.response
- exception
- common
- config
- security
- mapper
- specification

Bắt buộc:
- Controller -> Service -> Repository
- DTO riêng cho Request/Response
- Không trả Entity trực tiếp
- Tạo mapper riêng hoặc method mapping riêng
- Tạo ApiResponse dùng chung
- Tạo GlobalExceptionHandler
- Tạo ErrorCode enum hoặc constants theo docs/API_DOCUMENT.md
- Validate request bằng jakarta.validation
- Dùng Pageable cho API danh sách
- Dùng Specification cho filter phức tạp

Response chung:

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

Error codes trong docs/API_DOCUMENT.md:
- DOCUMENT_NOT_FOUND
- DOCUMENT_TYPE_NOT_FOUND
- ATTACHMENT_NOT_FOUND
- TEMPLATE_NOT_FOUND
- CASE_FILE_NOT_FOUND
- DUPLICATE_DOCUMENT_NUMBER
- FILE_UPLOAD_FAILED
- OCR_FAILED
- FORBIDDEN
- INTERNAL_SERVER_ERROR
- INVALID_REQUEST

==================================================
BẢO MẬT CHUẨN PRODUCTION
==================================================

Hệ thống dùng JWT theo mô hình microservice production:

1. auth-service:
- Là service DUY NHẤT tạo JWT
- Ký token bằng PRIVATE KEY RSA
- Thuật toán RS256
- Token chứa tối thiểu:
    + userId
    + username
    + roles
    + authorities nếu có

2. API Gateway:
- Verify JWT bằng PUBLIC KEY
- Reject request nếu token invalid/expired
- Forward request xuống service kèm header:
  Authorization: Bearer <token>

3. document-service:
- KHÔNG tạo JWT
- KHÔNG dùng secret key riêng
- KHÔNG dùng HS256
- PHẢI verify JWT bằng PUBLIC KEY giống gateway
- Dùng Spring Security Resource Server:
  spring-boot-starter-oauth2-resource-server

Nếu project chưa có dependency thì thêm:

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

Cấu hình JWT:
- Public key đọc từ application.yml hoặc env
- Ưu tiên cấu hình chuẩn:

spring:
security:
oauth2:
resourceserver:
jwt:
public-key-location: classpath:public.pem

Nếu cần đọc từ env thì tạo config rõ ràng, không hardcode key.

Security yêu cầu:
- Tất cả endpoint /api/documents/** yêu cầu authenticated
- Bật method security: @EnableMethodSecurity
- Dùng JwtAuthenticationConverter để map roles/authorities từ claim roles
- Role trong token có thể là ADMIN, CHUYEN_VIEN, LANH_DAO
- Nếu token claim là roles: ["ADMIN"] thì convert thành ROLE_ADMIN
- Dùng @PreAuthorize ở service/controller nếu phù hợp

Không được:
- Không hardcode secret
- Không tạo JWT trong document-service
- Không bỏ qua verify JWT
- Không chỉ parse token thủ công
- Không cho permitAll toàn bộ /api/documents/**

Trong code service, cần lấy current user nếu cần:
- userId từ claim userId
- username từ claim preferred_username hoặc sub/username
- roles từ authorities

Nếu chưa dùng current user được thì tạo SecurityUtils để lấy thông tin từ JwtAuthenticationToken.

==================================================
QUY ƯỚC NGHIỆP VỤ
==================================================

PhanLoaiVanBan:
- 1 = văn bản đến
- 2 = văn bản đi
- 3 = văn bản nháp

TrangThai gợi ý:
- 0 = mới tạo / nháp
- 1 = đang xử lý
- 2 = đã chuyển xử lý
- 3 = trình ký / trình duyệt
- 4 = đã ký
- 5 = đã phát hành

Soft delete:
- VanBan: DaXoa = true
- TemplateVanBan: SuDung = false
- LoaiVanBan: SuDung = false
- HoSoCongViec: nếu không có DaXoa thì có thể set TrangThai = -1

Date/time:
- Request dạng "2026-04-30" có thể parse thành LocalDate rồi convert LocalDateTime đầu ngày nếu field DB là TIMESTAMP
- Request dạng "2026-05-10T17:00:00" parse LocalDateTime

==================================================
API CẦN IMPLEMENT
==================================================

Đọc chi tiết request/response trong docs/API_DOCUMENT.md và implement đúng endpoint.

1. Văn bản đến:
- POST   /api/documents/incoming
- PUT    /api/documents/incoming/{id}
- GET    /api/documents/incoming
- GET    /api/documents/incoming/{id}
- POST   /api/documents/incoming/{id}/transfer

Yêu cầu:
- Tạo văn bản đến với PhanLoaiVanBan = 1
- List filter:
    + keyword
    + loaiVanBanId
    + donViChuTriId
    + trangThai
    + fromDate
    + toDate
    + DaXoa = false
- Detail kèm attachments
- Transfer cập nhật TrangThai = 2

2. OCR:
- POST /api/documents/{id}/ocr/upload
- POST /api/documents/{id}/ocr/process
- POST /api/documents/{id}/ocr/save

Yêu cầu:
- upload nhận multipart/form-data
- Lưu file local theo upload-dir trong application.yml
- process có thể skeleton/mock trả ocrText, confidence
- save cập nhật VanBan.DaOCR = true
- Vì schema chưa có cột lưu OCR text, không tự thêm cột, ghi TODO nếu cần bảng/cột riêng

3. Văn bản nháp:
- POST /api/documents/drafts
- PUT  /api/documents/drafts/{id}
- POST /api/documents/drafts/{id}/comments/request
- POST /api/documents/drafts/{id}/submit-signing

Yêu cầu:
- Tạo VanBan với PhanLoaiVanBan = 3, TrangThai = 0
- comments/request skeleton trả đúng response
- submit-signing cập nhật TrangThai = 3

4. Template văn bản:
- POST   /api/documents/templates
- PUT    /api/documents/templates/{id}
- DELETE /api/documents/templates/{id}
- GET    /api/documents/templates
- GET    /api/documents/templates/{id}

Yêu cầu:
- Validate MaTemplate unique
- Delete mềm bằng SuDung = false
- List filter keyword, loaiVanBanId, suDung

5. Sử dụng Template:
- POST /api/documents/templates/{templateId}/apply
- POST /api/documents/from-template

Yêu cầu:
- apply: thay placeholder đơn giản trong NoiDungMau theo replaceData
- from-template: tạo VanBan mới từ template
- Không cần sinh file docx thật nếu chưa có thư viện, ghi TODO

6. Ký số / phát hành / gửi văn bản:
- POST /api/documents/{id}/digital-sign
- POST /api/documents/{id}/publish
- POST /api/documents/{id}/send

Yêu cầu:
- digital-sign cập nhật DaKySo = true, TrangThai = 4
- publish cập nhật NgayPhatHanh, TrangThai = 5
- send skeleton trả totalReceivers
- Không tích hợp email/Teams thật nếu chưa có config, ghi TODO

7. Văn bản đi:
- POST /api/documents/outgoing
- PUT  /api/documents/outgoing/{id}
- POST /api/documents/outgoing/{id}/submit-approval
- GET  /api/documents/outgoing
- GET  /api/documents/outgoing/{id}

Yêu cầu:
- Tạo văn bản đi với PhanLoaiVanBan = 2
- submit approval cập nhật TrangThai = 3
- List filter:
    + keyword
    + loaiVanBanId
    + trangThai
    + fromDate
    + toDate
    + DaXoa = false
- Detail kèm attachments

Lưu ý:
Trong phần tóm tắt docs/API_DOCUMENT.md có thể ghi GET /api/documents/outgoing/search, nhưng chi tiết API là GET /api/documents/outgoing/{id}. Nếu conflict, ưu tiên phần chi tiết.

8. Đánh số văn bản tự động:
- POST  /api/documents/numbering/generate
- GET   /api/documents/numbering/check
- PATCH /api/documents/{id}/number

Yêu cầu:
- generate tạo số dạng 01/CV-QLVB/2026
- Nếu LoaiVanBan có MaLoaiVanBan thì dùng mã đó trong số văn bản nếu hợp lý
- check kiểm tra SoKyHieu tồn tại trong VanBan chưa
- assign number validate không trùng rồi cập nhật SoKyHieu
- Nếu trùng trả DUPLICATE_DOCUMENT_NUMBER

9. Hồ sơ công việc:
- POST   /api/documents/case-files
- PUT    /api/documents/case-files/{id}
- POST   /api/documents/case-files/{id}/documents
- GET    /api/documents/case-files
- GET    /api/documents/case-files/{id}
- DELETE /api/documents/case-files/{id}

Yêu cầu:
- Validate MaHoSo unique
- attach document: cập nhật HoSoCongViec.VanBan
- Vì schema hiện tại chỉ có 1 VanBanID trong HoSoCongViec, nếu cần nhiều văn bản trong 1 hồ sơ thì ghi TODO cần bảng trung gian, không tự tạo bảng
- Delete mềm bằng TrangThai = -1 nếu không có cột DaXoa

10. Phân loại hồ sơ:
- PATCH /api/documents/case-files/{id}/classification
- GET   /api/documents/case-files/classification

Yêu cầu:
- Schema hiện tại chưa có NhomHoSo
- Vẫn tạo Controller/Service/DTO đúng API
- Implement skeleton/mock hoặc dùng GhiChu để ghi chú tạm nếu hợp lý
- Ghi TODO cần bổ sung field/bảng nếu muốn lưu phân loại thật
- Không tự thêm cột/bảng

11. Phiên bản văn bản:
- POST   /api/documents/{id}/versions
- GET    /api/documents/{id}/versions
- GET    /api/documents/{id}/versions/compare
- POST   /api/documents/{id}/versions/restore
- DELETE /api/documents/{id}/versions/{versionName}

Yêu cầu:
- Schema chưa có bảng version
- Implement skeleton/mock hoặc in-memory để API chạy được
- Ghi TODO cần bảng DocumentVersion nếu muốn lưu thật
- Không tự tạo bảng

12. Tệp đính kèm:
- POST   /api/documents/{id}/attachments
- GET    /api/documents/{id}/attachments
- GET    /api/documents/attachments/{attachmentId}/download
- DELETE /api/documents/attachments/{attachmentId}

Yêu cầu:
- upload multipart file
- Lưu file local theo upload-dir
- Tạo TepDinhKem record
- List theo VanBanID
- Download trả link/path
- Delete record và nếu có thể thì xóa file vật lý

13. Loại văn bản:
- POST   /api/documents/types
- PUT    /api/documents/types/{id}
- GET    /api/documents/types
- GET    /api/documents/types/{id}
- DELETE /api/documents/types/{id}

Yêu cầu:
- CRUD LoaiVanBan
- Validate MaLoaiVanBan unique
- Delete mềm bằng SuDung = false

==================================================
FILE UPLOAD CONFIG
==================================================

Nếu chưa có thì thêm cấu hình:

app:
upload-dir: uploads

Tạo FileStorageService:
- store(MultipartFile file)
- delete(String path)
- buildFileUrl(String filename)

Yêu cầu:
- Validate file không rỗng
- Tạo thư mục nếu chưa tồn tại
- Không ghi đè tên file nếu trùng, thêm timestamp/UUID
- Bắt lỗi và trả FILE_UPLOAD_FAILED

==================================================
CÁC PHẦN SKELETON / TODO
==================================================

Một số API chưa thể lưu thật vì DB hiện tại chưa có bảng/cột:
- transfer history
- comment request
- submit signing history
- send document receivers
- OCR text
- case file classification
- document versions

Với các API này:
- Vẫn implement endpoint
- Trả response đúng format
- Cập nhật được field hiện có nếu phù hợp
- Thêm TODO rõ ràng trong code
- Không tự ý tạo bảng mới

==================================================
YÊU CẦU BUILD/TEST
==================================================

Sau khi code:
- Chạy lệnh phù hợp:
    + ./mvnw test
    + hoặc mvn test
    + hoặc ./gradlew test
- Nếu test fail do thiếu config, sửa config test nếu hợp lý
- Không bỏ qua lỗi compile
- Không báo hoàn thành khi project chưa compile

Nếu project chưa có test:
- Ít nhất phải chạy compile:
    + ./mvnw compile
    + hoặc mvn compile

==================================================
BÁO CÁO SAU KHI LÀM
==================================================

Sau khi hoàn thành, báo cáo bằng tiếng Việt:

1. Đã đọc những file nào
2. Đã tạo file nào
3. Đã sửa file nào
4. API nào đã implement thật
5. API nào đang skeleton/mock/TODO
6. Security đã cấu hình như thế nào
7. Lệnh build/test đã chạy
8. Kết quả build/test
9. Lỗi còn tồn tại nếu có

==================================================
NGUYÊN TẮC BẮT BUỘC
==================================================

- Không sửa tên bảng/cột database
- Không thêm bảng/cột mới
- Không hardcode JWT secret/key
- Không dùng HS256 cho document-service
- Không tạo JWT trong document-service
- Không permitAll toàn bộ API document
- Không trả Entity trực tiếp
- Không code tất cả trong một file
- Không bỏ qua lỗi compile
- Không xóa code cũ nếu không cần thiết
- Ưu tiên project chạy được trước
- Phần nào chưa có DB thật thì skeleton/mock + TODO rõ ràng