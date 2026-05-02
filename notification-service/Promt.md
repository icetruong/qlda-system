Bạn đang code trong support-service của hệ thống QLVB.

Hãy đọc trước:
1. AGENTS.md nếu có
2. .agents/skills nếu có
3. Skill tdd-workflow nếu có
4. API_SUPPORT.md
5. pom.xml / build.gradle
6. application.yml / application.properties
7. Cấu trúc package hiện tại

==================================================
BẮT BUỘC: QUY TRÌNH TDD (everything-claude-code)
==================================================

Làm theo Red → Green → Refactor:

- KHÔNG code implementation trước test
- Luôn viết test trước
- Chạy test → phải FAIL
- Sau đó mới implement code để PASS
- Chạy lại test → PASS
- Refactor nếu cần

Không được:
- Skip test
- Code trước test
- Báo hoàn thành khi chưa chạy test

==================================================
MỤC TIÊU
==================================================

Implement support-service gồm:

1. Notification (ThongBao)
2. Audit log / System log (LichSuHeThong)
3. Report API (dashboard + thống kê)

⚠️ QUAN TRỌNG:
- KHÔNG call API sang service khác ở bước này
- KHÔNG tích hợp document-service / workflow-service / auth-service
- Phần đó sẽ làm sau
- Hiện tại chỉ implement logic nội bộ + mock/skeleton

==================================================
DATABASE
==================================================

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

CREATE TABLE LichSuHeThong (
ID BIGSERIAL PRIMARY KEY,
NguoiDungID BIGINT,
HanhDong VARCHAR(255) NOT NULL,
DoiTuong VARCHAR(100),
DoiTuongID BIGINT,
NoiDungChiTiet TEXT,
DiaChiIP VARCHAR(50),
ThoiGianThucHien TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
TrangThai INT
);

Mapping:
- BIGSERIAL -> Long
- TIMESTAMP -> LocalDateTime
- BOOLEAN -> Boolean
- TEXT -> String
- Không đổi tên bảng/cột
- Không tạo relation sang service khác

==================================================
REPOSITORY
==================================================

ThongBaoRepository:
- findByNguoiNhanId
- findByNguoiNhanIdAndDaDoc
- countUnread

LichSuHeThongRepository:
- findByNguoiDungId
- findByDoiTuong
- filter theo date range

==================================================
KIẾN TRÚC
==================================================

- Controller → Service → Repository
- DTO riêng
- Không trả Entity trực tiếp

Tạo:
- ApiResponse
- PageResponse
- GlobalExceptionHandler
- ErrorCode
- AppException

==================================================
SECURITY (PRODUCTION)
==================================================

- Resource Server (OAuth2)
- Verify JWT bằng PUBLIC KEY
- RS256
- Không tạo JWT
- Không dùng HS256
- Không permitAll API

==================================================
API CẦN IMPLEMENT
==================================================

=====================================
1. NOTIFICATION
   =====================================

POST /api/notifications
GET /api/notifications
PATCH /api/notifications/{id}/read
DELETE /api/notifications/{id}
POST /api/notifications/{id}/send

Yêu cầu:
- CRUD notification
- DaDoc mặc định false
- read → set NgayDoc
- send → skeleton (không gửi thật)

=====================================
2. AUDIT LOG
   =====================================

POST /api/audit-logs
GET /api/audit-logs
GET /api/audit-logs/{id}

Yêu cầu:
- Lưu log hành động
- Filter theo:
    + nguoiDungId
    + doiTuong
    + date range

=====================================
3. REPORT (QUAN TRỌNG)
   =====================================

GET /api/reports/dashboard
GET /api/reports/documents/statistics
GET /api/reports/workflows/progress
GET /api/reports/overdue-documents
GET /api/reports/export

⚠️ QUAN TRỌNG:
- KHÔNG call service khác
- KHÔNG lấy data thật

👉 Thay vào đó:

- Return MOCK DATA hợp lý
- Hoặc return data từ in-memory
- Hoặc return structure đúng format

Ví dụ:
- dashboard → return số random hoặc fixed
- statistics → return sample list
- progress → return sample items
- export → return fake fileName/fileUrl

BẮT BUỘC:
- Structure response phải đúng docs/API_SUPPORT.md
- Ghi TODO rõ:
  "TODO: call document-service/workflow-service sau"

==================================================
TEST PLAN (BẮT BUỘC)
==================================================

Viết test trước:

1. NotificationServiceTest
- create success
- read success
- delete success
- not found

2. NotificationControllerTest
- POST success
- GET success
- PATCH read success
- DELETE success
- no token → 401

3. AuditLogServiceTest
- create log
- filter log
- get detail

4. AuditLogControllerTest
- GET list
- GET detail
- POST create

5. ReportServiceTest
- dashboard returns data
- statistics returns list
- progress returns items
- overdue returns page
- export returns file info

6. ReportControllerTest
- all endpoints return success
- no token → 401

==================================================
TDD FLOW
==================================================

Bước 1: Viết test Notification
Bước 2: Run → FAIL
Bước 3: Code → PASS

Bước 4: Viết test Audit
Bước 5: Run → FAIL
Bước 6: Code → PASS

Bước 7: Viết test Report
Bước 8: Run → FAIL
Bước 9: Code → PASS

Bước 10: Run toàn bộ test

==================================================
SKELETON / TODO
==================================================

BẮT BUỘC ghi TODO:

- TODO: call document-service
- TODO: call workflow-service
- TODO: call auth-service
- TODO: send email
- TODO: send Teams
- TODO: export Excel/PDF

==================================================
BUILD
==================================================

Phải chạy:
- mvn test hoặc ./mvnw test

Nếu chưa có test infra:
- mvn compile

==================================================
CẤM
==================================================

- Không code trước test
- Không skip test
- Không call service khác
- Không tạo bảng mới
- Không hardcode JWT
- Không permitAll API
- Không trả Entity trực tiếp
- Không code tất cả trong 1 file