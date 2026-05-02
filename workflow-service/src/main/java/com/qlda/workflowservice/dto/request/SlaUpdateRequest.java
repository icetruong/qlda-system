package com.qlda.workflowservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SlaUpdateRequest(
        @NotNull @Positive Integer thoiGianXuLy,
        @Size(max = 20) String donViThoiGian,
        @Size(max = 500) String ghiChu
) {
}
