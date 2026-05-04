package com.qlda.notificationservice.report;

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
import com.qlda.notificationservice.report.service.ReportService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private DocumentServiceClient documentServiceClient;

    @Mock
    private WorkflowServiceClient workflowServiceClient;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private ReportService reportService;

    @Test
    void dashboardSuccess() {
        when(documentServiceClient.getDocumentStatistics("2026-04-01", "2026-04-30", 1L, "status"))
            .thenReturn(new DocumentStatisticsClientResponse(
                120, 70, 50, List.of()
            ));
        when(workflowServiceClient.getWorkflowStatistics("2026-04-01", "2026-04-30", 1L))
            .thenReturn(new WorkflowStatisticsClientResponse(50, 35, 10, 5));

        var response = reportService.getDashboard("2026-04-01", "2026-04-30", 1L);

        assertThat(response.totalDocuments()).isEqualTo(120);
        assertThat(response.completionRate()).isEqualTo(70.0);
        assertThat(response.overdueRate()).isEqualTo(10.0);
    }

    @Test
    void documentStatisticsSuccess() {
        when(documentServiceClient.getDocumentStatistics(any(), any(), any(), eq("status")))
            .thenReturn(new DocumentStatisticsClientResponse(120, 70, 50, List.of()));

        var response = reportService.getDocumentStatistics("2026-04-01", "2026-04-30", 1L, "status");

        assertThat(response.groupBy()).isEqualTo("status");
    }

    @Test
    void invalidGroupByThrowsError() {
        assertThatThrownBy(() -> reportService.getDocumentStatistics("2026-04-01", "2026-04-30", 1L, "invalid"))
            .isInstanceOf(AppException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void workflowProgressSuccessAndEnrichUserName() {
        when(workflowServiceClient.getWorkflowProgress(any(), any(), any(), any()))
            .thenReturn(
                new WorkflowProgressClientResponse(
                    50,
                    35,
                    10,
                    List.of(
                        new WorkflowProgressClientItem(
                            1L, "123/CV-ABC", "Trich yeu", 2L, 1, 60, LocalDateTime.parse("2026-05-02T17:00:00")
                        )
                    )
                )
            );
        when(authServiceClient.getUserById(2L))
            .thenReturn(new AuthUserResponse(2L, "nva", "Nguyen Van A", "nva@company.com", 1, "Phong Hanh chinh"));

        var response = reportService.getWorkflowProgress("2026-04-01", "2026-04-30", 1L, 2L);

        assertThat(response.items()).isNotEmpty();
        assertThat(response.items().getFirst().nguoiXuLy()).isEqualTo("Nguyen Van A");
    }

    @Test
    void overdueDocumentsSuccess() {
        when(documentServiceClient.getOverdueDocuments(any(), any(), anyInt(), anyInt()))
            .thenReturn(
                new DocumentOverduePageResponse(
                    List.of(
                        new DocumentOverduePageResponse.OverdueDocumentClientItem(
                            1L, "123/CV-ABC", "Trich yeu", 2L, LocalDateTime.now().minusDays(2), 2, 1
                        )
                    ),
                    0,
                    10,
                    1L
                )
            );
        when(authServiceClient.getUserById(2L))
            .thenReturn(new AuthUserResponse(2L, "nva", "Nguyen Van A", "nva@company.com", 1, "Phong Hanh chinh"));
        PageResponse<?> response = reportService.getOverdueDocuments(1L, 2L, 0, 10);
        assertThat(response.content()).isNotEmpty();
    }

    @Test
    void exportSuccessSkeleton() {
        var response = reportService.export("dashboard", "excel", "2026-04-01", "2026-04-30", 1L);

        assertThat(response.fileName()).contains("bao-cao");
        assertThat(response.fileUrl()).startsWith("/exports/");
    }

    @Test
    void serviceClientErrorShouldThrowAppException() {
        when(documentServiceClient.getDocumentStatistics(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("downstream error"));

        assertThatThrownBy(() -> reportService.getDashboard("2026-04-01", "2026-04-30", 1L))
            .isInstanceOf(AppException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
