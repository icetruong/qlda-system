package com.qlda.notificationservice.notification.sender;

import com.qlda.notificationservice.notification.dto.NotificationResponse;
import com.qlda.notificationservice.notification.dto.kafka.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TeamsNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(TeamsNotificationSender.class);

    @Override
    public String channel() {
        return "TEAMS";
    }

    @Override
    public void send(NotificationEvent event, NotificationResponse notification) {
        log.info("TODO integrate Microsoft Teams webhook for eventId={}, notificationId={}", event.eventId(), notification.id());
    }
}
