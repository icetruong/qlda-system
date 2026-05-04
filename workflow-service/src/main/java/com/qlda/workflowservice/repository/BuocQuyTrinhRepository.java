package com.qlda.workflowservice.repository;

import com.qlda.workflowservice.entity.BuocQuyTrinh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuocQuyTrinhRepository extends JpaRepository<BuocQuyTrinh, Long> {
    Optional<BuocQuyTrinh> findFirstByQuyTrinh_IdOrderByThuTuBuocAsc(Integer quyTrinhId);

    List<BuocQuyTrinh> findByQuyTrinh_IdOrderByThuTuBuocAsc(Integer quyTrinhId);

    Optional<BuocQuyTrinh> findByIdAndQuyTrinh_Id(Long id, Integer quyTrinhId);

    boolean existsByQuyTrinh_IdAndThuTuBuoc(Integer quyTrinhId, Integer thuTuBuoc);

    long countByQuyTrinh_Id(Integer quyTrinhId);

    List<BuocQuyTrinh> findByQuyTrinh_IdAndThoiGianXuLyIsNotNullOrderByThuTuBuocAsc(Integer quyTrinhId);

    List<BuocQuyTrinh> findByThoiGianXuLyIsNotNullOrderByQuyTrinh_IdAscThuTuBuocAsc();
}
