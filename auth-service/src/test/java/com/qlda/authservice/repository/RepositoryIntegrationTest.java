package com.qlda.authservice.repository;

import com.qlda.authservice.entity.ChucNang;
import com.qlda.authservice.entity.DonVi;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.NhomQuyen;
import com.qlda.authservice.entity.PhanQuyen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class RepositoryIntegrationTest {

    @Autowired
    private DonViRepository donViRepository;
    @Autowired
    private NhomQuyenRepository nhomQuyenRepository;
    @Autowired
    private NguoiDungRepository nguoiDungRepository;
    @Autowired
    private ChucNangRepository chucNangRepository;
    @Autowired
    private PhanQuyenRepository phanQuyenRepository;

    @BeforeEach
    void initData() {
        phanQuyenRepository.deleteAll();
        nguoiDungRepository.deleteAll();
        chucNangRepository.deleteAll();
        nhomQuyenRepository.deleteAll();
        donViRepository.deleteAll();
    }

    @Test
    void nguoiDungRepositoryShouldFindByUsernameEmailAndAzureId() {
        DonVi donVi = new DonVi();
        donVi.setMaDonVi("DV01");
        donVi.setTenDonVi("Don vi 1");
        donVi.setSuDung(true);
        DonVi savedDonVi = donViRepository.save(donVi);

        NhomQuyen role = new NhomQuyen();
        role.setMaNhomQuyen("ADMIN");
        role.setTenNhomQuyen("Admin");
        role.setSuDung(true);
        NhomQuyen savedRole = nhomQuyenRepository.save(role);

        NguoiDung user = new NguoiDung();
        user.setUserName("admin");
        user.setHoTen("Admin");
        user.setEmail("admin@company.com");
        user.setAzureAdId("azure-1");
        user.setTrangThai(1);
        user.setDonVi(savedDonVi);
        user.setNhomQuyen(savedRole);
        nguoiDungRepository.save(user);

        assertTrue(nguoiDungRepository.findByUserName("admin").isPresent());
        assertTrue(nguoiDungRepository.findByEmail("admin@company.com").isPresent());
        assertTrue(nguoiDungRepository.findByAzureAdId("azure-1").isPresent());
        assertTrue(nguoiDungRepository.existsByUserName("admin"));
        assertTrue(nguoiDungRepository.existsByEmail("admin@company.com"));
        assertEquals(1, nguoiDungRepository.findByDonVi_Id(savedDonVi.getId()).size());
        assertEquals(1, nguoiDungRepository.findByNhomQuyen_Id(savedRole.getId()).size());
    }

    @Test
    void phanQuyenRepositoryShouldFindByRoleAndFunctionCode() {
        NhomQuyen role = new NhomQuyen();
        role.setMaNhomQuyen("ADMIN");
        role.setTenNhomQuyen("Admin");
        role.setSuDung(true);
        NhomQuyen savedRole = nhomQuyenRepository.save(role);

        ChucNang function = new ChucNang();
        function.setMaChucNang("USER_MANAGE");
        function.setTenChucNang("User Management");
        function.setSuDung(true);
        ChucNang savedFunction = chucNangRepository.save(function);

        PhanQuyen permission = new PhanQuyen();
        permission.setNhomQuyen(savedRole);
        permission.setChucNang(savedFunction);
        permission.setIsView(true);
        phanQuyenRepository.save(permission);

        assertTrue(phanQuyenRepository
                .findByNhomQuyen_IdAndChucNang_Id(savedRole.getId(), savedFunction.getId())
                .isPresent());
        assertTrue(phanQuyenRepository
                .findByNhomQuyenAndMaChucNang(savedRole.getId(), "USER_MANAGE")
                .isPresent());
        assertTrue(phanQuyenRepository
                .existsByNhomQuyen_IdAndChucNang_Id(savedRole.getId(), savedFunction.getId()));
    }
}
