package com.qlda.workflowservice.dto.internal.response;

public record InternalWorkflowTransferResponse(
        Long processingId,
        Long documentId,
        Long nguoiNhanId,
        Integer trangThaiXuLy
) {
}
