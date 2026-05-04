package com.qlda.authservice.dto.internal.response;

public record InternalPermissionCheckResponse(
        boolean allowed,
        Long userId,
        String maChucNang,
        String permission
) {
}
