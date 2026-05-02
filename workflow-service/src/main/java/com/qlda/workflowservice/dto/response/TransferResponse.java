package com.qlda.workflowservice.dto.response;

public record TransferResponse(
        Long processingId,
        Long documentId,
        Long nguoiNhanId,
        Integer trangThaiXuLy
) {
}
