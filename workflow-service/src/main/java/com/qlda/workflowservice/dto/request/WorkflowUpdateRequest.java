package com.qlda.workflowservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkflowUpdateRequest(
        @NotBlank @Size(max = 255) String tenQuyTrinh,
        Integer loaiVanBanId,
        @Size(max = 1000) String moTa,
        Boolean suDung
) {
}
