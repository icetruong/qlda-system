package com.qlda.notificationservice.report.service;

import com.qlda.notificationservice.common.api.PageResponse;
import com.qlda.notificationservice.report.dto.DashboardResponse;
import com.qlda.notificationservice.report.dto.DocumentStatisticsResponse;
import com.qlda.notificationservice.report.dto.ExportReportResponse;
import com.qlda.notificationservice.report.dto.OverdueDocumentItem;
import com.qlda.notificationservice.report.dto.StatisticItem;
import com.qlda.notificationservice.report.dto.WorkflowProgressItem;
import com.qlda.notificationservice.report.dto.WorkflowProgressResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    public DashboardResponse getDashboard(String fromDate, String toDate, Long donViId) {
        // TODO: call document-service
        // TODO: call workflow-service
        return new DashboardResponse(120, 70, 50, 80, 30, 10, 66.67, 8.33);
    }

    public DocumentStatisticsResponse getDocumentStatistics(String fromDate, String toDate, Long donViId, String groupBy) {
        // TODO: call document-service
        return new DocumentStatisticsResponse(
            groupBy == null ? "status" : groupBy,
            List.of(
                new StatisticItem("Dang xu ly", 30),
                new StatisticItem("Da hoan thanh", 80),
                new StatisticItem("Tre han", 10)
            )
        );
    }

    public WorkflowProgressResponse getWorkflowProgress(String fromDate, String toDate, Long donViId, Long nguoiXuLyId) {
        // TODO: call workflow-service
        return new WorkflowProgressResponse(
            50,
            35,
            10,
            5,
            List.of(
                new WorkflowProgressItem(
                    1L,
                    "123/CV-ABC",
                    "Van ban trien khai he thong",
                    nguoiXuLyId == null ? 2L : nguoiXuLyId,
                    "Nguyen Van A",
                    1,
                    60,
                    LocalDateTime.now().plusDays(3)
                )
            )
        );
    }

    public PageResponse<OverdueDocumentItem> getOverdueDocuments(Long donViId, Long nguoiXuLyId, int page, int size) {
        // TODO: call workflow-service
        List<OverdueDocumentItem> items = List.of(
            new OverdueDocumentItem(
                1L,
                "123/CV-ABC",
                "Van ban trien khai he thong",
                nguoiXuLyId == null ? 2L : nguoiXuLyId,
                "Nguyen Van A",
                LocalDateTime.now().minusDays(5),
                5,
                1
            )
        );
        return new PageResponse<>(items, page, size, items.size(), 1);
    }

    public ExportReportResponse export(String reportType, String format, String fromDate, String toDate, Long donViId) {
        // TODO: call document-service
        // TODO: call workflow-service
        // TODO: export Excel/PDF
        String fileExt = "pdf".equalsIgnoreCase(format) ? "pdf" : "xlsx";
        String type = reportType == null ? "dashboard" : reportType;
        String fileName = "bao-cao-" + type + "-20260430." + fileExt;
        return new ExportReportResponse(fileName, "/exports/" + fileName);
    }
}

