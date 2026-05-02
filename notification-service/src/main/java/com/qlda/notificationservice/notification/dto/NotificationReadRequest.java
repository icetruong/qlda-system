package com.qlda.notificationservice.notification.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationReadRequest(
    @NotNull(message = "nguoiNhanId is required")
    Long nguoiNhanId
) {
}

