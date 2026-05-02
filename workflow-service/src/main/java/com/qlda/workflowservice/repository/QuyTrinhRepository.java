package com.qlda.workflowservice.repository;

import com.qlda.workflowservice.entity.QuyTrinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface QuyTrinhRepository extends JpaRepository<QuyTrinh, Integer>, JpaSpecificationExecutor<QuyTrinh> {
    Optional<QuyTrinh> findByMaQuyTrinh(String maQuyTrinh);

    boolean existsByMaQuyTrinh(String maQuyTrinh);

    Optional<QuyTrinh> findByIdAndSuDungTrue(Integer id);
}
