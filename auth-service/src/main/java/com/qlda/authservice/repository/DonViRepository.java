package com.qlda.authservice.repository;

import com.qlda.authservice.entity.DonVi;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonViRepository extends JpaRepository<DonVi, Integer> {

    Optional<DonVi> findByMaDonVi(String maDonVi);

    boolean existsByMaDonVi(String maDonVi);
}
