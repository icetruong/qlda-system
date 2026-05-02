package com.qlda.documentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "LoaiVanBan")
@Getter
@Setter
@NoArgsConstructor
public class LoaiVanBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "MaLoaiVanBan", nullable = false, length = 50)
    private String maLoaiVanBan;

    @Column(name = "TenLoaiVanBan", nullable = false, length = 255)
    private String tenLoaiVanBan;

    @Column(name = "MoTa", length = 500)
    private String moTa;

    @Column(name = "SuDung")
    private Boolean suDung;
}

