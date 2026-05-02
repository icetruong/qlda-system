package com.qlda.workflowservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record WorkflowStepUpdateRequest(
        @NotBlank @Size(max = 255) String tenBuoc,
        @NotNull @Positive Integer thuTuBuoc,
        @Size(max = 100) String vaiTroXuLy,
        Integer thoiGianXuLy,
        Boolean batBuocPheDuyet,
        @Size(max = 500) String ghiChu
) {
}
