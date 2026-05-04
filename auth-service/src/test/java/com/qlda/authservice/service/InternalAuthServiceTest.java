package com.qlda.authservice.service;

import com.qlda.authservice.common.ErrorCode;
import com.qlda.authservice.dto.internal.request.InternalPermissionCheckRequest;
import com.qlda.authservice.dto.internal.request.ValidateUnitsRequest;
import com.qlda.authservice.dto.internal.request.ValidateUsersRequest;
import com.qlda.authservice.dto.internal.response.InternalPermissionCheckResponse;
import com.qlda.authservice.dto.internal.response.InternalUnitResponse;
import com.qlda.authservice.dto.internal.response.InternalUserResponse;
import com.qlda.authservice.dto.internal.response.InternalUserRolesResponse;
import com.qlda.authservice.dto.internal.response.ValidateUnitsResponse;
import com.qlda.authservice.dto.internal.response.ValidateUsersResponse;
import com.qlda.authservice.entity.ChucNang;
import com.qlda.authservice.entity.DonVi;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.NhomQuyen;
import com.qlda.authservice.entity.PhanQuyen;
import com.qlda.authservice.exception.ApiException;
import com.qlda.authservice.repository.DonViRepository;
import com.qlda.authservice.repository.NguoiDungRepository;
import com.qlda.authservice.repository.PhanQuyenRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalAuthServiceTest {

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private DonViRepository donViRepository;

    @Mock
    private PhanQuyenRepository phanQuyenRepository;

    private InternalAuthService internalAuthService;

    @BeforeEach
    void setUp() {
        internalAuthService = new InternalAuthService(nguoiDungRepository, donViRepository, phanQuyenRepository);
    }

    @Test
    void getInternalUserByIdShouldReturnSuccess() {
        NguoiDung user = buildUser(1L);
        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(user));

        InternalUserResponse response = internalAuthService.getInternalUser(1L);

        assertEquals(1L, response.id());
        assertEquals("nva", response.username());
        assertEquals("CHUYEN_VIEN", response.maNhomQuyen());
    }

    @Test
    void getInternalUserByIdShouldThrowWhenUserNotFound() {
        when(nguoiDungRepository.findById(100L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> internalAuthService.getInternalUser(100L));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void validateUsersShouldReturnInvalidUserIds() {
        NguoiDung user = buildUser(1L);
        when(nguoiDungRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(user));

        ValidateUsersResponse response = internalAuthService.validateUsers(new ValidateUsersRequest(List.of(1L, 2L, 3L)));

        assertFalse(response.valid());
        assertEquals(List.of(2L, 3L), response.invalidUserIds());
    }

    @Test
    void getUnitByIdShouldReturnSuccess() {
        DonVi unit = buildUnit(1);
        when(donViRepository.findById(1)).thenReturn(Optional.of(unit));

        InternalUnitResponse response = internalAuthService.getInternalUnit(1);

        assertEquals(1, response.id());
        assertEquals("DV01", response.maDonVi());
    }

    @Test
    void getUnitByIdShouldThrowWhenUnitNotFound() {
        when(donViRepository.findById(999)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> internalAuthService.getInternalUnit(999));
        assertEquals(ErrorCode.UNIT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void validateUnitsShouldReturnInvalidUnitIds() {
        DonVi unit = buildUnit(1);
        when(donViRepository.findAllById(List.of(1, 2, 3))).thenReturn(List.of(unit));

        ValidateUnitsResponse response = internalAuthService.validateUnits(new ValidateUnitsRequest(List.of(1, 2, 3)));

        assertFalse(response.valid());
        assertEquals(List.of(2, 3), response.invalidUnitIds());
    }

    @Test
    void getUserRolesShouldReturnRolesWithPermissions() {
        NguoiDung user = buildUser(1L);
        PhanQuyen permission = buildPermission(true, false, true, false, false);
        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(user));
        when(phanQuyenRepository.findByNhomQuyen_Id(2)).thenReturn(List.of(permission));

        InternalUserRolesResponse response = internalAuthService.getUserRoles(1L);

        assertEquals(1L, response.userId());
        assertEquals(List.of("CHUYEN_VIEN"), response.roles());
        assertEquals(1, response.permissions().size());
        assertEquals("DOCUMENT_INCOMING", response.permissions().get(0).maChucNang());
    }

    @Test
    void checkPermissionShouldSupportIsView() {
        InternalPermissionCheckResponse response = doPermissionCheck("IsView", true, false, false, false, false);
        assertTrue(response.allowed());
    }

    @Test
    void checkPermissionShouldSupportIsCreate() {
        InternalPermissionCheckResponse response = doPermissionCheck("IsCreate", false, true, false, false, false);
        assertTrue(response.allowed());
    }

    @Test
    void checkPermissionShouldSupportIsEdit() {
        InternalPermissionCheckResponse response = doPermissionCheck("IsEdit", false, false, true, false, false);
        assertTrue(response.allowed());
    }

    @Test
    void checkPermissionShouldSupportIsDelete() {
        InternalPermissionCheckResponse response = doPermissionCheck("IsDelete", false, false, false, true, false);
        assertTrue(response.allowed());
    }

    @Test
    void checkPermissionShouldSupportIsApprove() {
        InternalPermissionCheckResponse response = doPermissionCheck("IsApprove", false, false, false, false, true);
        assertTrue(response.allowed());
    }

    private InternalPermissionCheckResponse doPermissionCheck(
            String action,
            boolean isView,
            boolean isCreate,
            boolean isEdit,
            boolean isDelete,
            boolean isApprove
    ) {
        NguoiDung user = buildUser(1L);
        PhanQuyen permission = buildPermission(isView, isCreate, isEdit, isDelete, isApprove);
        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(user));
        when(phanQuyenRepository.findByNhomQuyenAndMaChucNang(2, "DOCUMENT_INCOMING")).thenReturn(Optional.of(permission));
        return internalAuthService.checkPermission(new InternalPermissionCheckRequest(1L, "DOCUMENT_INCOMING", action));
    }

    private NguoiDung buildUser(Long id) {
        DonVi donVi = buildUnit(1);
        NhomQuyen nhomQuyen = new NhomQuyen();
        nhomQuyen.setId(2);
        nhomQuyen.setMaNhomQuyen("CHUYEN_VIEN");

        NguoiDung user = new NguoiDung();
        user.setId(id);
        user.setUserName("nva");
        user.setHoTen("Nguyen Van A");
        user.setEmail("nva@company.com");
        user.setDonVi(donVi);
        user.setNhomQuyen(nhomQuyen);
        user.setTrangThai(1);
        return user;
    }

    private DonVi buildUnit(Integer id) {
        DonVi donVi = new DonVi();
        donVi.setId(id);
        donVi.setMaDonVi("DV01");
        donVi.setTenDonVi("Phong HC");
        donVi.setSuDung(true);
        return donVi;
    }

    private PhanQuyen buildPermission(
            boolean isView,
            boolean isCreate,
            boolean isEdit,
            boolean isDelete,
            boolean isApprove
    ) {
        ChucNang chucNang = new ChucNang();
        chucNang.setMaChucNang("DOCUMENT_INCOMING");

        NhomQuyen nhomQuyen = new NhomQuyen();
        nhomQuyen.setId(2);

        PhanQuyen phanQuyen = new PhanQuyen();
        phanQuyen.setNhomQuyen(nhomQuyen);
        phanQuyen.setChucNang(chucNang);
        phanQuyen.setIsView(isView);
        phanQuyen.setIsCreate(isCreate);
        phanQuyen.setIsEdit(isEdit);
        phanQuyen.setIsDelete(isDelete);
        phanQuyen.setIsApprove(isApprove);
        return phanQuyen;
    }
}
