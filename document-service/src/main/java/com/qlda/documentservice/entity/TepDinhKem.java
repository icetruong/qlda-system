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
@Table(name = "TepDinhKem")
@Getter
@Setter
@NoArgsConstructor
public class TepDinhKem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VanBanID", nullable = false)
    private VanBan vanBan;

    @Column(name = "TenTep", nullable = false, length = 255)
    private String tenTep;

    @Column(name = "DuongDanTep", length = 1000)
    private String duongDanTep;

    @Column(name = "LoaiTep", length = 50)
    private String loaiTep;

    @Column(name = "KichThuoc")
    private Long kichThuoc;

    @Column(name = "NguoiTaiLenID")
    private Long nguoiTaiLenId;

    @Column(name = "NgayTaiLen")
    private LocalDateTime ngayTaiLen;
}

