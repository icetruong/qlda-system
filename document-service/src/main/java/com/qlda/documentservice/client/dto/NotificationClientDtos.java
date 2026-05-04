package com.qlda.documentservice.client.dto;

import java.util.List;

public final class NotificationClientDtos {
    private NotificationClientDtos() {
    }

    public record SendNotificationRequest(Long nguoiNhanId, String tieuDe, String noiDung, String kenhGui) {
    }

    public record SendNotificationResponse(Long notificationId, Long nguoiNhanId, List<String> sentChannels) {
    }

    public record BulkSendNotificationRequest(List<Long> nguoiNhanIds, String tieuDe, String noiDung, String kenhGui) {
    }

    public record BulkSendNotificationResponse(Integer totalReceivers, Integer totalSent, List<String> sentChannels) {
    }
}
