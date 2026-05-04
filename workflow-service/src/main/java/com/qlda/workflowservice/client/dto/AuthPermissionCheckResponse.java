package com.qlda.workflowservice.client.dto;

public record AuthPermissionCheckResponse(
        boolean allowed,
        Long userId,
        String maChucNang,
        String permission
) {
}
