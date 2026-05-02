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
@Table(name = "HoSoCongViec")
@Getter
@Setter
@NoArgsConstructor
public class HoSoCongViec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "MaHoSo", nullable = false, length = 100)
    private String maHoSo;

    @Column(name = "TenHoSo", nullable = false, length = 500)
    private String tenHoSo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VanBanID")
    private VanBan vanBan;

    @Column(name = "NguoiPhuTrachID")
    private Long nguoiPhuTrachId;

    @Column(name = "DonViID")
    private Integer donViId;

    @Column(name = "TrangThai")
    private Integer trangThai;

    @Column(name = "NgayMoHoSo")
    private LocalDateTime ngayMoHoSo;

    @Column(name = "NgayDongHoSo")
    private LocalDateTime ngayDongHoSo;

    @Column(name = "GhiChu", length = 1000)
    private String ghiChu;
}

