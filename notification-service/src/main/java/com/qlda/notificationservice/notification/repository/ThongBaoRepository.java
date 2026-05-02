package com.qlda.notificationservice.notification.repository;

import com.qlda.notificationservice.notification.entity.ThongBao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThongBaoRepository extends JpaRepository<ThongBao, Long> {

    Page<ThongBao> findByNguoiNhanId(Long nguoiNhanId, Pageable pageable);

    Page<ThongBao> findByNguoiNhanIdAndDaDoc(Long nguoiNhanId, Boolean daDoc, Pageable pageable);

    @Query("select count(tb) from ThongBao tb where tb.nguoiNhanId = :nguoiNhanId and tb.daDoc = false")
    long countUnread(@Param("nguoiNhanId") Long nguoiNhanId);
}

