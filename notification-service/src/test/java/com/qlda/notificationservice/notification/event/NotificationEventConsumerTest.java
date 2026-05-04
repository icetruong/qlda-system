package com.qlda.notificationservice.notification.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.notificationservice.notification.dto.kafka.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationEventHandler notificationEventHandler;
    @Mock
    private DlqPublisher dlqPublisher;

    private NotificationEventConsumer notificationEventConsumer;

    @BeforeEach
    void setUp() {
        notificationEventConsumer = new NotificationEventConsumer(
            notificationEventHandler,
            dlqPublisher,
            new ObjectMapper().findAndRegisterModules()
        );
    }

    @Test
    void consumeValidEventShouldCallHandler() {
        String payload = """
            {
              "eventId":"evt-001",
              "eventType":"DOCUMENT_TRANSFERRED",
              "sourceService":"workflow-service",
              "nguoiNhanIds":[2],
              "tieuDe":"Thong bao xu ly van ban",
              "noiDung":"Ban co van ban moi can xu ly",
              "loaiThongBao":"NHAC_VIEC",
              "kenhGui":["SYSTEM"],
              "referenceType":"DOCUMENT",
              "referenceId":1,
              "createdAt":"2026-04-30T10:00:00"
            }
            """;

        notificationEventConsumer.consume(payload);

        verify(notificationEventHandler).handle(any(NotificationEvent.class));
    }

    @Test
    void handlerErrorShouldRouteDlq() {
        String payload = """
            {
              "eventId":"evt-001",
              "eventType":"DOCUMENT_TRANSFERRED",
              "sourceService":"workflow-service",
              "nguoiNhanIds":[2],
              "tieuDe":"Thong bao xu ly van ban",
              "noiDung":"Ban co van ban moi can xu ly",
              "loaiThongBao":"NHAC_VIEC",
              "kenhGui":["SYSTEM"],
              "referenceType":"DOCUMENT",
              "referenceId":1,
              "createdAt":"2026-04-30T10:00:00"
            }
            """;
        doThrow(new RuntimeException("handler error")).when(notificationEventHandler).handle(any(NotificationEvent.class));

        notificationEventConsumer.consume(payload);

        verify(dlqPublisher).publish(eq(payload), eq("handler error"));
    }

    @Test
    void parseJsonErrorShouldHandleSafely() {
        assertThatCode(() -> notificationEventConsumer.consume("invalid-json")).doesNotThrowAnyException();
        verify(dlqPublisher).publish(eq("invalid-json"), any(String.class));
    }
}
