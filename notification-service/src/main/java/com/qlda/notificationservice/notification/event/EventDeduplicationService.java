package com.qlda.notificationservice.notification.event;

public interface EventDeduplicationService {

    boolean isProcessed(String eventId);

    void markProcessed(String eventId);
}
