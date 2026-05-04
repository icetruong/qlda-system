package com.qlda.notificationservice.notification.dto.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationEvent(
    String eventId,
    String eventType,
    String sourceService,
    List<Long> nguoiNhanIds,
    String tieuDe,
    String noiDung,
    String loaiThongBao,
    List<String> kenhGui,
    String referenceType,
    Long referenceId,
    Map<String, Object> metadata,
    LocalDateTime createdAt
) {
}
