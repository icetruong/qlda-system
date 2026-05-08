package com.qlda.aiservice.repository;

import com.qlda.aiservice.entity.AiDocumentChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiDocumentChunkRepository extends JpaRepository<AiDocumentChunkEntity, Long> {
    List<AiDocumentChunkEntity> findByVanBanId(Long vanBanId);

    void deleteByVanBanId(Long vanBanId);
}

