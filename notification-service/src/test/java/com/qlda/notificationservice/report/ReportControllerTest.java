package com.qlda.notificationservice.report;

import com.qlda.notificationservice.common.api.PageResponse;
import com.qlda.notificationservice.common.exception.GlobalExceptionHandler;
import com.qlda.notificationservice.report.controller.ReportController;
import com.qlda.notificationservice.report.dto.DashboardResponse;
import com.qlda.notificationservice.report.dto.DocumentStatisticsResponse;
import com.qlda.notificationservice.report.dto.ExportReportResponse;
import com.qlda.notificationservice.report.dto.OverdueDocumentItem;
import com.qlda.notificationservice.report.dto.StatisticItem;
import com.qlda.notificationservice.report.dto.WorkflowProgressItem;
import com.qlda.notificationservice.report.dto.WorkflowProgressResponse;
import com.qlda.notificationservice.report.service.ReportService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReportController controller = new ReportController(reportService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new AuthorizationHeaderFilter())
            .build();
    }

    @Test
    void allEndpointsReturnSuccess() throws Exception {
        DashboardResponse dashboard = new DashboardResponse(120, 70, 50, 80, 30, 10, 66.67, 8.33);
        DocumentStatisticsResponse statistics = new DocumentStatisticsResponse(
            "status",
            List.of(new StatisticItem("Dang xu ly", 30), new StatisticItem("Da hoan thanh", 80))
        );
        WorkflowProgressResponse progress = new WorkflowProgressResponse(
            50, 35, 10, 5,
            List.of(new WorkflowProgressItem(1L, "123/CV-ABC", "Trich yeu", 2L, "Nguyen Van A", 1, 60, LocalDateTime.now().plusDays(3)))
        );
        PageResponse<OverdueDocumentItem> overdue = new PageResponse<>(
            List.of(new OverdueDocumentItem(1L, "123/CV-ABC", "Trich yeu", 2L, "Nguyen Van A", LocalDateTime.now().minusDays(5), 5, 1)),
            0, 10, 1, 1
        );
        ExportReportResponse export = new ExportReportResponse("bao-cao-dashboard-20260430.xlsx", "/exports/bao-cao-dashboard-20260430.xlsx");

        when(reportService.getDashboard(any(), any(), any())).thenReturn(dashboard);
        when(reportService.getDocumentStatistics(any(), any(), any(), any())).thenReturn(statistics);
        when(reportService.getWorkflowProgress(any(), any(), any(), any())).thenReturn(progress);
        when(reportService.getOverdueDocuments(any(), any(), anyInt(), anyInt())).thenReturn(overdue);
        when(reportService.export(any(), any(), any(), any(), any())).thenReturn(export);

        mockMvc.perform(get("/api/reports/dashboard").header("Authorization", "Bearer token"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/reports/documents/statistics").header("Authorization", "Bearer token"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/reports/workflows/progress").header("Authorization", "Bearer token"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/reports/overdue-documents").header("Authorization", "Bearer token"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/reports/export").header("Authorization", "Bearer token"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void noToken401() throws Exception {
        mockMvc.perform(get("/api/reports/dashboard"))
            .andExpect(status().isUnauthorized());
    }

    private static class AuthorizationHeaderFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            if (request.getHeader("Authorization") == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            filterChain.doFilter(request, response);
        }
    }
}
