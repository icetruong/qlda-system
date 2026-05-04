package com.qlda.workflowservice.dto.internal.response;

public record InternalWorkflowStartResponse(
        Long documentId,
        Integer workflowId,
        Long processingId,
        String currentStep,
        Integer trangThaiXuLy
) {
}
