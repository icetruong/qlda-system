package com.qlda.documentservice.notification;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.qlda.documentservice.notification.dto.NotificationEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class KafkaNotificationEventPublisherTest {

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Test
    void publish_shouldSendEventToConfiguredTopic() {
        KafkaNotificationEventPublisher publisher = new KafkaNotificationEventPublisher(kafkaTemplate, "notification-events");
        NotificationEvent event = new NotificationEvent(
            "evt-001",
            "DOCUMENT_CREATED",
            "document-service",
            List.of(2L),
            "Thong bao",
            "Noi dung",
            "VAN_BAN",
            List.of("SYSTEM", "EMAIL"),
            "DOCUMENT",
            1L,
            Map.of("documentId", 1),
            LocalDateTime.of(2026, 4, 30, 10, 0)
        );

        publisher.publish(event);

        verify(kafkaTemplate).send(eq("notification-events"), eq("1"), eq(event));
    }
}
