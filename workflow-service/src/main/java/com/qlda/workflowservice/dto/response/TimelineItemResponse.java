package com.qlda.workflowservice.dto.response;

import java.time.LocalDateTime;

public record TimelineItemResponse(
        Long processingId,
        String tenBuoc,
        String nguoiXuLy,
        String hanhDongXuLy,
        String yKienXuLy,
        LocalDateTime ngayNhan,
        LocalDateTime ngayHoanThanh,
        Integer trangThaiXuLy
) {
}
