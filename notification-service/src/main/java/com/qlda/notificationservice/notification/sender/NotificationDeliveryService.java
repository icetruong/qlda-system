package com.qlda.notificationservice.notification.sender;

import com.qlda.notificationservice.notification.dto.NotificationResponse;
import com.qlda.notificationservice.notification.dto.kafka.NotificationEvent;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);

    private final Map<String, NotificationSender> senderByChannel;

    public NotificationDeliveryService(
        SystemNotificationSender systemNotificationSender,
        EmailNotificationSender emailNotificationSender,
        TeamsNotificationSender teamsNotificationSender
    ) {
        this.senderByChannel = java.util.List.of(systemNotificationSender, emailNotificationSender, teamsNotificationSender)
            .stream()
            .collect(Collectors.toMap(sender -> sender.channel().toUpperCase(Locale.ROOT), Function.identity()));
    }

    public void deliver(NotificationEvent event, NotificationResponse notification) {
        if (event.kenhGui() == null || event.kenhGui().isEmpty()) {
            sendToChannel("SYSTEM", event, notification);
            return;
        }
        for (String channel : event.kenhGui()) {
            sendToChannel(channel, event, notification);
        }
    }

    private void sendToChannel(String channel, NotificationEvent event, NotificationResponse notification) {
        if (channel == null) {
            return;
        }
        NotificationSender sender = senderByChannel.get(channel.toUpperCase(Locale.ROOT));
        if (sender == null) {
            log.warn("Unknown notification channel={} for eventId={}", channel, event.eventId());
            return;
        }
        try {
            sender.send(event, notification);
        } catch (RuntimeException ex) {
            log.error("Send notification failed channel={} eventId={} notificationId={}",
                channel, event.eventId(), notification.id(), ex);
        }
    }
}
