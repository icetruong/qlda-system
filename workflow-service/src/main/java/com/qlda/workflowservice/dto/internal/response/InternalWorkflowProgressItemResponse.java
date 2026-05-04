package com.qlda.workflowservice.dto.internal.response;

import java.time.LocalDateTime;

public record InternalWorkflowProgressItemResponse(
        Long documentId,
        Long processingId,
        Long nguoiXuLyId,
        Integer trangThaiXuLy,
        Integer tyLeHoanThanh,
        LocalDateTime hanXuLy
) {
}
