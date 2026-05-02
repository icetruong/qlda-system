package com.qlda.documentservice.repository;

import com.qlda.documentservice.entity.LoaiVanBan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoaiVanBanRepository extends JpaRepository<LoaiVanBan, Integer> {
    Optional<LoaiVanBan> findByMaLoaiVanBan(String maLoaiVanBan);

    boolean existsByMaLoaiVanBan(String maLoaiVanBan);

    List<LoaiVanBan> findBySuDung(Boolean suDung);
}

