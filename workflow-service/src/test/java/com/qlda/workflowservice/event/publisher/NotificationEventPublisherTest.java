package com.qlda.workflowservice.event.publisher;

import com.qlda.workflowservice.event.NotificationEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationEventPublisherTest {

    @Test
    void transferPublish_shouldNotThrow() {
        NotificationEventPublisher publisher = new NoOpNotificationEventPublisher();
        NotificationEvent event = new NotificationEvent(
                "evt-1",
                "WORKFLOW_TRANSFERRED",
                "workflow-service",
                List.of(2L),
                "Transfer",
                "Document transferred",
                "NHAC_VIEC",
                List.of("SYSTEM"),
                "WORKFLOW",
                20L,
                Map.of("documentId", 1L),
                LocalDateTime.now()
        );

        assertDoesNotThrow(() -> publisher.publish(event));
    }

    @Test
    void submitApprovalPublish_shouldNotThrow() {
        NotificationEventPublisher publisher = new NoOpNotificationEventPublisher();
        NotificationEvent event = new NotificationEvent(
                "evt-2",
                "WORKFLOW_APPROVAL_REQUESTED",
                "workflow-service",
                List.of(4L),
                "Approval requested",
                "Need approval",
                "PHE_DUYET",
                List.of("SYSTEM"),
                "WORKFLOW",
                30L,
                Map.of("documentId", 1L),
                LocalDateTime.now()
        );

        assertDoesNotThrow(() -> publisher.publish(event));
    }

    @Test
    void slaViolationPublish_shouldNotThrow() {
        NotificationEventPublisher publisher = new NoOpNotificationEventPublisher();
        NotificationEvent event = new NotificationEvent(
                "evt-3",
                "WORKFLOW_SLA_VIOLATED",
                "workflow-service",
                List.of(2L, 4L),
                "SLA violated",
                "Document overdue",
                "CANH_BAO_SLA",
                List.of("SYSTEM"),
                "WORKFLOW",
                40L,
                Map.of("soGioTre", 5L),
                LocalDateTime.now()
        );

        assertDoesNotThrow(() -> publisher.publish(event));
    }
}
