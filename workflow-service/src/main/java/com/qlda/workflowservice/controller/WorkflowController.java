package com.qlda.workflowservice.controller;

import com.qlda.workflowservice.common.ApiResponse;
import com.qlda.workflowservice.dto.request.*;
import com.qlda.workflowservice.dto.response.*;
import com.qlda.workflowservice.service.WorkflowApiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowApiService workflowApiService;

    @PostMapping
    public ApiResponse<WorkflowSummaryResponse> createWorkflow(@Valid @RequestBody WorkflowCreateRequest request) {
        return ApiResponse.ok("Create workflow successfully", workflowApiService.createWorkflow(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<IdResponse> updateWorkflow(@PathVariable Integer id, @Valid @RequestBody WorkflowUpdateRequest request) {
        return ApiResponse.ok("Update workflow successfully", workflowApiService.updateWorkflow(id, request));
    }

    @GetMapping
    public ApiResponse<?> getWorkflows(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer loaiVanBanId,
            @RequestParam(required = false) Boolean suDung,
            Pageable pageable
    ) {
        return ApiResponse.ok("Get workflows successfully", workflowApiService.getWorkflows(keyword, loaiVanBanId, suDung, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkflowDetailResponse> getWorkflowDetail(@PathVariable Integer id) {
        return ApiResponse.ok("Get workflow detail successfully", workflowApiService.getWorkflowDetail(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<IdResponse> deleteWorkflow(@PathVariable Integer id) {
        return ApiResponse.ok("Delete workflow successfully", workflowApiService.deleteWorkflow(id));
    }

    @PostMapping("/{workflowId}/steps")
    public ApiResponse<WorkflowStepResponse> createStep(
            @PathVariable Integer workflowId,
            @Valid @RequestBody WorkflowStepCreateRequest request
    ) {
        return ApiResponse.ok("Create workflow step successfully", workflowApiService.createWorkflowStep(workflowId, request));
    }

    @PutMapping("/{workflowId}/steps/{stepId}")
    public ApiResponse<IdResponse> updateStep(
            @PathVariable Integer workflowId,
            @PathVariable Long stepId,
            @Valid @RequestBody WorkflowStepUpdateRequest request
    ) {
        return ApiResponse.ok("Update workflow step successfully", workflowApiService.updateWorkflowStep(workflowId, stepId, request));
    }

    @DeleteMapping("/{workflowId}/steps/{stepId}")
    public ApiResponse<IdResponse> deleteStep(@PathVariable Integer workflowId, @PathVariable Long stepId) {
        return ApiResponse.ok("Delete workflow step successfully", workflowApiService.deleteWorkflowStep(workflowId, stepId));
    }

    @PatchMapping("/{workflowId}/steps/{stepId}/sla")
    public ApiResponse<SlaResponse> updateStepSla(
            @PathVariable Integer workflowId,
            @PathVariable Long stepId,
            @Valid @RequestBody SlaUpdateRequest request
    ) {
        return ApiResponse.ok("Update workflow step SLA successfully", workflowApiService.updateStepSla(workflowId, stepId, request));
    }

    @GetMapping("/approvals/pending")
    public ApiResponse<?> getPendingApprovals(
            @RequestParam Long nguoiDuyetId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable
    ) {
        return ApiResponse.ok("Get pending approvals successfully",
                workflowApiService.getPendingApprovals(nguoiDuyetId, keyword, fromDate, toDate, pageable));
    }

    @PostMapping("/approvals/{processingId}/comment")
    public ApiResponse<ApprovalActionResponse> commentApproval(
            @PathVariable Long processingId,
            @Valid @RequestBody ApprovalCommentRequest request
    ) {
        return ApiResponse.ok("Comment approval successfully", workflowApiService.commentApproval(processingId, request));
    }

    @PostMapping("/approvals/{processingId}/approve")
    public ApiResponse<ApprovalActionResponse> approveDocument(
            @PathVariable Long processingId,
            @Valid @RequestBody ApprovalApproveRequest request
    ) {
        return ApiResponse.ok("Approve document successfully", workflowApiService.approveDocument(processingId, request));
    }

    @PostMapping("/approvals/{processingId}/reject")
    public ApiResponse<ApprovalActionResponse> rejectDocument(
            @PathVariable Long processingId,
            @Valid @RequestBody ApprovalRejectRequest request
    ) {
        return ApiResponse.ok("Reject document successfully", workflowApiService.rejectDocument(processingId, request));
    }

    @PostMapping("/delegations")
    public ApiResponse<DelegationResponse> createDelegation(@Valid @RequestBody DelegationCreateRequest request) {
        return ApiResponse.ok("Create delegation successfully", workflowApiService.createDelegation(request));
    }

    @GetMapping("/delegations")
    public ApiResponse<?> getDelegations(
            @RequestParam(required = false) Long nguoiUyQuyenId,
            @RequestParam(required = false) Long nguoiDuocUyQuyenId,
            @RequestParam(required = false) Boolean active,
            Pageable pageable
    ) {
        return ApiResponse.ok("Get delegations successfully",
                workflowApiService.getDelegations(nguoiUyQuyenId, nguoiDuocUyQuyenId, active, pageable));
    }

    @DeleteMapping("/delegations/{id}")
    public ApiResponse<IdResponse> cancelDelegation(@PathVariable Long id) {
        return ApiResponse.ok("Cancel delegation successfully", workflowApiService.cancelDelegation(id));
    }

    @GetMapping("/documents/{documentId}/status")
    public ApiResponse<DocumentStatusResponse> getDocumentStatus(@PathVariable Long documentId) {
        return ApiResponse.ok("Get document workflow status successfully", workflowApiService.getDocumentStatus(documentId));
    }

    @GetMapping("/documents/{documentId}/timeline")
    public ApiResponse<List<TimelineItemResponse>> getDocumentTimeline(@PathVariable Long documentId) {
        return ApiResponse.ok("Get document timeline successfully", workflowApiService.getDocumentTimeline(documentId));
    }

    @GetMapping("/processings/{processingId}")
    public ApiResponse<ProcessingDetailResponse> getProcessingDetail(@PathVariable Long processingId) {
        return ApiResponse.ok("Get processing detail successfully", workflowApiService.getProcessingDetail(processingId));
    }

    @PostMapping("/reminders/check-deadlines")
    public ApiResponse<DeadlineCheckResponse> checkDeadlines(@Valid @RequestBody ReminderCheckDeadlineRequest request) {
        return ApiResponse.ok("Check deadlines successfully", workflowApiService.checkDeadlines(request));
    }

    @PostMapping("/reminders/send")
    public ApiResponse<ReminderSendResponse> sendReminders(@Valid @RequestBody ReminderSendRequest request) {
        return ApiResponse.ok("Send reminders successfully", workflowApiService.sendReminders(request));
    }

    @PostMapping("/documents/{documentId}/transfer")
    public ApiResponse<TransferResponse> transferDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody TransferDocumentRequest request
    ) {
        return ApiResponse.ok("Transfer document successfully", workflowApiService.transferDocument(documentId, request));
    }

    @PostMapping("/processings/{processingId}/receive")
    public ApiResponse<ReceiveResponse> receiveDocument(
            @PathVariable Long processingId,
            @Valid @RequestBody ReceiveProcessingRequest request
    ) {
        return ApiResponse.ok("Receive document successfully", workflowApiService.receiveDocument(processingId, request));
    }

    @PostMapping("/processings/{processingId}/complete")
    public ApiResponse<CompleteResponse> completeProcessing(
            @PathVariable Long processingId,
            @Valid @RequestBody CompleteProcessingRequest request
    ) {
        return ApiResponse.ok("Complete processing successfully", workflowApiService.completeProcessing(processingId, request));
    }

    @GetMapping("/sla")
    public ApiResponse<List<SlaResponse>> getSlaList(@RequestParam(required = false) Integer workflowId) {
        return ApiResponse.ok("Get SLA list successfully", workflowApiService.getSlaList(workflowId));
    }

    @GetMapping("/sla/violations")
    public ApiResponse<List<SlaViolationResponse>> getSlaViolations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer donViId
    ) {
        return ApiResponse.ok("Get SLA violations successfully", workflowApiService.getSlaViolations(fromDate, toDate, donViId));
    }
}
