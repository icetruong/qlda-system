package com.qlda.authservice.service;

import com.qlda.authservice.common.ErrorCode;
import com.qlda.authservice.dto.internal.request.InternalPermissionCheckRequest;
import com.qlda.authservice.dto.internal.request.ValidateUnitsRequest;
import com.qlda.authservice.dto.internal.request.ValidateUsersRequest;
import com.qlda.authservice.dto.internal.response.InternalPermissionCheckResponse;
import com.qlda.authservice.dto.internal.response.InternalPermissionResponse;
import com.qlda.authservice.dto.internal.response.InternalUnitResponse;
import com.qlda.authservice.dto.internal.response.InternalUserCountResponse;
import com.qlda.authservice.dto.internal.response.InternalUserResponse;
import com.qlda.authservice.dto.internal.response.InternalUserRolesResponse;
import com.qlda.authservice.dto.internal.response.ValidateUnitsResponse;
import com.qlda.authservice.dto.internal.response.ValidateUsersResponse;
import com.qlda.authservice.entity.DonVi;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.PhanQuyen;
import com.qlda.authservice.exception.ApiException;
import com.qlda.authservice.repository.DonViRepository;
import com.qlda.authservice.repository.NguoiDungRepository;
import com.qlda.authservice.repository.PhanQuyenRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class InternalAuthService {

    private final NguoiDungRepository nguoiDungRepository;
    private final DonViRepository donViRepository;
    private final PhanQuyenRepository phanQuyenRepository;

    public InternalAuthService(
            NguoiDungRepository nguoiDungRepository,
            DonViRepository donViRepository,
            PhanQuyenRepository phanQuyenRepository
    ) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.donViRepository = donViRepository;
        this.phanQuyenRepository = phanQuyenRepository;
    }

    public InternalUserResponse getInternalUser(Long id) {
        NguoiDung user = findActiveUserById(id);
        return new InternalUserResponse(
                user.getId(),
                user.getUserName(),
                user.getHoTen(),
                user.getEmail(),
                user.getDonVi() == null ? null : user.getDonVi().getId(),
                user.getDonVi() == null ? null : user.getDonVi().getTenDonVi(),
                user.getNhomQuyen() == null ? null : user.getNhomQuyen().getId(),
                user.getNhomQuyen() == null ? null : user.getNhomQuyen().getMaNhomQuyen(),
                user.getTrangThai()
        );
    }

    public ValidateUsersResponse validateUsers(ValidateUsersRequest request) {
        Set<Long> existingUserIds = nguoiDungRepository.findAllById(request.userIds()).stream()
                .filter(user -> user.getTrangThai() != null && user.getTrangThai() != -1)
                .map(NguoiDung::getId)
                .collect(java.util.stream.Collectors.toSet());
        List<Long> invalidUserIds = request.userIds().stream()
                .distinct()
                .filter(id -> !existingUserIds.contains(id))
                .sorted()
                .toList();
        return new ValidateUsersResponse(invalidUserIds.isEmpty(), invalidUserIds);
    }

    public InternalUnitResponse getInternalUnit(Integer id) {
        DonVi unit = donViRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.UNIT_NOT_FOUND, "Unit not found"));
        return new InternalUnitResponse(
                unit.getId(),
                unit.getMaDonVi(),
                unit.getTenDonVi(),
                unit.getDonViCha() == null ? null : unit.getDonViCha().getId(),
                unit.getSuDung()
        );
    }

    public ValidateUnitsResponse validateUnits(ValidateUnitsRequest request) {
        Set<Integer> existingUnitIds = donViRepository.findAllById(request.unitIds()).stream()
                .map(DonVi::getId)
                .collect(java.util.stream.Collectors.toSet());
        List<Integer> invalidUnitIds = request.unitIds().stream()
                .distinct()
                .filter(id -> !existingUnitIds.contains(id))
                .sorted()
                .toList();
        return new ValidateUnitsResponse(invalidUnitIds.isEmpty(), invalidUnitIds);
    }

    public InternalUserRolesResponse getUserRoles(Long userId) {
        NguoiDung user = findActiveUserById(userId);
        List<String> roles = user.getNhomQuyen() == null || !StringUtils.hasText(user.getNhomQuyen().getMaNhomQuyen())
                ? List.of()
                : List.of(user.getNhomQuyen().getMaNhomQuyen());
        List<InternalPermissionResponse> permissions = user.getNhomQuyen() == null
                ? List.of()
                : phanQuyenRepository.findByNhomQuyen_Id(user.getNhomQuyen().getId()).stream()
                .sorted(Comparator.comparing(permission -> permission.getChucNang().getMaChucNang()))
                .map(this::toPermissionResponse)
                .toList();
        return new InternalUserRolesResponse(user.getId(), roles, permissions);
    }

    public InternalPermissionCheckResponse checkPermission(InternalPermissionCheckRequest request) {
        NguoiDung user = findActiveUserById(request.userId());
        boolean allowed = false;
        if (user.getNhomQuyen() != null) {
            PhanQuyen permission = phanQuyenRepository
                    .findByNhomQuyenAndMaChucNang(user.getNhomQuyen().getId(), request.maChucNang())
                    .orElse(null);
            if (permission != null) {
                allowed = hasPermission(permission, request.permission());
            }
        }
        return new InternalPermissionCheckResponse(
                allowed,
                request.userId(),
                request.maChucNang(),
                request.permission()
        );
    }

    public InternalUserCountResponse getUserCount() {
        return new InternalUserCountResponse(nguoiDungRepository.countSystemUsers());
    }

    private NguoiDung findActiveUserById(Long id) {
        return nguoiDungRepository.findById(id)
                .filter(user -> user.getTrangThai() != null && user.getTrangThai() != -1)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND, "User not found"));
    }

    private InternalPermissionResponse toPermissionResponse(PhanQuyen permission) {
        return new InternalPermissionResponse(
                permission.getChucNang().getMaChucNang(),
                Boolean.TRUE.equals(permission.getIsView()),
                Boolean.TRUE.equals(permission.getIsCreate()),
                Boolean.TRUE.equals(permission.getIsEdit()),
                Boolean.TRUE.equals(permission.getIsDelete()),
                Boolean.TRUE.equals(permission.getIsApprove())
        );
    }

    private boolean hasPermission(PhanQuyen permission, String action) {
        if (!StringUtils.hasText(action)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "permission is required");
        }
        String normalized = action.trim().toUpperCase(Locale.ROOT).replace("IS", "");
        return switch (normalized) {
            case "VIEW" -> Boolean.TRUE.equals(permission.getIsView());
            case "CREATE" -> Boolean.TRUE.equals(permission.getIsCreate());
            case "EDIT" -> Boolean.TRUE.equals(permission.getIsEdit());
            case "DELETE" -> Boolean.TRUE.equals(permission.getIsDelete());
            case "APPROVE" -> Boolean.TRUE.equals(permission.getIsApprove());
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.PERMISSION_NOT_FOUND, "Permission type not found");
        };
    }
}
