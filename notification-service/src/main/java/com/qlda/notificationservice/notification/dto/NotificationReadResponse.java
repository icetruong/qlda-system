package com.qlda.notificationservice.notification.dto;

import java.time.LocalDateTime;

public record NotificationReadResponse(
    Long notificationId,
    Boolean daDoc,
    LocalDateTime ngayDoc
) {
}

