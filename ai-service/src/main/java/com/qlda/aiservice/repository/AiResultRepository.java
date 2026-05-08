package com.qlda.aiservice.repository;

import com.qlda.aiservice.entity.AiProcessType;
import com.qlda.aiservice.entity.AiResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiResultRepository extends JpaRepository<AiResultEntity, Long> {
    Page<AiResultEntity> findByVanBanID(Long vanBanID, Pageable pageable);

    Page<AiResultEntity> findByVanBanIDAndLoaiXuLyAI(Long vanBanID, AiProcessType loaiXuLyAI, Pageable pageable);
}

