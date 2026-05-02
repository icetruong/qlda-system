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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PhanQuyen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhanQuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NhomQuyenID", nullable = false)
    private NhomQuyen nhomQuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ChucNangID", nullable = false)
    private ChucNang chucNang;

    @Column(name = "IsView")
    private Boolean isView;

    @Column(name = "IsCreate")
    private Boolean isCreate;

    @Column(name = "IsEdit")
    private Boolean isEdit;

    @Column(name = "IsDelete")
    private Boolean isDelete;

    @Column(name = "IsApprove")
    private Boolean isApprove;
}
