package com.qlda.notificationservice.notification.sender;

import com.qlda.notificationservice.notification.dto.NotificationResponse;
import com.qlda.notificationservice.notification.dto.kafka.NotificationEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationSenderTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailNotificationSender emailNotificationSender;

    @Test
    void sendShouldUseOutlookMailSenderWithRecipientFromMetadata() {
        NotificationEvent event = new NotificationEvent(
            "evt-001",
            "DOCUMENT_TRANSFERRED",
            "workflow-service",
            List.of(2L),
            "Thong bao xu ly van ban",
            "Ban co van ban moi can xu ly",
            "NHAC_VIEC",
            List.of("EMAIL"),
            "DOCUMENT",
            1L,
            Map.of("recipientEmails", List.of("user1@outlook.com", "user2@outlook.com")),
            LocalDateTime.parse("2026-04-30T10:00:00")
        );
        NotificationResponse notification = new NotificationResponse(
            10L,
            "Thong bao xu ly van ban",
            "Ban co van ban moi can xu ly",
            2L,
            1L,
            "NHAC_VIEC",
            "EMAIL",
            false,
            LocalDateTime.now(),
            null
        );

        emailNotificationSender.send(event, notification);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getTo()).containsExactly("user1@outlook.com", "user2@outlook.com");
        assertThat(message.getSubject()).isEqualTo("Thong bao xu ly van ban");
        assertThat(message.getText()).contains("Ban co van ban moi can xu ly");
    }

    @Test
    void sendShouldFailWhenNoRecipientEmail() {
        NotificationEvent event = new NotificationEvent(
            "evt-001",
            "DOCUMENT_TRANSFERRED",
            "workflow-service",
            List.of(2L),
            "Thong bao xu ly van ban",
            "Ban co van ban moi can xu ly",
            "NHAC_VIEC",
            List.of("EMAIL"),
            "DOCUMENT",
            1L,
            Map.of(),
            LocalDateTime.parse("2026-04-30T10:00:00")
        );
        NotificationResponse notification = new NotificationResponse(
            10L,
            "Thong bao xu ly van ban",
            "Ban co van ban moi can xu ly",
            2L,
            1L,
            "NHAC_VIEC",
            "EMAIL",
            false,
            LocalDateTime.now(),
            null
        );

        assertThatThrownBy(() -> emailNotificationSender.send(event, notification))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("recipient email");
    }
}
