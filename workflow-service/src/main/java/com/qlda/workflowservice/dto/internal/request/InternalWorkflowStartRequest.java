package com.qlda.workflowservice.dto.internal.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record InternalWorkflowStartRequest(
        @NotNull Integer workflowId,
        @NotNull Long nguoiTaoId,
        Integer donViChuTriId,
        String documentType,
        LocalDateTime hanXuLy
) {
}
