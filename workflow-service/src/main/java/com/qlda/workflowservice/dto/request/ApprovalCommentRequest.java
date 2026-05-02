package com.qlda.workflowservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApprovalCommentRequest(
        @NotBlank @Size(max = 1000) String noiDungGopY
) {
}
