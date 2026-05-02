package com.qlda.notificationservice.notification.controller;

import com.qlda.notificationservice.common.api.ApiResponse;
import com.qlda.notificationservice.notification.dto.NotificationCreateRequest;
import com.qlda.notificationservice.notification.dto.NotificationReadRequest;
import com.qlda.notificationservice.notification.dto.NotificationSendRequest;
import com.qlda.notificationservice.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody NotificationCreateRequest request) {
        return ApiResponse.success("Create notification successfully", notificationService.create(request));
    }

    @GetMapping
    public ApiResponse<?> getNotifications(
        @RequestParam Long nguoiNhanId,
        @RequestParam(required = false) Boolean daDoc,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success("Get notifications successfully", notificationService.getNotifications(nguoiNhanId, daDoc, page, size));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<?> markAsRead(@PathVariable Long id, @Valid @RequestBody NotificationReadRequest request) {
        return ApiResponse.success("Mark notification as read successfully", notificationService.markAsRead(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ApiResponse.success("Delete notification successfully", Map.of("notificationId", id));
    }

    @PostMapping("/{id}/send")
    public ApiResponse<?> send(@PathVariable Long id, @Valid @RequestBody NotificationSendRequest request) {
        return ApiResponse.success("Send notification successfully", notificationService.send(id, request));
    }
}

