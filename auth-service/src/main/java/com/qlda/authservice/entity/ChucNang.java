package com.qlda.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ChucNang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChucNang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "MaChucNang", nullable = false, length = 100)
    private String maChucNang;

    @Column(name = "TenChucNang", nullable = false, length = 255)
    private String tenChucNang;

    @Column(name = "MoTa", length = 500)
    private String moTa;

    @Column(name = "ThuTu")
    private Integer thuTu;

    @Column(name = "SuDung")
    private Boolean suDung;
}
