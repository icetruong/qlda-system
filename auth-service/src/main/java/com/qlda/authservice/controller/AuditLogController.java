package com.qlda.authservice.controller;

import com.qlda.authservice.common.ApiResponse;
import com.qlda.authservice.dto.audit.AuditLogExportResponse;
import com.qlda.authservice.dto.audit.AuditLogResponse;
import com.qlda.authservice.dto.common.PageData;
import com.qlda.authservice.service.AuditLogService;
import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<PageData<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String hanhDong,
            @RequestParam(required = false) String doiTuong,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ApiResponse.success(
                "Get audit logs successfully",
                auditLogService.getAuditLogs(
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")),
                        userId,
                        hanhDong,
                        doiTuong,
                        fromDate,
                        toDate
                )
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<AuditLogResponse> getAuditLogDetail(@PathVariable Long id) {
        return ApiResponse.success("Get audit log detail successfully", auditLogService.getAuditLogById(id));
    }

    @GetMapping("/export")
    public ApiResponse<AuditLogExportResponse> exportAuditLogs(
            @RequestParam(defaultValue = "excel") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ApiResponse.success("Export audit logs successfully", auditLogService.exportLogs(format, fromDate, toDate));
    }
}
