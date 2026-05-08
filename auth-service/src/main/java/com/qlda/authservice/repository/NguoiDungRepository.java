package com.qlda.authservice.repository;

import com.qlda.authservice.entity.NguoiDung;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long>, JpaSpecificationExecutor<NguoiDung> {

    Optional<NguoiDung> findByUserName(String userName);

    Optional<NguoiDung> findByEmail(String email);

    Optional<NguoiDung> findByAzureAdId(String azureAdId);

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    boolean existsByUserNameAndIdNot(String userName, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<NguoiDung> findByDonVi_Id(Integer donViId);

    List<NguoiDung> findByNhomQuyen_Id(Integer nhomQuyenId);

    @Query("select count(u) from NguoiDung u where u.trangThai is null or u.trangThai <> -1")
    long countSystemUsers();
}
