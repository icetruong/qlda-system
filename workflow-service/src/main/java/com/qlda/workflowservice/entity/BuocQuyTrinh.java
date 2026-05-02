package com.qlda.workflowservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "BuocQuyTrinh")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuocQuyTrinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QuyTrinhID", nullable = false)
    private QuyTrinh quyTrinh;

    @Column(name = "TenBuoc", nullable = false, length = 255)
    private String tenBuoc;

    @Column(name = "ThuTuBuoc", nullable = false)
    private Integer thuTuBuoc;

    @Column(name = "VaiTroXuLy", length = 100)
    private String vaiTroXuLy;

    @Column(name = "ThoiGianXuLy")
    private Integer thoiGianXuLy;

    @Column(name = "BatBuocPheDuyet")
    private Boolean batBuocPheDuyet;

    @Column(name = "GhiChu", length = 500)
    private String ghiChu;
}
