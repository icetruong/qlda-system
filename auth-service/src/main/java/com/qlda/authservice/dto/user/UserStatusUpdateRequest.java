package com.qlda.authservice.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(
        @NotNull(message = "trangThai is required")
        @Min(value = 0, message = "trangThai must be 0 or 1")
        @Max(value = 1, message = "trangThai must be 0 or 1")
        Integer trangThai
) {
}
