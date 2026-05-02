package com.qlda.authservice.repository;

import com.qlda.authservice.entity.PhanQuyen;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhanQuyenRepository extends JpaRepository<PhanQuyen, Integer> {

    Optional<PhanQuyen> findByNhomQuyen_IdAndChucNang_Id(Integer nhomQuyenId, Integer chucNangId);

    List<PhanQuyen> findByNhomQuyen_Id(Integer nhomQuyenId);

    boolean existsByNhomQuyen_IdAndChucNang_Id(Integer nhomQuyenId, Integer chucNangId);

    @Query("""
            select p from PhanQuyen p
            where p.nhomQuyen.id = :nhomQuyenId
            and p.chucNang.maChucNang = :maChucNang
            """)
    Optional<PhanQuyen> findByNhomQuyenAndMaChucNang(
            @Param("nhomQuyenId") Integer nhomQuyenId,
            @Param("maChucNang") String maChucNang
    );
}
