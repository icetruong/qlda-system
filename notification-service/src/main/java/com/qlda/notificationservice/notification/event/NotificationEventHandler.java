package com.qlda.notificationservice.notification.event;

import com.qlda.notificationservice.common.exception.AppException;
import com.qlda.notificationservice.common.exception.ErrorCode;
import com.qlda.notificationservice.notification.dto.NotificationCreateRequest;
import com.qlda.notificationservice.notification.dto.NotificationResponse;
import com.qlda.notificationservice.notification.dto.kafka.NotificationEvent;
import com.qlda.notificationservice.notification.sender.NotificationDeliveryService;
import com.qlda.notificationservice.notification.service.NotificationService;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NotificationEventHandler {

    private final NotificationService notificationService;
    private final EventDeduplicationService eventDeduplicationService;
    private final NotificationDeliveryService notificationDeliveryService;

    public NotificationEventHandler(
        NotificationService notificationService,
        EventDeduplicationService eventDeduplicationService,
        NotificationDeliveryService notificationDeliveryService
    ) {
        this.notificationService = notificationService;
        this.eventDeduplicationService = eventDeduplicationService;
        this.notificationDeliveryService = notificationDeliveryService;
    }

    public void handle(NotificationEvent event) {
        validate(event);
        if (eventDeduplicationService.isProcessed(event.eventId())) {
            return;
        }
        for (Long receiverId : event.nguoiNhanIds()) {
            NotificationCreateRequest request = new NotificationCreateRequest(
                event.tieuDe(),
                event.noiDung(),
                receiverId,
                event.referenceId(),
                event.loaiThongBao(),
                flattenChannels(event)
            );
            NotificationResponse notification = notificationService.create(request);
            notificationDeliveryService.deliver(event, notification);
        }
        eventDeduplicationService.markProcessed(event.eventId());
    }

    private void validate(NotificationEvent event) {
        if (event == null
            || !StringUtils.hasText(event.eventId())
            || !StringUtils.hasText(event.eventType())
            || !StringUtils.hasText(event.tieuDe())
            || !StringUtils.hasText(event.noiDung())
            || event.nguoiNhanIds() == null
            || event.nguoiNhanIds().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String flattenChannels(NotificationEvent event) {
        if (event.kenhGui() == null || event.kenhGui().isEmpty()) {
            return "SYSTEM";
        }
        StringJoiner joiner = new StringJoiner(",");
        event.kenhGui().forEach(joiner::add);
        return joiner.toString();
    }
}
