package com.qlda.documentservice.notification;

import com.qlda.documentservice.notification.dto.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpNotificationEventPublisher implements NotificationEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(NoOpNotificationEventPublisher.class);

    @Override
    public void publish(NotificationEvent event) {
        log.debug("Skip notification publish because no concrete publisher is configured: {}", event.eventType());
    }
}
