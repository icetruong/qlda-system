package com.qlda.documentservice.notification;

import com.qlda.documentservice.notification.dto.NotificationEvent;

public interface NotificationEventPublisher {
    void publish(NotificationEvent event);
}
