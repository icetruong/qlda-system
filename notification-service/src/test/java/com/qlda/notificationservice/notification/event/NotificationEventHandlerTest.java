package com.qlda.notificationservice.notification.event;

import com.qlda.notificationservice.common.exception.AppException;
import com.qlda.notificationservice.common.exception.ErrorCode;
import com.qlda.notificationservice.notification.dto.NotificationCreateRequest;
import com.qlda.notificationservice.notification.dto.NotificationResponse;
import com.qlda.notificationservice.notification.dto.kafka.NotificationEvent;
import com.qlda.notificationservice.notification.sender.EmailNotificationSender;
import com.qlda.notificationservice.notification.sender.NotificationDeliveryService;
import com.qlda.notificationservice.notification.sender.SystemNotificationSender;
import com.qlda.notificationservice.notification.sender.TeamsNotificationSender;
import com.qlda.notificationservice.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private EventDeduplicationService eventDeduplicationService;
    @Mock
    private SystemNotificationSender systemNotificationSender;
    @Mock
    private EmailNotificationSender emailNotificationSender;
    @Mock
    private TeamsNotificationSender teamsNotificationSender;

    private NotificationEventHandler notificationEventHandler;

    @BeforeEach
    void setUp() {
        when(systemNotificationSender.channel()).thenReturn("SYSTEM");
        when(emailNotificationSender.channel()).thenReturn("EMAIL");
        when(teamsNotificationSender.channel()).thenReturn("TEAMS");
        NotificationDeliveryService deliveryService = new NotificationDeliveryService(
            systemNotificationSender,
            emailNotificationSender,
            teamsNotificationSender
        );
        notificationEventHandler = new NotificationEventHandler(
            notificationService,
            eventDeduplicationService,
            deliveryService
        );
    }

    @Test
    void handleSingleReceiverEventSuccess() {
        NotificationEvent event = baseEvent(List.of(2L), List.of("SYSTEM"));
        when(eventDeduplicationService.isProcessed("evt-001")).thenReturn(false);
        when(notificationService.create(any(NotificationCreateRequest.class)))
            .thenReturn(createdNotification(1L, 2L));

        notificationEventHandler.handle(event);

        verify(notificationService, times(1)).create(any(NotificationCreateRequest.class));
        verify(systemNotificationSender, times(1)).send(eq(event), any(NotificationResponse.class));
        verify(eventDeduplicationService).markProcessed("evt-001");
    }

    @Test
    void handleMultipleReceiversEventSuccess() {
        NotificationEvent event = baseEvent(List.of(2L, 3L), List.of("SYSTEM"));
        when(eventDeduplicationService.isProcessed("evt-001")).thenReturn(false);
        when(notificationService.create(any(NotificationCreateRequest.class)))
            .thenReturn(createdNotification(1L, 2L), createdNotification(2L, 3L));

        notificationEventHandler.handle(event);

        verify(notificationService, times(2)).create(any(NotificationCreateRequest.class));
    }

    @Test
    void duplicateEventIdShouldNotCreateNotification() {
        NotificationEvent event = baseEvent(List.of(2L), List.of("SYSTEM"));
        when(eventDeduplicationService.isProcessed("evt-001")).thenReturn(true);

        notificationEventHandler.handle(event);

        verify(notificationService, never()).create(any(NotificationCreateRequest.class));
        verify(eventDeduplicationService, never()).markProcessed(any());
    }

    @Test
    void invalidEventMissingEventIdShouldReject() {
        NotificationEvent event = new NotificationEvent(
            null,
            "DOCUMENT_TRANSFERRED",
            "workflow-service",
            List.of(2L),
            "Thong bao xu ly van ban",
            "Noi dung",
            "NHAC_VIEC",
            List.of("SYSTEM"),
            "DOCUMENT",
            1L,
            null,
            LocalDateTime.now()
        );

        assertThatThrownBy(() -> notificationEventHandler.handle(event))
            .isInstanceOf(AppException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void invalidEventMissingNguoiNhanIdsShouldReject() {
        NotificationEvent event = baseEvent(List.of(), List.of("SYSTEM"));

        assertThatThrownBy(() -> notificationEventHandler.handle(event))
            .isInstanceOf(AppException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void emailChannelShouldCallEmailSender() {
        NotificationEvent event = baseEvent(List.of(2L), List.of("EMAIL"));
        when(eventDeduplicationService.isProcessed("evt-001")).thenReturn(false);
        when(notificationService.create(any(NotificationCreateRequest.class))).thenReturn(createdNotification(1L, 2L));

        notificationEventHandler.handle(event);

        verify(emailNotificationSender, times(1)).send(eq(event), any(NotificationResponse.class));
    }

    @Test
    void teamsChannelShouldCallTeamsSender() {
        NotificationEvent event = baseEvent(List.of(2L), List.of("TEAMS"));
        when(eventDeduplicationService.isProcessed("evt-001")).thenReturn(false);
        when(notificationService.create(any(NotificationCreateRequest.class))).thenReturn(createdNotification(1L, 2L));

        notificationEventHandler.handle(event);

        verify(teamsNotificationSender, times(1)).send(eq(event), any(NotificationResponse.class));
    }

    @Test
    void senderErrorShouldNotLoseWholeEvent() {
        NotificationEvent event = baseEvent(List.of(2L), List.of("SYSTEM", "EMAIL", "TEAMS"));
        when(eventDeduplicationService.isProcessed("evt-001")).thenReturn(false);
        when(notificationService.create(any(NotificationCreateRequest.class))).thenReturn(createdNotification(1L, 2L));
        doThrow(new RuntimeException("mail not configured")).when(emailNotificationSender)
            .send(eq(event), any(NotificationResponse.class));

        notificationEventHandler.handle(event);

        verify(systemNotificationSender, times(1)).send(eq(event), any(NotificationResponse.class));
        verify(teamsNotificationSender, times(1)).send(eq(event), any(NotificationResponse.class));
        verify(eventDeduplicationService, times(1)).markProcessed("evt-001");
    }

    private NotificationEvent baseEvent(List<Long> nguoiNhanIds, List<String> kenhGui) {
        return new NotificationEvent(
            "evt-001",
            "DOCUMENT_TRANSFERRED",
            "workflow-service",
            nguoiNhanIds,
            "Thong bao xu ly van ban",
            "Ban co van ban moi can xu ly",
            "NHAC_VIEC",
            kenhGui,
            "DOCUMENT",
            1L,
            null,
            LocalDateTime.now()
        );
    }

    private NotificationResponse createdNotification(Long id, Long receiverId) {
        return new NotificationResponse(
            id,
            "Thong bao",
            "Noi dung",
            receiverId,
            1L,
            "NHAC_VIEC",
            "SYSTEM",
            false,
            LocalDateTime.now(),
            null
        );
    }
}
