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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TemplateVanBan")
@Getter
@Setter
@NoArgsConstructor
public class TemplateVanBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "MaTemplate", nullable = false, length = 50)
    private String maTemplate;

    @Column(name = "TenTemplate", nullable = false, length = 255)
    private String tenTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoaiVanBanID")
    private LoaiVanBan loaiVanBan;

    @Column(name = "NoiDungMau")
    private String noiDungMau;

    @Column(name = "TepMau", length = 500)
    private String tepMau;

    @Column(name = "NguoiTaoID")
    private Long nguoiTaoId;

    @Column(name = "SuDung")
    private Boolean suDung;
}

