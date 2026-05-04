package com.qlda.notificationservice.notification.event;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class InMemoryEventDeduplicationService implements EventDeduplicationService {

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    @Override
    public boolean isProcessed(String eventId) {
        return eventId != null && processedEventIds.contains(eventId);
    }

    @Override
    public void markProcessed(String eventId) {
        if (eventId != null) {
            processedEventIds.add(eventId);
        }
        // TODO persist eventId in processed_event table for durable deduplication.
    }
}
