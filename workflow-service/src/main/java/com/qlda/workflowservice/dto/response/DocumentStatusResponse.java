package com.qlda.workflowservice.dto.response;

import java.time.LocalDateTime;

public record DocumentStatusResponse(
        Long documentId,
        String currentStep,
        Integer trangThaiXuLy,
        Integer tyLeHoanThanh,
        LocalDateTime hanXuLy,
        Boolean isOverdue
) {
}
