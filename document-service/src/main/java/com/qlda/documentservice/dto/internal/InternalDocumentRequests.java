package com.qlda.documentservice.dto.internal;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public final class InternalDocumentRequests {
    private InternalDocumentRequests() {
    }

    public record UpdateStatusRequest(
        @NotNull(message = "trangThai is required") Integer trangThai,
        String reason,
        String updatedByService
    ) {
    }

    public record UpdateAssigneeRequest(
        @NotNull(message = "nguoiXuLyId is required") Long nguoiXuLyId,
        Integer donViXuLyId,
        LocalDateTime hanXuLy
    ) {
    }

    public record UpdateWorkflowStatusRequest(
        @NotNull(message = "workflowStatus is required") String workflowStatus,
        String currentStep,
        Long processingId
    ) {
    }

    public record UpdateOcrStatusRequest(
        @NotNull(message = "daOCR is required") Boolean daOCR
    ) {
    }

    public record AccessCheckRequest(
        @NotNull(message = "userId is required") Long userId,
        @NotNull(message = "documentIds is required") List<Long> documentIds
    ) {
    }
}
