package com.qlda.documentservice.notification;

import com.qlda.documentservice.notification.dto.NotificationEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(KafkaTemplate.class)
public class KafkaNotificationEventPublisher implements NotificationEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final String notificationTopic;

    public KafkaNotificationEventPublisher(
        KafkaTemplate<Object, Object> kafkaTemplate,
        @Value("${services.notification-service.topic:notification-events}") String notificationTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationTopic = notificationTopic;
    }

    @Override
    public void publish(NotificationEvent event) {
        String key = event.referenceId() == null ? event.eventId() : String.valueOf(event.referenceId());
        kafkaTemplate.send(notificationTopic, key, event);
    }
}
