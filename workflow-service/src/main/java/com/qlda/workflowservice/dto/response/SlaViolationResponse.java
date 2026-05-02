package com.qlda.workflowservice.dto.response;

import java.time.LocalDateTime;

public record SlaViolationResponse(
        Long processingId,
        Long documentId,
        String trichYeu,
        Long nguoiNhanId,
        LocalDateTime hanXuLy,
        LocalDateTime ngayHoanThanh,
        long soGioTre
) {
}
