package com.qlda.notificationservice.report;

import com.qlda.notificationservice.common.api.PageResponse;
import com.qlda.notificationservice.report.service.ReportService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportServiceTest {

    private final ReportService reportService = new ReportService();

    @Test
    void dashboardReturnsData() {
        var response = reportService.getDashboard("2026-04-01", "2026-04-30", 1L);
        assertThat(response.totalDocuments()).isGreaterThan(0);
    }

    @Test
    void statisticsReturnsList() {
        var response = reportService.getDocumentStatistics("2026-04-01", "2026-04-30", 1L, "status");
        assertThat(response.items()).isNotEmpty();
    }

    @Test
    void progressReturnsItems() {
        var response = reportService.getWorkflowProgress("2026-04-01", "2026-04-30", 1L, 2L);
        assertThat(response.items()).isNotEmpty();
    }

    @Test
    void overdueReturnsPage() {
        PageResponse<?> response = reportService.getOverdueDocuments(1L, 2L, 0, 10);
        assertThat(response.content()).isNotEmpty();
    }

    @Test
    void exportReturnsFileInfo() {
        var response = reportService.export("dashboard", "excel", "2026-04-01", "2026-04-30", 1L);
        assertThat(response.fileName()).contains("bao-cao");
        assertThat(response.fileUrl()).startsWith("/exports/");
    }
}

