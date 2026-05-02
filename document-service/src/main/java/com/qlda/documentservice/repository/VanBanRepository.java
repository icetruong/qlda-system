package com.qlda.documentservice.repository;

import com.qlda.documentservice.entity.VanBan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VanBanRepository extends JpaRepository<VanBan, Long>, JpaSpecificationExecutor<VanBan> {
    boolean existsBySoKyHieuAndDaXoaFalse(String soKyHieu);

    Optional<VanBan> findByIdAndDaXoaFalse(Long id);

    Page<VanBan> findByPhanLoaiVanBanAndDaXoaFalse(Integer phanLoaiVanBan, Pageable pageable);

    List<VanBan> findBySoKyHieuAndDaXoaFalse(String soKyHieu);
}

