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
@Table(name = "NguoiDung")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "UserName", nullable = false, length = 100)
    private String userName;

    @Column(name = "HoTen", nullable = false, length = 255)
    private String hoTen;

    @Column(name = "Email", nullable = false, length = 150)
    private String email;

    @Column(name = "DienThoai", length = 20)
    private String dienThoai;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DonViID")
    private DonVi donVi;

    @Column(name = "ChucVu", length = 100)
    private String chucVu;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "NhomQuyenID")
    private NhomQuyen nhomQuyen;

    @Column(name = "AzureAD_ID", length = 100)
    private String azureAdId;

    @Column(name = "TrangThai")
    private Integer trangThai;

    @Column(name = "LanDangNhapCuoi")
    private LocalDateTime lanDangNhapCuoi;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat;
}
