package com.qlda.authservice.repository;

import com.qlda.authservice.entity.NhomQuyen;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NhomQuyenRepository extends JpaRepository<NhomQuyen, Integer> {

    Optional<NhomQuyen> findByMaNhomQuyen(String maNhomQuyen);

    boolean existsByMaNhomQuyen(String maNhomQuyen);
}
