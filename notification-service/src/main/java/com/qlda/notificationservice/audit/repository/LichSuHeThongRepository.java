package com.qlda.notificationservice.audit.repository;

import com.qlda.notificationservice.audit.entity.LichSuHeThong;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LichSuHeThongRepository extends JpaRepository<LichSuHeThong, Long> {

    Page<LichSuHeThong> findByNguoiDungId(Long nguoiDungId, Pageable pageable);

    Page<LichSuHeThong> findByDoiTuong(String doiTuong, Pageable pageable);

    @Query("""
        select l from LichSuHeThong l
        where (:nguoiDungId is null or l.nguoiDungId = :nguoiDungId)
          and (:doiTuong is null or l.doiTuong = :doiTuong)
          and (:fromDate is null or l.thoiGianThucHien >= :fromDate)
          and (:toDate is null or l.thoiGianThucHien <= :toDate)
    """)
    Page<LichSuHeThong> filter(
        @Param("nguoiDungId") Long nguoiDungId,
        @Param("doiTuong") String doiTuong,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable
    );
}

