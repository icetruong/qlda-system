INSERT INTO donvi (id, madonvi, tendonvi, donvichaid, dienthoai, email, diachi, sudung)
VALUES
(1, 'DV-001', 'Phòng Kế hoạch', NULL, '024-1234567', 'kehoach@coquan.gov.vn', 'Số 1, ĐT', TRUE),
(2, 'DV-002', 'Phòng CNTT', NULL, '024-7654321', 'cntt@coquan.gov.vn', 'Số 2, ĐT', TRUE),
(3, 'DV-003', 'Ban QLDA', NULL, '024-9988776', 'qlda@coquan.gov.vn', 'Số 3, ĐT', TRUE);


INSERT INTO nhomquyen (id, manhomquyen, tennhomquyen, mota, sudung)
VALUES
(1, 'ADMIN', 'Quản trị hệ thống', 'Quyền quản trị cao nhất', TRUE),
(2, 'USER', 'Người dùng', 'Quyền người dùng thông thường', TRUE),
(3, 'MANAGER', 'Trưởng đơn vị', 'Quản lý đơn vị', TRUE);


INSERT INTO chucnang (id, machucnang, tenchucnang, mota, thutu, sudung)
VALUES
(1, 'VIEW_DASHBOARD', 'Xem Dashboard', 'Xem trang tổng quan', 1, TRUE),
(2, 'MANAGE_USERS', 'Quản lý người dùng', 'Thêm/sửa/xóa người dùng', 2, TRUE),
(3, 'MANAGE_DOCUMENTS', 'Quản lý văn bản', 'Tạo/Chỉnh sửa văn bản', 3, TRUE),
(4, 'APPROVE_DOCUMENT', 'Phê duyệt văn bản', 'Phê duyệt các văn bản', 4, TRUE);


INSERT INTO phanquyen (id, nhomquyenid, chucnangid, isview, iscreate, isedit, isdelete, isapprove)
VALUES
(1, 1, 1, TRUE, TRUE, TRUE, TRUE, TRUE),
(2, 1, 2, TRUE, TRUE, TRUE, TRUE, TRUE),
(3, 1, 3, TRUE, TRUE, TRUE, TRUE, TRUE),
(4, 1, 4, TRUE, TRUE, TRUE, TRUE, TRUE),
(5, 2, 1, TRUE, FALSE, FALSE, FALSE, FALSE),
(6, 2, 3, TRUE, TRUE, FALSE, FALSE, FALSE),
(7, 3, 1, TRUE, FALSE, FALSE, FALSE, FALSE),
(8, 3, 4, TRUE, FALSE, FALSE, FALSE, TRUE);


INSERT INTO nguoidung (id, username, hoten, email, dienthoai, donviid, chucvu, nhomquyenid, azuread_id, trangthai)
VALUES
(1001, 'admin', 'Administrator', '102230238@sv1.dut.udn.vn', '0987000001', 1, 'Kỹ thuật', 1, NULL, 1),
(1002, 'nguyenvana', 'Trần Khả Hào', 'trankhahao7@gmail.com', '0987000002', 1, 'Chuyên viên', 2, NULL, 1),
(1003, 'truongdv', 'Trương ĐV', 'truongdv@coquan.gov.vn', '0987000003', 3, 'Trưởng phòng', 3, NULL, 1);


INSERT INTO loaivanban (id, maloaivanban, tenloaivanban, mota, sudung)
VALUES
(1, 'CONG_VAN', 'Công văn', 'Công văn đi/đến', TRUE),
(2, 'BIEN_BAN', 'Biên bản', 'Biên bản nghiệm thu, họp', TRUE),
(3, 'DE_XUAT', 'Đề xuất', 'Đề xuất nội dung', TRUE);


INSERT INTO templatevanban (id, matemplate, tentemplate, loaivanbanid, noidungmau, tepmau, nguoitaoid, sudung)
VALUES
(1, 'TPL-CV-01', 'Mẫu công văn hành chính', 1, 'Nội dung mẫu công văn...', NULL, 1001, TRUE),
(2, 'TPL-BB-01', 'Mẫu biên bản nghiệm thu', 2, 'Nội dung mẫu biên bản...', NULL, 1002, TRUE);


INSERT INTO quytrinh (id, maquytrinh, tenquytrinh, loaivanbanid, mota, sobuoc, sudung)
VALUES
(1, 'QTP-CV', 'Quy trình công văn chuẩn', 1, 'Luồng xử lý công văn', 3, TRUE),
(2, 'QTP-BB', 'Quy trình biên bản', 2, 'Luồng xử lý biên bản', 2, TRUE);


INSERT INTO buocquytrinh (id, quytrinhid, tenbuoc, thutubuoc, vaitroxuly, thoigianxuly, batbuocpheduyet)
VALUES
(10001, 1, 'Thư ký tiếp nhận', 1, 'THUKY', 24, FALSE),
(10002, 1, 'Trưởng phòng xử lý', 2, 'TRUONGPHONG', 48, TRUE),
(10003, 1, 'Ban QLDA phê duyệt', 3, 'BANQLDA', 72, TRUE),
(10004, 2, 'Thẩm định', 1, 'THAMDINH', 48, TRUE);


INSERT INTO vanban (id, sokyhieu, trichyeu, loaivanbanid, phanloaivanban, donvibanhanh, nguoiky, ngayvanban, ngaytiepnhan, domat, dokhan, nguoitaoid, donvichutriid, hanxuly, trangthai, duongdansharepoint, daocr, dakyso, ngayphathanh, daxoa)
VALUES
(5001, 'VB-001', 'Công văn về việc phân bổ kinh phí', 1, 0, 'Phòng Kế hoạch', 'Nguyễn Trưởng', NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days', 'Bình thường', 'Trung bình', 1002, 1, NOW() + INTERVAL '7 days', 1, NULL, FALSE, FALSE, NOW() - INTERVAL '10 days', FALSE),
(5002, 'VB-002', 'Biên bản nghiệm thu hạng mục A', 2, 0, 'Ban QLDA', 'Lê Kiên', NOW() - INTERVAL '20 days', NOW() - INTERVAL '19 days', 'Bình thường', 'Cao', 1003, 3, NOW() + INTERVAL '3 days', 2, NULL, FALSE, FALSE, NOW() - INTERVAL '20 days', FALSE);


INSERT INTO tepdinhkem (id, vanbanid, tentep, duongdantep, loaitep, kichthuoc, nguoitailenid)
VALUES
(20001, 5001, 'phan_bo_kinh_phi.pdf', '/files/phan_bo_kinh_phi.pdf', 'PDF', 123456, 1002),
(20002, 5002, 'bien_ban_nghiem_thu.pdf', '/files/bien_ban_nghiem_thu.pdf', 'PDF', 234567, 1003);


INSERT INTO hosocongviec (id, mahoso, tenhoso, vanbanid, nguoiphutrachid, donviid, trangthai, ngaymohoso)
VALUES
(30001, 'HS-001', 'Hồ sơ dự án Trung tâm HC', 5001, 1003, 3, 1, NOW() - INTERVAL '9 days');


INSERT INTO xulyvanban (id, vanbanid, buocquytrinhid, nguoiguiid, nguoinhanid, donvixulyid, hanhdongxuly, ykienxuly, ngaynhan, hanxuly, trangthaixuly)
VALUES
(40001, 5001, 10001, 1002, 1003, 3, 'PHAN_CONG', 'Xin ý kiến trưởng phòng', NOW() - INTERVAL '8 days', NOW() + INTERVAL '6 days', 1),
(40002, 5002, 10004, 1003, 1001, 1, 'NOP_HO_SO', 'Hoàn thành nghiệm thu', NOW() - INTERVAL '18 days', NOW() + INTERVAL '2 days', 2);


INSERT INTO thongbao (id, tieude, noidung, nguoinhanid, vanbanid, loaithongbao, kenhgui, dadoc)
VALUES
(60001, 'Văn bản mới: VB-001', 'Bạn có 1 văn bản cần xử lý', 1003, 5001, 'VAN_BAN', 'IN_APP', FALSE),
(60002, 'Biên bản nghiệm thu hoàn tất', 'Biên bản VB-002 đã hoàn tất', 1001, 5002, 'VAN_BAN', 'EMAIL', FALSE);


INSERT INTO lichsuhethong (id, nguoidungid, hanhdong, doituong, doituongid, noidungchitiet, diachiip, trangthai)
VALUES
(70001, 1002, 'Đăng nhập', 'NguoiDung', 1002, 'Người dùng đăng nhập hệ thống', '192.168.1.10', 1),
(70002, 1001, 'Phê duyệt văn bản', 'VanBan', 5002, 'Admin phê duyệt biên bản', '192.168.1.11', 1);


INSERT INTO ketquaai (id, vanbanid, nguoiyeucauid, loaixulyai, noidungdauvao, ketquatrave, dotincay, modelsudung)
VALUES
(80001, 5001, 1002, 'OCR', 'Scan văn bản', '{"text":"Kết quả OCR mẫu"}', 0.95, 'ocr-model-v1'),
(80002, 5002, 1003, 'EXTRACT_SUMMARY', 'Tóm tắt văn bản', '{"summary":"Tóm tắt mẫu"}', 0.88, 'summarizer-v1');

SELECT setval(pg_get_serial_sequence('donvi','id'), COALESCE((SELECT MAX(id) FROM donvi), 1));
SELECT setval(pg_get_serial_sequence('nhomquyen','id'), COALESCE((SELECT MAX(id) FROM nhomquyen), 1));
SELECT setval(pg_get_serial_sequence('chucnang','id'), COALESCE((SELECT MAX(id) FROM chucnang), 1));
SELECT setval(pg_get_serial_sequence('phanquyen','id'), COALESCE((SELECT MAX(id) FROM phanquyen), 1));
SELECT setval(pg_get_serial_sequence('nguoidung','id'), COALESCE((SELECT MAX(id) FROM nguoidung), 1));
SELECT setval(pg_get_serial_sequence('loaivanban','id'), COALESCE((SELECT MAX(id) FROM loaivanban), 1));
SELECT setval(pg_get_serial_sequence('templatevanban','id'), COALESCE((SELECT MAX(id) FROM templatevanban), 1));
SELECT setval(pg_get_serial_sequence('quytrinh','id'), COALESCE((SELECT MAX(id) FROM quytrinh), 1));
SELECT setval(pg_get_serial_sequence('buocquytrinh','id'), COALESCE((SELECT MAX(id) FROM buocquytrinh), 1));
SELECT setval(pg_get_serial_sequence('vanban','id'), COALESCE((SELECT MAX(id) FROM vanban), 1));
SELECT setval(pg_get_serial_sequence('tepdinhkem','id'), COALESCE((SELECT MAX(id) FROM tepdinhkem), 1));
SELECT setval(pg_get_serial_sequence('hosocongviec','id'), COALESCE((SELECT MAX(id) FROM hosocongviec), 1));
SELECT setval(pg_get_serial_sequence('xulyvanban','id'), COALESCE((SELECT MAX(id) FROM xulyvanban), 1));
SELECT setval(pg_get_serial_sequence('thongbao','id'), COALESCE((SELECT MAX(id) FROM thongbao), 1));
SELECT setval(pg_get_serial_sequence('lichsuhethong','id'), COALESCE((SELECT MAX(id) FROM lichsuhethong), 1));
SELECT setval(pg_get_serial_sequence('ketquaai','id'), COALESCE((SELECT MAX(id) FROM ketquaai), 1));