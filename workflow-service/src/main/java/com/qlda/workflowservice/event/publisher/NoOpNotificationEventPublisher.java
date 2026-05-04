package com.qlda.workflowservice.event.publisher;

import com.qlda.workflowservice.event.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoOpNotificationEventPublisher implements NotificationEventPublisher {
    @Override
    public void publish(NotificationEvent event) {
        // TODO: Replace with real Kafka publisher when broker integration is enabled.
        log.info("Notification event published (skeleton): type={}, referenceId={}", event.eventType(), event.referenceId());
    }
}
