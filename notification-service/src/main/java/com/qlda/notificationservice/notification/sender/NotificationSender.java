package com.qlda.notificationservice.notification.sender;

import com.qlda.notificationservice.notification.dto.NotificationResponse;
import com.qlda.notificationservice.notification.dto.kafka.NotificationEvent;

public interface NotificationSender {

    String channel();

    void send(NotificationEvent event, NotificationResponse notification);
}
