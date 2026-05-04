package com.qlda.workflowservice.client.dto;

public record AuthPermissionDto(
        String maChucNang,
        Boolean isView,
        Boolean isCreate,
        Boolean isEdit,
        Boolean isDelete,
        Boolean isApprove
) {
}
