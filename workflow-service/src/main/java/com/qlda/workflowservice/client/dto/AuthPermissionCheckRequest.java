package com.qlda.workflowservice.client.dto;

public record AuthPermissionCheckRequest(
        Long userId,
        String maChucNang,
        String permission
) {
}
