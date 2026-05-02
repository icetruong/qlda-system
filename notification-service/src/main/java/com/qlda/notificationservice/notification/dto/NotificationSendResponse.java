package com.qlda.notificationservice.notification.dto;

import java.util.List;

public record NotificationSendResponse(
    Long notificationId,
    List<String> sentChannels
) {
}

