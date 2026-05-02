package com.qlda.workflowservice.dto.response;

import java.time.LocalDateTime;

public record ReceiveResponse(
        Long processingId,
        LocalDateTime receivedAt,
        Integer trangThaiXuLy
) {
}
