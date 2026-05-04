package com.qlda.workflowservice.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
