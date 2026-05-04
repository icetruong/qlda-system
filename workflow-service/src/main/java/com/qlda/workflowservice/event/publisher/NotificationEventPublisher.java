package com.qlda.workflowservice.event.publisher;

import com.qlda.workflowservice.event.NotificationEvent;

public interface NotificationEventPublisher {
    void publish(NotificationEvent event);
}
