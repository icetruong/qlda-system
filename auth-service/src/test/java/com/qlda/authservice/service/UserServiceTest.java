package com.qlda.authservice.service;

import com.qlda.authservice.common.ErrorCode;
import com.qlda.authservice.dto.common.IdResponse;
import com.qlda.authservice.dto.user.UserCreateRequest;
import com.qlda.authservice.dto.user.UserRoleAssignRequest;
import com.qlda.authservice.dto.user.UserUpdateRequest;
import com.qlda.authservice.entity.DonVi;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.NhomQuyen;
import com.qlda.authservice.exception.ApiException;
import com.qlda.authservice.repository.DonViRepository;
import com.qlda.authservice.repository.NguoiDungRepository;
import com.qlda.authservice.repository.NhomQuyenRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private DonViRepository donViRepository;

    @Mock
    private NhomQuyenRepository nhomQuyenRepository;

    @Mock
    private AuditLogService auditLogService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(nguoiDungRepository, donViRepository, nhomQuyenRepository, auditLogService);
    }

    @Test
    void createUserShouldSuccess() {
        when(nguoiDungRepository.existsByUserName("newuser")).thenReturn(false);
        when(nguoiDungRepository.existsByEmail("new@company.com")).thenReturn(false);
        when(donViRepository.findById(1)).thenReturn(Optional.of(buildDonVi(1)));
        when(nhomQuyenRepository.findById(2)).thenReturn(Optional.of(buildRole(2)));
        when(nguoiDungRepository.save(any(NguoiDung.class))).thenAnswer(invocation -> {
            NguoiDung user = invocation.getArgument(0);
            user.setId(100L);
            return user;
        });

        IdResponse response = userService.createUser(new UserCreateRequest(
                "newuser", "New User", "new@company.com", "0909", 1, "Staff", 2, "azure-id"
        ));

        assertEquals(100L, response.id());
    }

    @Test
    void createUserShouldThrowWhenDuplicateUsername() {
        when(nguoiDungRepository.existsByUserName("dupuser")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> userService.createUser(new UserCreateRequest(
                "dupuser", "New User", "new@company.com", "0909", 1, "Staff", 2, "azure-id"
        )));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(ErrorCode.DUPLICATE_USERNAME, exception.getErrorCode());
    }

    @Test
    void createUserShouldThrowWhenDuplicateEmail() {
        when(nguoiDungRepository.existsByUserName("newuser")).thenReturn(false);
        when(nguoiDungRepository.existsByEmail("dup@company.com")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> userService.createUser(new UserCreateRequest(
                "newuser", "New User", "dup@company.com", "0909", 1, "Staff", 2, "azure-id"
        )));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(ErrorCode.DUPLICATE_EMAIL, exception.getErrorCode());
    }

    @Test
    void updateUserShouldSuccess() {
        NguoiDung user = buildUser(1L, 2);
        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(user));
        when(nguoiDungRepository.existsByUserNameAndIdNot("nva", 1L)).thenReturn(false);
        when(nguoiDungRepository.existsByEmailAndIdNot("update@company.com", 1L)).thenReturn(false);
        when(donViRepository.findById(1)).thenReturn(Optional.of(buildDonVi(1)));
        when(nhomQuyenRepository.findById(3)).thenReturn(Optional.of(buildRole(3)));

        IdResponse response = userService.updateUser(1L, new UserUpdateRequest(
                "Update Name", "update@company.com", "0909", 1, "Lead", 3, 1
        ));

        assertEquals(1L, response.id());
    }

    @Test
    void deleteUserShouldSoftDeleteTrangThaiMinusOne() {
        NguoiDung user = buildUser(1L, 2);
        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        ArgumentCaptor<NguoiDung> captor = ArgumentCaptor.forClass(NguoiDung.class);
        verify(nguoiDungRepository).save(captor.capture());
        assertEquals(-1, captor.getValue().getTrangThai());
    }

    @Test
    void assignRoleShouldSuccess() {
        NguoiDung user = buildUser(1L, 2);
        NhomQuyen role = buildRole(3);
        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(user));
        when(nhomQuyenRepository.findById(3)).thenReturn(Optional.of(role));

        var response = userService.assignRole(1L, new UserRoleAssignRequest(3));

        assertEquals(1L, response.userId());
        assertEquals(3, response.nhomQuyenId());
    }

    @Test
    void assignRoleShouldThrowWhenRoleNotFound() {
        NguoiDung user = buildUser(1L, 2);
        when(nguoiDungRepository.findById(1L)).thenReturn(Optional.of(user));
        when(nhomQuyenRepository.findById(999)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> userService.assignRole(1L, new UserRoleAssignRequest(999)));

        assertEquals(ErrorCode.ROLE_NOT_FOUND, exception.getErrorCode());
    }

    private NguoiDung buildUser(Long id, Integer roleId) {
        NguoiDung user = new NguoiDung();
        user.setId(id);
        user.setUserName("nva");
        user.setHoTen("Nguyen Van A");
        user.setEmail("nva@company.com");
        user.setTrangThai(1);
        user.setDonVi(buildDonVi(1));
        user.setNhomQuyen(buildRole(roleId));
        return user;
    }

    private DonVi buildDonVi(Integer id) {
        DonVi donVi = new DonVi();
        donVi.setId(id);
        donVi.setMaDonVi("DV01");
        donVi.setTenDonVi("Phong HC");
        donVi.setSuDung(true);
        return donVi;
    }

    private NhomQuyen buildRole(Integer id) {
        NhomQuyen role = new NhomQuyen();
        role.setId(id);
        role.setMaNhomQuyen("ROLE_" + id);
        role.setTenNhomQuyen("Role " + id);
        role.setSuDung(true);
        return role;
    }
}
