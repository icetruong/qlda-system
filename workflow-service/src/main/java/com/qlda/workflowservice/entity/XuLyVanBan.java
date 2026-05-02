package com.qlda.workflowservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "XuLyVanBan")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XuLyVanBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "VanBanID", nullable = false)
    private Long vanBanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BuocQuyTrinhID")
    private BuocQuyTrinh buocQuyTrinh;

    @Column(name = "NguoiGuiID")
    private Long nguoiGuiId;

    @Column(name = "NguoiNhanID")
    private Long nguoiNhanId;

    @Column(name = "DonViXuLyID")
    private Integer donViXuLyId;

    @Column(name = "HanhDongXuLy", length = 100)
    private String hanhDongXuLy;

    @Column(name = "YKienXuLy", length = 1000)
    private String yKienXuLy;

    @Column(name = "NgayNhan")
    private LocalDateTime ngayNhan;

    @Column(name = "HanXuLy")
    private LocalDateTime hanXuLy;

    @Column(name = "NgayHoanThanh")
    private LocalDateTime ngayHoanThanh;

    @Column(name = "TyLeHoanThanh")
    private Integer tyLeHoanThanh;

    @Column(name = "TrangThaiXuLy")
    private Integer trangThaiXuLy;

    @Column(name = "TepKetQua", length = 500)
    private String tepKetQua;
}
