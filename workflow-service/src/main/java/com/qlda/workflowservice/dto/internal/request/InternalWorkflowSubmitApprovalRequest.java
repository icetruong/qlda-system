package com.qlda.workflowservice.dto.internal.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InternalWorkflowSubmitApprovalRequest(
        @NotNull Long nguoiTrinhId,
        @NotNull Long nguoiPheDuyetId,
        @Size(max = 1000) String noiDungTrinh
) {
}
