-- =========================
-- 1. AUTH SERVICE
-- =========================

CREATE TABLE DonVi (
    ID SERIAL PRIMARY KEY,
    MaDonVi VARCHAR(50) NOT NULL,
    TenDonVi VARCHAR(255) NOT NULL,
    DonViChaID INT,
    DienThoai VARCHAR(20),
    Email VARCHAR(100),
    DiaChi VARCHAR(255),
    SuDung BOOLEAN DEFAULT TRUE,
    NgayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NgayCapNhat TIMESTAMP,

    CONSTRAINT uq_donvi_madonvi UNIQUE (MaDonVi),
    CONSTRAINT fk_donvi_cha FOREIGN KEY (DonViChaID) REFERENCES DonVi(ID)
);

CREATE TABLE NhomQuyen (
    ID SERIAL PRIMARY KEY,
    MaNhomQuyen VARCHAR(50) NOT NULL,
    TenNhomQuyen VARCHAR(255) NOT NULL,
    MoTa VARCHAR(500),
    SuDung BOOLEAN DEFAULT TRUE,

    CONSTRAINT uq_nhomquyen_manhomquyen UNIQUE (MaNhomQuyen)
);

CREATE TABLE NguoiDung (
    ID BIGSERIAL PRIMARY KEY,
    UserName VARCHAR(100) NOT NULL,
    HoTen VARCHAR(255) NOT NULL,
    Email VARCHAR(150) NOT NULL,
    DienThoai VARCHAR(20),
    DonViID INT,
    ChucVu VARCHAR(100),
    NhomQuyenID INT,
    AzureAD_ID VARCHAR(100),
    TrangThai INT,
    LanDangNhapCuoi TIMESTAMP,
    NgayTao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NgayCapNhat TIMESTAMP,

    CONSTRAINT uq_nguoidung_username UNIQUE (UserName),
    CONSTRAINT uq_nguoidung_email UNIQUE (Email),
    CONSTRAINT fk_nguoidung_donvi FOREIGN KEY (DonViID) REFERENCES DonVi(ID),
    CONSTRAINT fk_nguoidung_nhomquyen FOREIGN KEY (NhomQuyenID) REFERENCES NhomQuyen(ID)
);

CREATE TABLE ChucNang (
    ID SERIAL PRIMARY KEY,
    MaChucNang VARCHAR(100) NOT NULL,
    TenChucNang VARCHAR(255) NOT NULL,
    MoTa VARCHAR(500),
    ThuTu INT,
    SuDung BOOLEAN DEFAULT TRUE,

    CONSTRAINT uq_chucnang_machucnang UNIQUE (MaChucNang)
);

CREATE TABLE PhanQuyen (
    ID SERIAL PRIMARY KEY,
    NhomQuyenID INT NOT NULL,
    ChucNangID INT NOT NULL,
    IsView BOOLEAN DEFAULT FALSE,
    IsCreate BOOLEAN DEFAULT FALSE,
    IsEdit BOOLEAN DEFAULT FALSE,
    IsDelete BOOLEAN DEFAULT FALSE,
    IsApprove BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_phanquyen_nhomquyen FOREIGN KEY (NhomQuyenID) REFERENCES NhomQuyen(ID),
    CONSTRAINT fk_phanquyen_chucnang FOREIGN KEY (ChucNangID) REFERENCES ChucNang(ID),
    CONSTRAINT uq_phanquyen_nhom_chucnang UNIQUE (NhomQuyenID, ChucNangID)
);


-- =========================
-- 2. DOCUMENT SERVICE
-- =========================

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

    -- ID tham chiếu mềm sang auth-service
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

    -- ID tham chiếu mềm sang auth-service
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

    -- ID tham chiếu mềm sang auth-service
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

    -- ID tham chiếu mềm sang auth-service
    NguoiPhuTrachID BIGINT,
    DonViID INT,

    TrangThai INT,
    NgayMoHoSo TIMESTAMP,
    NgayDongHoSo TIMESTAMP,
    GhiChu VARCHAR(1000),

    CONSTRAINT uq_hoso_mahoso UNIQUE (MaHoSo),
    CONSTRAINT fk_hoso_vanban FOREIGN KEY (VanBanID) REFERENCES VanBan(ID)
);


-- =========================
-- 3. WORKFLOW SERVICE
-- =========================

CREATE TABLE QuyTrinh (
    ID SERIAL PRIMARY KEY,
    MaQuyTrinh VARCHAR(50) NOT NULL,
    TenQuyTrinh VARCHAR(255) NOT NULL,

    -- ID tham chiếu mềm sang document-service
    LoaiVanBanID INT,

    MoTa VARCHAR(1000),
    SoBuoc INT,
    SuDung BOOLEAN DEFAULT TRUE,

    CONSTRAINT uq_quytrinh_maquytrinh UNIQUE (MaQuyTrinh)
);

CREATE TABLE BuocQuyTrinh (
    ID BIGSERIAL PRIMARY KEY,
    QuyTrinhID INT NOT NULL,
    TenBuoc VARCHAR(255) NOT NULL,
    ThuTuBuoc INT NOT NULL,
    VaiTroXuLy VARCHAR(100),
    ThoiGianXuLy INT,
    BatBuocPheDuyet BOOLEAN DEFAULT FALSE,
    GhiChu VARCHAR(500),

    CONSTRAINT fk_buocquytrinh_quytrinh FOREIGN KEY (QuyTrinhID) REFERENCES QuyTrinh(ID)
);

CREATE TABLE XuLyVanBan (
    ID BIGSERIAL PRIMARY KEY,

    -- ID tham chiếu mềm sang document-service
    VanBanID BIGINT NOT NULL,

    BuocQuyTrinhID BIGINT,

    -- ID tham chiếu mềm sang auth-service
    NguoiGuiID BIGINT,
    NguoiNhanID BIGINT,
    DonViXuLyID INT,

    HanhDongXuLy VARCHAR(100),
    YKienXuLy VARCHAR(1000),
    NgayNhan TIMESTAMP,
    HanXuLy TIMESTAMP,
    NgayHoanThanh TIMESTAMP,
    TyLeHoanThanh INT,
    TrangThaiXuLy INT,
    TepKetQua VARCHAR(500),

    CONSTRAINT fk_xuly_buoc FOREIGN KEY (BuocQuyTrinhID) REFERENCES BuocQuyTrinh(ID),
    CONSTRAINT chk_xuly_tylehoanthanh CHECK (
        TyLeHoanThanh IS NULL OR (TyLeHoanThanh >= 0 AND TyLeHoanThanh <= 100)
    )
);


-- =========================
-- 4. SUPPORT SERVICE
-- =========================

CREATE TABLE ThongBao (
    ID BIGSERIAL PRIMARY KEY,
    TieuDe VARCHAR(255) NOT NULL,
    NoiDung TEXT NOT NULL,

    -- ID tham chiếu mềm sang auth-service
    NguoiNhanID BIGINT,

    -- ID tham chiếu mềm sang document-service
    VanBanID BIGINT,

    LoaiThongBao VARCHAR(100),
    KenhGui VARCHAR(50),
    DaDoc BOOLEAN DEFAULT FALSE,
    NgayGui TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    NgayDoc TIMESTAMP
);

CREATE TABLE LichSuHeThong (
    ID BIGSERIAL PRIMARY KEY,

    -- ID tham chiếu mềm sang auth-service
    NguoiDungID BIGINT,

    HanhDong VARCHAR(255) NOT NULL,
    DoiTuong VARCHAR(100),
    DoiTuongID BIGINT,
    NoiDungChiTiet TEXT,
    DiaChiIP VARCHAR(50),
    ThoiGianThucHien TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    TrangThai INT
);


-- =========================
-- 5. AI SERVICE
-- =========================

CREATE TABLE KetQuaAI (
    ID BIGSERIAL PRIMARY KEY,

    -- ID tham chiếu mềm sang document-service
    VanBanID BIGINT,

    -- ID tham chiếu mềm sang auth-service
    NguoiYeuCauID BIGINT,

    LoaiXuLyAI VARCHAR(100) NOT NULL,
    NoiDungDauVao TEXT,
    KetQuaTraVe TEXT,
    DoTinCay DECIMAL(5,2),
    ModelSuDung VARCHAR(100),
    ThoiGianXuLy TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    GhiChu VARCHAR(500)
);


-- =========================
-- 6. INDEX CHO AUTH SERVICE
-- =========================

CREATE INDEX idx_nguoidung_donvi 
ON NguoiDung(DonViID);

CREATE INDEX idx_nguoidung_nhomquyen 
ON NguoiDung(NhomQuyenID);

CREATE INDEX idx_phanquyen_nhomquyen_chucnang 
ON PhanQuyen(NhomQuyenID, ChucNangID);


-- =========================
-- 7. INDEX CHO DOCUMENT SERVICE
-- =========================

CREATE INDEX idx_vanban_loaivanban 
ON VanBan(LoaiVanBanID);

CREATE INDEX idx_vanban_nguoitao 
ON VanBan(NguoiTaoID);

CREATE INDEX idx_vanban_donvichutri 
ON VanBan(DonViChuTriID);

CREATE INDEX idx_vanban_donvi_trangthai 
ON VanBan(DonViChuTriID, TrangThai);

CREATE INDEX idx_vanban_loai_trangthai 
ON VanBan(LoaiVanBanID, TrangThai);

CREATE INDEX idx_vanban_sokyhieu 
ON VanBan(SoKyHieu);

CREATE INDEX idx_vanban_ngaytiepnhan 
ON VanBan(NgayTiepNhan);

CREATE INDEX idx_vanban_hanxuly 
ON VanBan(HanXuLy);

CREATE INDEX idx_vanban_chuaxoa 
ON VanBan(ID)
WHERE DaXoa = FALSE;

CREATE INDEX idx_tepdinhkem_vanban 
ON TepDinhKem(VanBanID);

CREATE INDEX idx_tepdinhkem_nguoitailen 
ON TepDinhKem(NguoiTaiLenID);

CREATE INDEX idx_template_loaivanban 
ON TemplateVanBan(LoaiVanBanID);

CREATE INDEX idx_template_nguoitao 
ON TemplateVanBan(NguoiTaoID);

CREATE INDEX idx_hoso_vanban 
ON HoSoCongViec(VanBanID);

CREATE INDEX idx_hoso_nguoiphutrach 
ON HoSoCongViec(NguoiPhuTrachID);

CREATE INDEX idx_hoso_donvi 
ON HoSoCongViec(DonViID);


-- =========================
-- 8. INDEX CHO WORKFLOW SERVICE
-- =========================

CREATE INDEX idx_quytrinh_loaivanban 
ON QuyTrinh(LoaiVanBanID);

CREATE INDEX idx_buocquytrinh_quytrinh 
ON BuocQuyTrinh(QuyTrinhID);

CREATE INDEX idx_xuly_vanban 
ON XuLyVanBan(VanBanID);

CREATE INDEX idx_xuly_buoc 
ON XuLyVanBan(BuocQuyTrinhID);

CREATE INDEX idx_xuly_nguoinhan 
ON XuLyVanBan(NguoiNhanID);

CREATE INDEX idx_xuly_nguoinhan_trangthai 
ON XuLyVanBan(NguoiNhanID, TrangThaiXuLy);

CREATE INDEX idx_xuly_vanban_trangthai 
ON XuLyVanBan(VanBanID, TrangThaiXuLy);

CREATE INDEX idx_xuly_hanxuly 
ON XuLyVanBan(HanXuLy);

CREATE INDEX idx_xuly_chuahoanthanh 
ON XuLyVanBan(NguoiNhanID, HanXuLy)
WHERE NgayHoanThanh IS NULL;


-- =========================
-- 9. INDEX CHO SUPPORT SERVICE
-- =========================

CREATE INDEX idx_thongbao_nguoinhan 
ON ThongBao(NguoiNhanID);

CREATE INDEX idx_thongbao_vanban 
ON ThongBao(VanBanID);

CREATE INDEX idx_thongbao_chuadoc 
ON ThongBao(NguoiNhanID, NgayGui)
WHERE DaDoc = FALSE;

CREATE INDEX idx_lichsu_nguoidung 
ON LichSuHeThong(NguoiDungID);

CREATE INDEX idx_lichsu_thoigian 
ON LichSuHeThong(ThoiGianThucHien);

CREATE INDEX idx_lichsu_doituong 
ON LichSuHeThong(DoiTuong, DoiTuongID);


-- =========================
-- 10. INDEX CHO AI SERVICE
-- =========================

CREATE INDEX idx_ketquaai_vanban 
ON KetQuaAI(VanBanID);

CREATE INDEX idx_ketquaai_vanban_loaixuly 
ON KetQuaAI(VanBanID, LoaiXuLyAI);

CREATE INDEX idx_ketquaai_nguoiyeucau 
ON KetQuaAI(NguoiYeuCauID);


-- =========================
-- 11. FULL TEXT SEARCH CHO VĂN BẢN
-- =========================

CREATE INDEX idx_vanban_fulltext 
ON VanBan 
USING GIN (
    to_tsvector(
        'simple',
        coalesce(SoKyHieu, '') || ' ' ||
        coalesce(TrichYeu, '') || ' ' ||
        coalesce(DonViBanHanh, '') || ' ' ||
        coalesce(NguoiKy, '')
    )
);