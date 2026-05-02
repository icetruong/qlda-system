package com.qlda.workflowservice.dto.response;

public record WorkflowSummaryResponse(
        Integer id,
        String maQuyTrinh,
        String tenQuyTrinh,
        Integer loaiVanBanId,
        Integer soBuoc,
        Boolean suDung
) {
}
