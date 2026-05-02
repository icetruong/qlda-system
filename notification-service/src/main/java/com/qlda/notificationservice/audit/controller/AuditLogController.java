package com.qlda.notificationservice.audit.controller;

import com.qlda.notificationservice.audit.dto.AuditLogCreateRequest;
import com.qlda.notificationservice.audit.service.AuditLogService;
import com.qlda.notificationservice.common.api.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody AuditLogCreateRequest request) {
        return ApiResponse.success("Create audit log successfully", auditLogService.create(request));
    }

    @GetMapping
    public ApiResponse<?> getLogs(
        @RequestParam(required = false) Long nguoiDungId,
        @RequestParam(required = false) String doiTuong,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success("Get audit logs successfully", auditLogService.getLogs(nguoiDungId, doiTuong, fromDate, toDate, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getDetail(@PathVariable Long id) {
        return ApiResponse.success("Get audit log detail successfully", auditLogService.getDetail(id));
    }
}

