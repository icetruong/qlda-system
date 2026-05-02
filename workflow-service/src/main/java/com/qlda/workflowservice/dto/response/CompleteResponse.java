package com.qlda.workflowservice.dto.response;

import java.time.LocalDateTime;

public record CompleteResponse(
        Long processingId,
        LocalDateTime ngayHoanThanh,
        Integer tyLeHoanThanh,
        Integer trangThaiXuLy
) {
}
