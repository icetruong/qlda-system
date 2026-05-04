package com.qlda.documentservice.client;

import com.qlda.documentservice.client.dto.NotificationClientDtos;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service-client", url = "${services.notification-service.base-url:http://localhost:8085}")
public interface NotificationServiceClient {

    @PostMapping("/internal/notifications/send")
    NotificationClientDtos.SendNotificationResponse send(@RequestBody NotificationClientDtos.SendNotificationRequest request);

    @PostMapping("/internal/notifications/bulk-send")
    NotificationClientDtos.BulkSendNotificationResponse bulkSend(
        @RequestBody NotificationClientDtos.BulkSendNotificationRequest request
    );
}
