package com.qlda.documentservice.client.dto;

import java.util.List;

public final class AuthClientDtos {
    private AuthClientDtos() {
    }

    public record UserInfoResponse(
        Long id,
        String username,
        String hoTen,
        String email,
        Integer donViId,
        String tenDonVi,
        Integer nhomQuyenId,
        String maNhomQuyen,
        Integer trangThai
    ) {
    }

    public record ValidateUsersRequest(List<Long> userIds) {
    }

    public record ValidateUsersResponse(Boolean valid, List<Long> invalidUserIds) {
    }

    public record UnitInfoResponse(
        Integer id,
        String maDonVi,
        String tenDonVi,
        Integer donViChaId,
        Boolean suDung
    ) {
    }

    public record ValidateUnitsRequest(List<Integer> unitIds) {
    }

    public record ValidateUnitsResponse(Boolean valid, List<Integer> invalidUnitIds) {
    }

    public record PermissionItemResponse(
        String maChucNang,
        Boolean isView,
        Boolean isCreate,
        Boolean isEdit,
        Boolean isDelete,
        Boolean isApprove
    ) {
    }

    public record UserRolesResponse(
        Long userId,
        List<String> roles,
        List<PermissionItemResponse> permissions
    ) {
    }

    public record CheckPermissionRequest(
        Long userId,
        String maChucNang,
        String permission
    ) {
    }

    public record CheckPermissionResponse(
        Boolean allowed,
        Long userId,
        String maChucNang,
        String permission
    ) {
    }
}
