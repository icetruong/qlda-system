package com.qlda.workflowservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.workflowservice.common.PageResponse;
import com.qlda.workflowservice.dto.request.*;
import com.qlda.workflowservice.dto.response.*;
import com.qlda.workflowservice.exception.ApiException;
import com.qlda.workflowservice.exception.ErrorCode;
import com.qlda.workflowservice.service.WorkflowApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkflowApiService workflowApiService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void requestWithoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/workflows"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithToken_canAccessProtectedEndpoint() throws Exception {
        PageResponse<WorkflowSummaryResponse> page = PageResponse.<WorkflowSummaryResponse>builder()
                .content(List.of(new WorkflowSummaryResponse(1, "QT01", "Ten", 1, 0, true)))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .build();
        when(workflowApiService.getWorkflows(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/workflows")
                        .with(jwt().jwt(j -> j.claim("roles", List.of("ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void createWorkflow_success() throws Exception {
        WorkflowCreateRequest request = new WorkflowCreateRequest("MA01", "Ten quy trinh", 1, "Mo ta", true);
        when(workflowApiService.createWorkflow(any(WorkflowCreateRequest.class)))
                .thenReturn(new WorkflowSummaryResponse(5, "MA01", "Ten quy trinh", 1, 0, true));

        mockMvc.perform(post("/api/workflows")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    void createWorkflow_invalidRequest_returns400() throws Exception {
        WorkflowCreateRequest invalid = new WorkflowCreateRequest("", "Ten", 1, null, true);

        mockMvc.perform(post("/api/workflows")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }

    @Test
    void updateWorkflowStep_success() throws Exception {
        WorkflowStepUpdateRequest request = new WorkflowStepUpdateRequest("Buoc X", 2, "LD", 8, true, "note");
        when(workflowApiService.updateWorkflowStep(eq(1), eq(11L), any(WorkflowStepUpdateRequest.class)))
                .thenReturn(new IdResponse(11L));

        mockMvc.perform(put("/api/workflows/1/steps/11")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(11));
    }

    @Test
    void approveDocument_success() throws Exception {
        when(workflowApiService.approveDocument(eq(10L), any(ApprovalApproveRequest.class)))
                .thenReturn(new ApprovalActionResponse(10L, 100L, 2, "OK", LocalDateTime.now()));

        mockMvc.perform(post("/api/workflows/approvals/10/approve")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ApprovalApproveRequest("OK", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.processingId").value(10))
                .andExpect(jsonPath("$.data.trangThaiXuLy").value(2));
    }

    @Test
    void getSlaViolations_success() throws Exception {
        when(workflowApiService.getSlaViolations(any(), any(), any()))
                .thenReturn(List.of(new SlaViolationResponse(1L, 2L, null, 3L,
                        LocalDateTime.now().minusHours(5), null, 5)));

        mockMvc.perform(get("/api/workflows/sla/violations")
                        .with(jwt())
                        .param("fromDate", LocalDate.now().minusDays(1).toString())
                        .param("toDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].processingId").value(1));
    }

    @Test
    void getDelegations_success() throws Exception {
        PageResponse<DelegationResponse> response = PageResponse.<DelegationResponse>builder()
                .content(List.of(new DelegationResponse(1L, 1L, 2L, LocalDate.now(), LocalDate.now().plusDays(1), "APPROVE", true)))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .build();
        when(workflowApiService.getDelegations(any(), any(), any(), any(Pageable.class))).thenReturn(response);

        mockMvc.perform(get("/api/workflows/delegations")
                        .with(jwt())
                        .param("nguoiUyQuyenId", "1")
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getWorkflowDetail_notFound_returnsStructuredError() throws Exception {
        when(workflowApiService.getWorkflowDetail(404))
                .thenThrow(new ApiException(ErrorCode.WORKFLOW_NOT_FOUND, HttpStatus.NOT_FOUND, "Workflow not found"));

        mockMvc.perform(get("/api/workflows/404").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("WORKFLOW_NOT_FOUND"));
    }
}
