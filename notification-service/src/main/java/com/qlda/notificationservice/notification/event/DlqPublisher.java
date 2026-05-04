package com.qlda.notificationservice.notification.event;

public interface DlqPublisher {

    void publish(String payload, String reason);
}
