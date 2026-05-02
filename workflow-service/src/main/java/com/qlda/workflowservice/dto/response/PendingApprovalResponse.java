package com.qlda.workflowservice.dto.response;

import java.time.LocalDateTime;

public record PendingApprovalResponse(
        Long processingId,
        Long documentId,
        String soKyHieu,
        String trichYeu,
        Long nguoiGuiId,
        String nguoiGuiTen,
        LocalDateTime ngayNhan,
        LocalDateTime hanXuLy,
        Integer trangThaiXuLy
) {
}
