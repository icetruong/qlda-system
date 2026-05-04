package com.qlda.authservice.dto.internal.response;

import java.util.List;

public record InternalUserRolesResponse(
        Long userId,
        List<String> roles,
        List<InternalPermissionResponse> permissions
) {
}
