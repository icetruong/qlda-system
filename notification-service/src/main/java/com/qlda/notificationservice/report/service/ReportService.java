package com.qlda.notificationservice.report.service;

import com.qlda.notificationservice.client.AuthServiceClient;
import com.qlda.notificationservice.client.DocumentServiceClient;
import com.qlda.notificationservice.client.WorkflowServiceClient;
import com.qlda.notificationservice.client.dto.AuthUserResponse;
import com.qlda.notificationservice.client.dto.DocumentOverduePageResponse;
import com.qlda.notificationservice.client.dto.DocumentStatisticsClientResponse;
import com.qlda.notificationservice.client.dto.WorkflowProgressClientItem;
import com.qlda.notificationservice.client.dto.WorkflowProgressClientResponse;
import com.qlda.notificationservice.client.dto.WorkflowStatisticsClientResponse;
import com.qlda.notificationservice.common.api.PageResponse;
import com.qlda.notificationservice.common.exception.AppException;
import com.qlda.notificationservice.common.exception.ErrorCode;
import com.qlda.notificationservice.report.dto.DashboardResponse;
import com.qlda.notificationservice.report.dto.DocumentStatisticsResponse;
import com.qlda.notificationservice.report.dto.ExportReportResponse;
import com.qlda.notificationservice.report.dto.OverdueDocumentItem;
import com.qlda.notificationservice.report.dto.StatisticItem;
import com.qlda.notificationservice.report.dto.WorkflowProgressItem;
import com.qlda.notificationservice.report.dto.WorkflowProgressResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private static final Set<String> ALLOWED_GROUP_BY = Set.of("status", "type", "unit", "month");
    private static final Set<String> ALLOWED_REPORT_TYPES = Set.of(
        "dashboard",
        "document_statistics",
        "workflow_progress",
        "overdue_documents"
    );
    private static final Set<String> ALLOWED_EXPORT_FORMATS = Set.of("excel", "pdf");

    private final DocumentServiceClient documentServiceClient;
    private final WorkflowServiceClient workflowServiceClient;
    private final AuthServiceClient authServiceClient;

    public ReportService(
        DocumentServiceClient documentServiceClient,
        WorkflowServiceClient workflowServiceClient,
        AuthServiceClient authServiceClient
    ) {
        this.documentServiceClient = documentServiceClient;
        this.workflowServiceClient = workflowServiceClient;
        this.authServiceClient = authServiceClient;
    }

    public DashboardResponse getDashboard(String fromDate, String toDate, Long donViId) {
        try {
            DocumentStatisticsClientResponse documentStatistics = documentServiceClient
                .getDocumentStatistics(fromDate, toDate, donViId, "status");
            WorkflowStatisticsClientResponse workflowStatistics = workflowServiceClient
                .getWorkflowStatistics(fromDate, toDate, donViId);

            int totalDocuments = safeInt(documentStatistics.totalDocuments());
            int incomingDocuments = safeInt(documentStatistics.incomingDocuments());
            int outgoingDocuments = safeInt(documentStatistics.outgoingDocuments());
            int completedDocuments = safeInt(workflowStatistics.completedTasks());
            int processingDocuments = safeInt(workflowStatistics.processingTasks());
            int overdueDocuments = safeInt(workflowStatistics.overdueTasks());
            int totalTasks = safeInt(workflowStatistics.totalTasks());
            double completionRate = calculateRate(completedDocuments, totalTasks);
            double overdueRate = calculateRate(overdueDocuments, totalTasks);

            return new DashboardResponse(
                totalDocuments,
                incomingDocuments,
                outgoingDocuments,
                completedDocuments,
                processingDocuments,
                overdueDocuments,
                completionRate,
                overdueRate
            );
        } catch (RuntimeException ex) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public DocumentStatisticsResponse getDocumentStatistics(String fromDate, String toDate, Long donViId, String groupBy) {
        String normalizedGroupBy = normalizeGroupBy(groupBy);
        try {
            DocumentStatisticsClientResponse clientResponse = documentServiceClient
                .getDocumentStatistics(fromDate, toDate, donViId, normalizedGroupBy);
            List<StatisticItem> items = clientResponse.items() == null ? List.of() : clientResponse.items().stream()
                .map(item -> new StatisticItem(item.label(), safeInt(item.value())))
                .toList();
            return new DocumentStatisticsResponse(normalizedGroupBy, items);
        } catch (RuntimeException ex) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public WorkflowProgressResponse getWorkflowProgress(String fromDate, String toDate, Long donViId, Long nguoiXuLyId) {
        try {
            WorkflowProgressClientResponse clientResponse = workflowServiceClient
                .getWorkflowProgress(fromDate, toDate, donViId, nguoiXuLyId);
            List<WorkflowProgressItem> mappedItems = clientResponse.items() == null ? List.of() : clientResponse.items().stream()
                .map(this::mapWorkflowItem)
                .toList();
            int total = safeInt(clientResponse.totalTasks());
            int completed = safeInt(clientResponse.completedTasks());
            int processing = safeInt(clientResponse.processingTasks());
            int overdue = Math.max(total - completed - processing, 0);
            return new WorkflowProgressResponse(total, completed, processing, overdue, mappedItems);
        } catch (RuntimeException ex) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public PageResponse<OverdueDocumentItem> getOverdueDocuments(Long donViId, Long nguoiXuLyId, int page, int size) {
        try {
            DocumentOverduePageResponse clientResponse = documentServiceClient
                .getOverdueDocuments(donViId, nguoiXuLyId, page, size);
            List<OverdueDocumentItem> items = clientResponse.content() == null ? List.of() : clientResponse.content().stream()
                .map(item -> new OverdueDocumentItem(
                    item.documentId(),
                    item.soKyHieu(),
                    item.trichYeu(),
                    item.nguoiXuLyId(),
                    resolveUserName(item.nguoiXuLyId()),
                    item.hanXuLy(),
                    item.soNgayTre(),
                    item.trangThai()
                ))
                .toList();
            int responsePage = clientResponse.page() == null ? page : clientResponse.page();
            int responseSize = clientResponse.size() == null ? size : clientResponse.size();
            long totalElements = clientResponse.totalElements() == null ? items.size() : clientResponse.totalElements();
            int totalPages = responseSize == 0 ? 1 : (int) Math.ceil((double) totalElements / responseSize);
            return new PageResponse<>(items, responsePage, responseSize, totalElements, totalPages);
        } catch (RuntimeException ex) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public ExportReportResponse export(String reportType, String format, String fromDate, String toDate, Long donViId) {
        String normalizedReportType = normalizeReportType(reportType);
        String normalizedFormat = normalizeFormat(format);
        String fileExt = "pdf".equals(normalizedFormat) ? "pdf" : "xlsx";
        String now = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String fileName = "bao-cao-" + normalizedReportType + "-" + now + "." + fileExt;
        // TODO integrate real Excel/PDF export.
        return new ExportReportResponse(fileName, "/exports/" + fileName);
    }

    private WorkflowProgressItem mapWorkflowItem(WorkflowProgressClientItem item) {
        return new WorkflowProgressItem(
            item.documentId(),
            item.soKyHieu(),
            item.trichYeu(),
            item.nguoiXuLyId(),
            resolveUserName(item.nguoiXuLyId()),
            item.trangThaiXuLy(),
            item.tyLeHoanThanh(),
            item.hanXuLy()
        );
    }

    private String resolveUserName(Long nguoiXuLyId) {
        if (nguoiXuLyId == null) {
            return null;
        }
        try {
            AuthUserResponse user = authServiceClient.getUserById(nguoiXuLyId);
            if (user != null && user.hoTen() != null) {
                return user.hoTen();
            }
        } catch (RuntimeException ignored) {
            // TODO log auth-service enrich failure in centralized logging.
        }
        return String.valueOf(nguoiXuLyId);
    }

    private String normalizeGroupBy(String groupBy) {
        String value = groupBy == null ? "status" : groupBy.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_GROUP_BY.contains(value)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return value;
    }

    private String normalizeReportType(String reportType) {
        String value = reportType == null ? "dashboard" : reportType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_REPORT_TYPES.contains(value)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return value;
    }

    private String normalizeFormat(String format) {
        String value = format == null ? "excel" : format.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXPORT_FORMATS.contains(value)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        return value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double calculateRate(int value, int total) {
        if (total <= 0) {
            return 0;
        }
        double rate = (double) value * 100 / total;
        return Math.round(rate * 100.0) / 100.0;
    }
}
