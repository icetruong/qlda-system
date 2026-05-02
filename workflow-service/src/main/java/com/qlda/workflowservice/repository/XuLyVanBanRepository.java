package com.qlda.workflowservice.repository;

import com.qlda.workflowservice.entity.XuLyVanBan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface XuLyVanBanRepository extends JpaRepository<XuLyVanBan, Long>, JpaSpecificationExecutor<XuLyVanBan> {
    Optional<XuLyVanBan> findByIdAndVanBanId(Long id, Long vanBanId);

    List<XuLyVanBan> findByVanBanIdOrderByNgayNhanAsc(Long vanBanId);

    List<XuLyVanBan> findByVanBanIdOrderByIdAsc(Long vanBanId);

    Page<XuLyVanBan> findByNguoiNhanIdAndTrangThaiXuLy(Long nguoiNhanId, Integer trangThaiXuLy, Pageable pageable);

    Optional<XuLyVanBan> findTopByVanBanIdOrderByIdDesc(Long vanBanId);
}
