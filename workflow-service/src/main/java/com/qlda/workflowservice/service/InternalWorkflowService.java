package com.qlda.workflowservice.service;

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

import java.time.LocalDate;
import java.util.List;

public interface InternalWorkflowService {
    InternalWorkflowStartResponse startWorkflow(Long documentId, InternalWorkflowStartRequest request);

    InternalWorkflowTransferResponse transferWorkflow(Long documentId, InternalWorkflowTransferRequest request);

    InternalWorkflowSubmitApprovalResponse submitApproval(Long documentId, InternalWorkflowSubmitApprovalRequest request);

    InternalWorkflowStatusResponse getStatus(Long documentId);

    List<InternalWorkflowTimelineItemResponse> getTimeline(Long documentId);

    InternalWorkflowStatisticsResponse getStatistics(LocalDate fromDate, LocalDate toDate, Integer donViId);

    InternalWorkflowProgressResponse getProgress(LocalDate fromDate, LocalDate toDate, Integer donViId, Long nguoiXuLyId);

    List<SlaViolationResponse> getSlaViolations(LocalDate fromDate, LocalDate toDate, Integer donViId);
}
