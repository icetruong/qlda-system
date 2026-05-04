package com.qlda.workflowservice.dto.internal.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record InternalWorkflowTransferRequest(
        @NotNull Long nguoiGuiId,
        @NotNull Long nguoiNhanId,
        Integer donViXuLyId,
        Long buocQuyTrinhId,
        @Size(max = 1000) String yKienXuLy,
        LocalDateTime hanXuLy
) {
}
