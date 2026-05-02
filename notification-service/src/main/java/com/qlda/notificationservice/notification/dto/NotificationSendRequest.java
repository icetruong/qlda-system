package com.qlda.notificationservice.notification.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record NotificationSendRequest(
    @NotEmpty(message = "kenhGui is required")
    List<String> kenhGui
) {
}

