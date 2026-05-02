package com.qlda.workflowservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "QuyTrinh")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuyTrinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "MaQuyTrinh", nullable = false, length = 50)
    private String maQuyTrinh;

    @Column(name = "TenQuyTrinh", nullable = false, length = 255)
    private String tenQuyTrinh;

    @Column(name = "LoaiVanBanID")
    private Integer loaiVanBanId;

    @Column(name = "MoTa", length = 1000)
    private String moTa;

    @Column(name = "SoBuoc")
    private Integer soBuoc;

    @Column(name = "SuDung")
    private Boolean suDung;
}
