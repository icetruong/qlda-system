package com.qlda.workflowservice.client.dto;

import java.util.List;

public record AuthUserRolesDto(
        Long userId,
        List<String> roles,
        List<AuthPermissionDto> permissions
) {
}
