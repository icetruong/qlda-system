package com.qlda.authservice.dto.user;

import jakarta.validation.constraints.NotNull;

public record UserRoleAssignRequest(
        @NotNull(message = "nhomQuyenId is required")
        Integer nhomQuyenId
) {
}
