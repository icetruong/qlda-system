package com.qlda.authservice.repository;

import com.qlda.authservice.entity.ChucNang;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChucNangRepository extends JpaRepository<ChucNang, Integer> {

    Optional<ChucNang> findByMaChucNang(String maChucNang);

    boolean existsByMaChucNang(String maChucNang);
}
