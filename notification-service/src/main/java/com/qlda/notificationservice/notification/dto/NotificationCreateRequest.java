package com.qlda.notificationservice.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationCreateRequest(
    @NotBlank(message = "tieuDe is required")
    String tieuDe,
    @NotBlank(message = "noiDung is required")
    String noiDung,
    @NotNull(message = "nguoiNhanId is required")
    Long nguoiNhanId,
    Long vanBanId,
    String loaiThongBao,
    String kenhGui
) {
}

