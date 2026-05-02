package com.qlda.authservice.entity;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DonVi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonVi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "MaDonVi", nullable = false, length = 50)
    private String maDonVi;

    @Column(name = "TenDonVi", nullable = false, length = 255)
    private String tenDonVi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DonViChaID")
    private DonVi donViCha;

    @Column(name = "DienThoai", length = 20)
    private String dienThoai;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "DiaChi", length = 255)
    private String diaChi;

    @Column(name = "SuDung")
    private Boolean suDung;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat;
}
