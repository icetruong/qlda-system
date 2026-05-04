package com.qlda.authservice.dto.internal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InternalPermissionCheckRequest(
        @NotNull(message = "userId is required")
        Long userId,
        @NotBlank(message = "maChucNang is required")
        String maChucNang,
        @NotBlank(message = "permission is required")
        String permission
) {
}
