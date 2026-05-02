package com.qlda.notificationservice.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ThongBao")
public class ThongBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TieuDe", nullable = false)
    private String tieuDe;

    @Column(name = "NoiDung", nullable = false)
    private String noiDung;

    @Column(name = "NguoiNhanID")
    private Long nguoiNhanId;

    @Column(name = "VanBanID")
    private Long vanBanId;

    @Column(name = "LoaiThongBao")
    private String loaiThongBao;

    @Column(name = "KenhGui")
    private String kenhGui;

    @Column(name = "DaDoc")
    private Boolean daDoc;

    @Column(name = "NgayGui")
    private LocalDateTime ngayGui;

    @Column(name = "NgayDoc")
    private LocalDateTime ngayDoc;

    @PrePersist
    void prePersist() {
        if (daDoc == null) {
            daDoc = false;
        }
        if (ngayGui == null) {
            ngayGui = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public Long getNguoiNhanId() {
        return nguoiNhanId;
    }

    public void setNguoiNhanId(Long nguoiNhanId) {
        this.nguoiNhanId = nguoiNhanId;
    }

    public Long getVanBanId() {
        return vanBanId;
    }

    public void setVanBanId(Long vanBanId) {
        this.vanBanId = vanBanId;
    }

    public String getLoaiThongBao() {
        return loaiThongBao;
    }

    public void setLoaiThongBao(String loaiThongBao) {
        this.loaiThongBao = loaiThongBao;
    }

    public String getKenhGui() {
        return kenhGui;
    }

    public void setKenhGui(String kenhGui) {
        this.kenhGui = kenhGui;
    }

    public Boolean getDaDoc() {
        return daDoc;
    }

    public void setDaDoc(Boolean daDoc) {
        this.daDoc = daDoc;
    }

    public LocalDateTime getNgayGui() {
        return ngayGui;
    }

    public void setNgayGui(LocalDateTime ngayGui) {
        this.ngayGui = ngayGui;
    }

    public LocalDateTime getNgayDoc() {
        return ngayDoc;
    }

    public void setNgayDoc(LocalDateTime ngayDoc) {
        this.ngayDoc = ngayDoc;
    }
}

