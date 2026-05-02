package com.qlda.documentservice.repository;

import com.qlda.documentservice.entity.TepDinhKem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TepDinhKemRepository extends JpaRepository<TepDinhKem, Long> {
    List<TepDinhKem> findByVanBan_Id(Long vanBanId);

    Optional<TepDinhKem> findByIdAndVanBan_Id(Long id, Long vanBanId);
}

