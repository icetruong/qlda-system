package com.qlda.workflowservice.service;

import com.qlda.workflowservice.common.PageResponse;
import com.qlda.workflowservice.dto.request.*;
import com.qlda.workflowservice.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface WorkflowApiService {
    WorkflowSummaryResponse createWorkflow(WorkflowCreateRequest request);

    IdResponse updateWorkflow(Integer id, WorkflowUpdateRequest request);

    PageResponse<WorkflowSummaryResponse> getWorkflows(String keyword, Integer loaiVanBanId, Boolean suDung, Pageable pageable);

    WorkflowDetailResponse getWorkflowDetail(Integer id);

    IdResponse deleteWorkflow(Integer id);

    WorkflowStepResponse createWorkflowStep(Integer workflowId, WorkflowStepCreateRequest request);

    IdResponse updateWorkflowStep(Integer workflowId, Long stepId, WorkflowStepUpdateRequest request);

    IdResponse deleteWorkflowStep(Integer workflowId, Long stepId);

    SlaResponse updateStepSla(Integer workflowId, Long stepId, SlaUpdateRequest request);

    PageResponse<PendingApprovalResponse> getPendingApprovals(Long nguoiDuyetId, String keyword, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    ApprovalActionResponse commentApproval(Long processingId, ApprovalCommentRequest request);

    ApprovalActionResponse approveDocument(Long processingId, ApprovalApproveRequest request);

    ApprovalActionResponse rejectDocument(Long processingId, ApprovalRejectRequest request);

    DelegationResponse createDelegation(DelegationCreateRequest request);

    PageResponse<DelegationResponse> getDelegations(Long nguoiUyQuyenId, Long nguoiDuocUyQuyenId, Boolean active, Pageable pageable);

    IdResponse cancelDelegation(Long id);

    DocumentStatusResponse getDocumentStatus(Long documentId);

    List<TimelineItemResponse> getDocumentTimeline(Long documentId);

    ProcessingDetailResponse getProcessingDetail(Long processingId);

    DeadlineCheckResponse checkDeadlines(ReminderCheckDeadlineRequest request);

    ReminderSendResponse sendReminders(ReminderSendRequest request);

    TransferResponse transferDocument(Long documentId, TransferDocumentRequest request);

    ReceiveResponse receiveDocument(Long processingId, ReceiveProcessingRequest request);

    CompleteResponse completeProcessing(Long processingId, CompleteProcessingRequest request);

    List<SlaResponse> getSlaList(Integer workflowId);

    List<SlaViolationResponse> getSlaViolations(LocalDate fromDate, LocalDate toDate, Integer donViId);
}
