package com.qlda.workflowservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReceiveProcessingRequest(
        @NotNull Long nguoiNhanId,
        @Size(max = 500) String ghiChu
) {
}
