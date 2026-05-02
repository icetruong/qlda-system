package com.qlda.workflowservice.dto.response;

public record WorkflowStepResponse(
        Long id,
        Integer workflowId,
        String tenBuoc,
        Integer thuTuBuoc,
        String vaiTroXuLy,
        Integer thoiGianXuLy,
        Boolean batBuocPheDuyet,
        String ghiChu
) {
}
