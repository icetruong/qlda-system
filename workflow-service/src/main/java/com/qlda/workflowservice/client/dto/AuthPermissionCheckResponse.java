package com.qlda.workflowservice.client.dto;

public record AuthPermissionCheckResponse(
        boolean allowed,
        String maChucNang,
        String permission
) {
}
