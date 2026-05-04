package com.qlda.workflowservice.dto.internal.response;

public record InternalWorkflowSubmitApprovalResponse(
        Long documentId,
        Long processingId,
        Long nguoiPheDuyetId,
        Integer trangThaiXuLy
) {
}
