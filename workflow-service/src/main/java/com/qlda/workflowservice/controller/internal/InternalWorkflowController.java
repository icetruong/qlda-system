package com.qlda.workflowservice.controller.internal;

import com.qlda.workflowservice.common.ApiResponse;
import com.qlda.workflowservice.dto.internal.request.InternalWorkflowStartRequest;
import com.qlda.workflowservice.dto.internal.request.InternalWorkflowSubmitApprovalRequest;
import com.qlda.workflowservice.dto.internal.request.InternalWorkflowTransferRequest;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowProgressResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStartResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStatisticsResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStatusResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowSubmitApprovalResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowTimelineItemResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowTransferResponse;
import com.qlda.workflowservice.dto.response.SlaViolationResponse;
import com.qlda.workflowservice.service.InternalWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/workflows")
public class InternalWorkflowController {
    private final InternalWorkflowService internalWorkflowService;

    @PostMapping("/documents/{documentId}/start")
    public ApiResponse<InternalWorkflowStartResponse> startWorkflow(@PathVariable Long documentId,
                                                                    @Valid @RequestBody InternalWorkflowStartRequest request) {
        return ApiResponse.ok("Start document workflow successfully", internalWorkflowService.startWorkflow(documentId, request));
    }

    @PostMapping("/documents/{documentId}/transfer")
    public ApiResponse<InternalWorkflowTransferResponse> transferWorkflow(@PathVariable Long documentId,
                                                                          @Valid @RequestBody InternalWorkflowTransferRequest request) {
        return ApiResponse.ok("Transfer document workflow successfully", internalWorkflowService.transferWorkflow(documentId, request));
    }

    @PostMapping("/documents/{documentId}/submit-approval")
    public ApiResponse<InternalWorkflowSubmitApprovalResponse> submitApproval(@PathVariable Long documentId,
                                                                               @Valid @RequestBody InternalWorkflowSubmitApprovalRequest request) {
        return ApiResponse.ok("Submit document approval workflow successfully", internalWorkflowService.submitApproval(documentId, request));
    }

    @GetMapping("/documents/{documentId}/status")
    public ApiResponse<InternalWorkflowStatusResponse> getStatus(@PathVariable Long documentId) {
        return ApiResponse.ok("Get internal workflow status successfully", internalWorkflowService.getStatus(documentId));
    }

    @GetMapping("/documents/{documentId}/timeline")
    public ApiResponse<List<InternalWorkflowTimelineItemResponse>> getTimeline(@PathVariable Long documentId) {
        return ApiResponse.ok("Get internal workflow timeline successfully", internalWorkflowService.getTimeline(documentId));
    }

    @GetMapping("/statistics")
    public ApiResponse<InternalWorkflowStatisticsResponse> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer donViId
    ) {
        return ApiResponse.ok("Get internal workflow statistics successfully", internalWorkflowService.getStatistics(fromDate, toDate, donViId));
    }

    @GetMapping("/progress")
    public ApiResponse<InternalWorkflowProgressResponse> getProgress(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer donViId,
            @RequestParam(required = false) Long nguoiXuLyId
    ) {
        return ApiResponse.ok("Get internal workflow progress successfully", internalWorkflowService.getProgress(fromDate, toDate, donViId, nguoiXuLyId));
    }

    @GetMapping("/sla/violations")
    public ApiResponse<List<SlaViolationResponse>> getSlaViolations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer donViId
    ) {
        return ApiResponse.ok("Get internal SLA violations successfully", internalWorkflowService.getSlaViolations(fromDate, toDate, donViId));
    }
}
