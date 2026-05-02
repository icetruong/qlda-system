package com.qlda.workflowservice.dto.request;

import jakarta.validation.constraints.Size;

public record ApprovalApproveRequest(
        @Size(max = 1000) String yKienXuLy,
        Boolean chuyenBuocTiepTheo
) {
}
