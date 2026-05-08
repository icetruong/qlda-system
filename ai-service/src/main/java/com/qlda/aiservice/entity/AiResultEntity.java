package com.qlda.aiservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "KetQuaAI")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "VanBanID")
    private Long vanBanID;

    @Column(name = "NguoiYeuCauID")
    private Long nguoiYeuCauID;

    @Enumerated(EnumType.STRING)
    @Column(name = "LoaiXuLyAI")
    private AiProcessType loaiXuLyAI;

    @Column(name = "NoiDungDauVao")
    private String noiDungDauVao;

    @Column(name = "KetQuaTraVe")
    private String ketQuaTraVe;

    @Column(name = "DoTinCay")
    private Double doTinCay;

    @Column(name = "ModelSuDung")
    private String modelSuDung;

    @Column(name = "ThoiGianXuLy")
    private LocalDateTime thoiGianXuLy;

    @Column(name = "GhiChu")
    private String ghiChu;
}

