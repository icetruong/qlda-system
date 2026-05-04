package com.qlda.authservice.dto.internal.response;

public record InternalPermissionResponse(
        String maChucNang,
        boolean isView,
        boolean isCreate,
        boolean isEdit,
        boolean isDelete,
        boolean isApprove
) {
}
