package com.qlda.workflowservice.dto.response;

import java.util.List;

public record WorkflowDetailResponse(
        Integer id,
        String maQuyTrinh,
        String tenQuyTrinh,
        Integer loaiVanBanId,
        String moTa,
        Integer soBuoc,
        Boolean suDung,
        List<WorkflowStepResponse> steps
) {
}
