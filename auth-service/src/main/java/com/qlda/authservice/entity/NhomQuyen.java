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
@Table(name = "NhomQuyen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NhomQuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "MaNhomQuyen", nullable = false, length = 50)
    private String maNhomQuyen;

    @Column(name = "TenNhomQuyen", nullable = false, length = 255)
    private String tenNhomQuyen;

    @Column(name = "MoTa", length = 500)
    private String moTa;

    @Column(name = "SuDung")
    private Boolean suDung;
}
