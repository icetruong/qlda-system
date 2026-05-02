package com.qlda.workflowservice.dto.response;

import java.time.LocalDateTime;

public record ApprovalActionResponse(
        Long processingId,
        Long documentId,
        Integer trangThaiXuLy,
        String noiDung,
        LocalDateTime ngayHoanThanh
) {
}
