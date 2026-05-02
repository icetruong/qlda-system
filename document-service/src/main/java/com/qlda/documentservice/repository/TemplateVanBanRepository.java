package com.qlda.documentservice.repository;

import com.qlda.documentservice.entity.TemplateVanBan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TemplateVanBanRepository extends JpaRepository<TemplateVanBan, Integer>, JpaSpecificationExecutor<TemplateVanBan> {
    Optional<TemplateVanBan> findByMaTemplate(String maTemplate);

    boolean existsByMaTemplate(String maTemplate);

    Optional<TemplateVanBan> findByIdAndSuDungTrue(Integer id);
}

