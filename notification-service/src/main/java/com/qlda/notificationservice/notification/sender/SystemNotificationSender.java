package com.qlda.notificationservice.notification.sender;

import com.qlda.notificationservice.notification.dto.NotificationResponse;
import com.qlda.notificationservice.notification.dto.kafka.NotificationEvent;
import org.springframework.stereotype.Component;

@Component
public class SystemNotificationSender implements NotificationSender {

    @Override
    public String channel() {
        return "SYSTEM";
    }

    @Override
    public void send(NotificationEvent event, NotificationResponse notification) {
        // SYSTEM channel is successful when notification is persisted in DB.
    }
}
