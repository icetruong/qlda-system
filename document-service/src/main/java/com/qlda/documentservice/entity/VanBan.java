package com.qlda.documentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "VanBan")
@Getter
@Setter
@NoArgsConstructor
public class VanBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "SoKyHieu", length = 100)
    private String soKyHieu;

    @Column(name = "TrichYeu", nullable = false, length = 1000)
    private String trichYeu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoaiVanBanID")
    private LoaiVanBan loaiVanBan;

    @Column(name = "PhanLoaiVanBan")
    private Integer phanLoaiVanBan;

    @Column(name = "DonViBanHanh", length = 255)
    private String donViBanHanh;

    @Column(name = "NguoiKy", length = 255)
    private String nguoiKy;

    @Column(name = "NgayVanBan")
    private LocalDateTime ngayVanBan;

    @Column(name = "NgayTiepNhan")
    private LocalDateTime ngayTiepNhan;

    @Column(name = "DoMat", length = 50)
    private String doMat;

    @Column(name = "DoKhan", length = 50)
    private String doKhan;

    @Column(name = "NguoiTaoID")
    private Long nguoiTaoId;

    @Column(name = "DonViChuTriID")
    private Integer donViChuTriId;

    @Column(name = "HanXuLy")
    private LocalDateTime hanXuLy;

    @Column(name = "TrangThai")
    private Integer trangThai;

    @Column(name = "DuongDanSharePoint", length = 500)
    private String duongDanSharePoint;

    @Column(name = "DuongDanOneDrive", length = 500)
    private String duongDanOneDrive;

    @Column(name = "DaOCR")
    private Boolean daOCR;

    @Column(name = "DaKySo")
    private Boolean daKySo;

    @Column(name = "NgayPhatHanh")
    private LocalDateTime ngayPhatHanh;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "DaXoa")
    private Boolean daXoa;
}

