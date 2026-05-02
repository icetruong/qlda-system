package com.qlda.notificationservice.report.controller;

import com.qlda.notificationservice.common.api.ApiResponse;
import com.qlda.notificationservice.report.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard(
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @RequestParam(required = false) Long donViId
    ) {
        return ApiResponse.success("Get dashboard successfully", reportService.getDashboard(fromDate, toDate, donViId));
    }

    @GetMapping("/documents/statistics")
    public ApiResponse<?> statistics(
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @RequestParam(required = false) Long donViId,
        @RequestParam(required = false) String groupBy
    ) {
        return ApiResponse.success("Get document statistics successfully", reportService.getDocumentStatistics(fromDate, toDate, donViId, groupBy));
    }

    @GetMapping("/workflows/progress")
    public ApiResponse<?> progress(
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @RequestParam(required = false) Long donViId,
        @RequestParam(required = false) Long nguoiXuLyId
    ) {
        return ApiResponse.success("Get workflow progress successfully", reportService.getWorkflowProgress(fromDate, toDate, donViId, nguoiXuLyId));
    }

    @GetMapping("/overdue-documents")
    public ApiResponse<?> overdue(
        @RequestParam(required = false) Long donViId,
        @RequestParam(required = false) Long nguoiXuLyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success("Get overdue documents successfully", reportService.getOverdueDocuments(donViId, nguoiXuLyId, page, size));
    }

    @GetMapping("/export")
    public ApiResponse<?> export(
        @RequestParam(required = false) String reportType,
        @RequestParam(required = false) String format,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @RequestParam(required = false) Long donViId
    ) {
        return ApiResponse.success("Export report successfully", reportService.export(reportType, format, fromDate, toDate, donViId));
    }
}

