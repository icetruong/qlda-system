package com.qlda.workflowservice.dto.internal.response;

import java.time.LocalDateTime;

public record InternalWorkflowStatusResponse(
        Long documentId,
        String currentStep,
        Integer trangThaiXuLy,
        Integer tyLeHoanThanh,
        LocalDateTime hanXuLy,
        Boolean isOverdue
) {
}
