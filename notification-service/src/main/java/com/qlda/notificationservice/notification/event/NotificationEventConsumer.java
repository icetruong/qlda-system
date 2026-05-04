package com.qlda.notificationservice.notification.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.notificationservice.notification.dto.kafka.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationEventHandler notificationEventHandler;
    private final DlqPublisher dlqPublisher;
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(
        NotificationEventHandler notificationEventHandler,
        DlqPublisher dlqPublisher,
        ObjectMapper objectMapper
    ) {
        this.notificationEventHandler = notificationEventHandler;
        this.dlqPublisher = dlqPublisher;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "${app.kafka.notification-topic:notification-events}",
        groupId = "${spring.kafka.consumer.group-id:notification-service}"
    )
    public void consume(String payload) {
        try {
            NotificationEvent event = objectMapper.readValue(payload, NotificationEvent.class);
            notificationEventHandler.handle(event);
        } catch (Exception ex) {
            log.error("Consume notification event failed", ex);
            dlqPublisher.publish(payload, ex.getMessage());
        }
    }
}
