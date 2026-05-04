package com.qlda.workflowservice.dto.internal.response;

import java.time.LocalDateTime;

public record InternalWorkflowTimelineItemResponse(
        Long processingId,
        String tenBuoc,
        Long nguoiXuLyId,
        String hanhDongXuLy,
        LocalDateTime ngayNhan,
        LocalDateTime ngayHoanThanh,
        Integer trangThaiXuLy
) {
}
