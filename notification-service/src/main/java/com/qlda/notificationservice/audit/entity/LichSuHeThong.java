package com.qlda.notificationservice.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "LichSuHeThong")
public class LichSuHeThong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NguoiDungID")
    private Long nguoiDungId;

    @Column(name = "HanhDong", nullable = false)
    private String hanhDong;

    @Column(name = "DoiTuong")
    private String doiTuong;

    @Column(name = "DoiTuongID")
    private Long doiTuongId;

    @Column(name = "NoiDungChiTiet")
    private String noiDungChiTiet;

    @Column(name = "DiaChiIP")
    private String diaChiIP;

    @Column(name = "ThoiGianThucHien", nullable = false)
    private LocalDateTime thoiGianThucHien;

    @Column(name = "TrangThai")
    private Integer trangThai;

    @PrePersist
    void prePersist() {
        if (thoiGianThucHien == null) {
            thoiGianThucHien = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNguoiDungId() {
        return nguoiDungId;
    }

    public void setNguoiDungId(Long nguoiDungId) {
        this.nguoiDungId = nguoiDungId;
    }

    public String getHanhDong() {
        return hanhDong;
    }

    public void setHanhDong(String hanhDong) {
        this.hanhDong = hanhDong;
    }

    public String getDoiTuong() {
        return doiTuong;
    }

    public void setDoiTuong(String doiTuong) {
        this.doiTuong = doiTuong;
    }

    public Long getDoiTuongId() {
        return doiTuongId;
    }

    public void setDoiTuongId(Long doiTuongId) {
        this.doiTuongId = doiTuongId;
    }

    public String getNoiDungChiTiet() {
        return noiDungChiTiet;
    }

    public void setNoiDungChiTiet(String noiDungChiTiet) {
        this.noiDungChiTiet = noiDungChiTiet;
    }

    public String getDiaChiIP() {
        return diaChiIP;
    }

    public void setDiaChiIP(String diaChiIP) {
        this.diaChiIP = diaChiIP;
    }

    public LocalDateTime getThoiGianThucHien() {
        return thoiGianThucHien;
    }

    public void setThoiGianThucHien(LocalDateTime thoiGianThucHien) {
        this.thoiGianThucHien = thoiGianThucHien;
    }

    public Integer getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Integer trangThai) {
        this.trangThai = trangThai;
    }
}

