package com.qlda.workflowservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.workflowservice.dto.internal.request.InternalWorkflowStartRequest;
import com.qlda.workflowservice.dto.internal.request.InternalWorkflowSubmitApprovalRequest;
import com.qlda.workflowservice.dto.internal.request.InternalWorkflowTransferRequest;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowMyDueSoonCountResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowMyOverdueCountResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowProgressResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStartResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStatisticsResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStatusResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowSubmitApprovalResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowTimelineItemResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowTransferResponse;
import com.qlda.workflowservice.dto.response.SlaViolationResponse;
import com.qlda.workflowservice.service.InternalWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "internal.auth.service-token=test-token",
        "internal.auth.allowed-services[0]=document-service"
})
class InternalWorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InternalWorkflowService internalWorkflowService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void startWorkflow_success() throws Exception {
        when(internalWorkflowService.startWorkflow(eq(1L), any()))
                .thenReturn(new InternalWorkflowStartResponse(1L, 2, 10L, "Van thu tiep nhan", 1));

        InternalWorkflowStartRequest request = new InternalWorkflowStartRequest(
                2, 100L, 1, "INCOMING", LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/internal/workflows/documents/1/start")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Service-Name", "document-service")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.processingId").value(10));
    }

    @Test
    void transferWorkflow_success() throws Exception {
        when(internalWorkflowService.transferWorkflow(eq(1L), any()))
                .thenReturn(new InternalWorkflowTransferResponse(20L, 1L, 200L, 1));

        InternalWorkflowTransferRequest request = new InternalWorkflowTransferRequest(
                100L, 200L, 1, 3L, "Chuyen xu ly", LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/internal/workflows/documents/1/transfer")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Service-Name", "document-service")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.processingId").value(20));
    }

    @Test
    void submitApproval_success() throws Exception {
        when(internalWorkflowService.submitApproval(eq(1L), any()))
                .thenReturn(new InternalWorkflowSubmitApprovalResponse(1L, 30L, 500L, 1));

        InternalWorkflowSubmitApprovalRequest request =
                new InternalWorkflowSubmitApprovalRequest(200L, 500L, "Trinh phe duyet");

        mockMvc.perform(post("/internal/workflows/documents/1/submit-approval")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Service-Name", "document-service")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nguoiPheDuyetId").value(500));
    }

    @Test
    void getStatus_success() throws Exception {
        when(internalWorkflowService.getStatus(1L))
                .thenReturn(new InternalWorkflowStatusResponse(1L, "Lanh dao", 1, 60, LocalDateTime.now().plusDays(1), false));

        mockMvc.perform(get("/internal/workflows/documents/1/status")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentId").value(1));
    }

    @Test
    void getTimeline_success() throws Exception {
        when(internalWorkflowService.getTimeline(1L)).thenReturn(List.of(
                new InternalWorkflowTimelineItemResponse(1L, "Buoc 1", 2L, "TRANSFER", LocalDateTime.now(), null, 1)
        ));

        mockMvc.perform(get("/internal/workflows/documents/1/timeline")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].processingId").value(1));
    }

    @Test
    void getStatistics_success() throws Exception {
        when(internalWorkflowService.getStatistics(any(), any(), any()))
                .thenReturn(new InternalWorkflowStatisticsResponse(10, 5, 3, 2));

        mockMvc.perform(get("/internal/workflows/statistics")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Service-Name", "document-service")
                        .param("fromDate", LocalDate.now().minusDays(1).toString())
                        .param("toDate", LocalDate.now().toString())
                        .param("donViId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTasks").value(10));
    }

    @Test
    void getProgress_success() throws Exception {
        when(internalWorkflowService.getProgress(any(), any(), any(), any()))
                .thenReturn(new InternalWorkflowProgressResponse(10, 5, 3, List.of()));

        mockMvc.perform(get("/internal/workflows/progress")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completedTasks").value(5));
    }

    @Test
    void getSlaViolations_success() throws Exception {
        when(internalWorkflowService.getSlaViolations(any(), any(), any()))
                .thenReturn(List.of(new SlaViolationResponse(1L, 1L, null, 2L, LocalDateTime.now().minusHours(2), null, 2)));

        mockMvc.perform(get("/internal/workflows/sla/violations")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].processingId").value(1));
    }

    @Test
    void getMyDueSoonCount_success() throws Exception {
        when(internalWorkflowService.getMyDueSoonCount(2L, 3))
                .thenReturn(new InternalWorkflowMyDueSoonCountResponse(2L, 3, 5));

        mockMvc.perform(get("/internal/workflows/statistics/my-due-soon-count")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Service-Name", "document-service")
                        .param("userId", "2")
                        .param("days", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Get my due soon document count successfully"))
                .andExpect(jsonPath("$.data.userId").value(2))
                .andExpect(jsonPath("$.data.days").value(3))
                .andExpect(jsonPath("$.data.count").value(5));
    }

    @Test
    void getMyOverdueCount_success() throws Exception {
        when(internalWorkflowService.getMyOverdueCount(2L))
                .thenReturn(new InternalWorkflowMyOverdueCountResponse(2L, 2));

        mockMvc.perform(get("/internal/workflows/statistics/my-overdue-count")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Service-Name", "document-service")
                        .param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Get my overdue document count successfully"))
                .andExpect(jsonPath("$.data.userId").value(2))
                .andExpect(jsonPath("$.data.count").value(2));
    }
}
