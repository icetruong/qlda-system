package com.qlda.workflowservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record CompleteProcessingRequest(
        @Size(max = 1000) String yKienXuLy,
        @Size(max = 500) String tepKetQua,
        @Min(0) @Max(100) Integer tyLeHoanThanh
) {
}
