package com.qlda.workflowservice.dto.response;

public record SlaResponse(
        Integer workflowId,
        Long stepId,
        String tenBuoc,
        Integer thoiGianXuLy,
        String donViThoiGian
) {
}
