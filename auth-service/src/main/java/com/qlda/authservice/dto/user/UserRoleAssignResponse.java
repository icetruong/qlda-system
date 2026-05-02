package com.qlda.authservice.dto.user;

public record UserRoleAssignResponse(
        Long userId,
        Integer nhomQuyenId
) {
}
