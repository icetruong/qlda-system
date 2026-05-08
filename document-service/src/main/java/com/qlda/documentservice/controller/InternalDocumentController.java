package com.qlda.documentservice.controller;

import com.qlda.documentservice.common.ApiResponse;
import com.qlda.documentservice.dto.internal.InternalDocumentRequests;
import com.qlda.documentservice.dto.internal.InternalDocumentResponses;
import com.qlda.documentservice.service.InternalDocumentService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/documents")
public class InternalDocumentController {

    private final InternalDocumentService internalDocumentService;

    public InternalDocumentController(InternalDocumentService internalDocumentService) {
        this.internalDocumentService = internalDocumentService;
    }

    @GetMapping("/{id}")
    public ApiResponse<InternalDocumentResponses.InternalDocumentResponse> getInternalDocument(@PathVariable Long id) {
        return ApiResponse.success("Get internal document successfully", internalDocumentService.getInternalDocument(id));
    }

    @GetMapping("/{id}/content")
    public ApiResponse<InternalDocumentResponses.InternalDocumentContentResponse> getContent(@PathVariable Long id) {
        return ApiResponse.success("Get document content successfully", internalDocumentService.getDocumentContent(id));
    }

    @GetMapping("/{id}/attachments")
    public ApiResponse<List<InternalDocumentResponses.InternalAttachmentResponse>> getAttachments(@PathVariable Long id) {
        return ApiResponse.success(
            "Get internal document attachments successfully",
            internalDocumentService.getDocumentAttachments(id)
        );
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<InternalDocumentResponses.UpdateStatusResponse> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody InternalDocumentRequests.UpdateStatusRequest request
    ) {
        return ApiResponse.success(
            "Update document status successfully",
            internalDocumentService.updateDocumentStatus(id, request)
        );
    }

    @PatchMapping("/{id}/assignee")
    public ApiResponse<InternalDocumentResponses.UpdateAssigneeResponse> updateAssignee(
        @PathVariable Long id,
        @Valid @RequestBody InternalDocumentRequests.UpdateAssigneeRequest request
    ) {
        return ApiResponse.success(
            "Update document assignee successfully",
            internalDocumentService.updateDocumentAssignee(id, request)
        );
    }

    @PatchMapping("/{id}/workflow-status")
    public ApiResponse<InternalDocumentResponses.UpdateWorkflowStatusResponse> updateWorkflowStatus(
        @PathVariable Long id,
        @Valid @RequestBody InternalDocumentRequests.UpdateWorkflowStatusRequest request
    ) {
        return ApiResponse.success(
            "Update document workflow status successfully",
            internalDocumentService.updateWorkflowStatus(id, request)
        );
    }

    @PatchMapping("/{id}/ocr-status")
    public ApiResponse<InternalDocumentResponses.UpdateOcrStatusResponse> updateOcrStatus(
        @PathVariable Long id,
        @Valid @RequestBody InternalDocumentRequests.UpdateOcrStatusRequest request
    ) {
        return ApiResponse.success("Update OCR status successfully", internalDocumentService.updateOcrStatus(id, request));
    }

    @PostMapping("/access-check")
    public ApiResponse<InternalDocumentResponses.AccessCheckResponse> checkAccess(
        @Valid @RequestBody InternalDocumentRequests.AccessCheckRequest request
    ) {
        return ApiResponse.success("Check document access successfully", internalDocumentService.checkDocumentAccess(request));
    }

    @GetMapping("/statistics")
    public ApiResponse<InternalDocumentResponses.InternalDocumentStatisticsResponse> getStatistics(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(required = false) Integer donViId,
        @RequestParam(required = false, defaultValue = "status") String groupBy
    ) {
        return ApiResponse.success(
            "Get internal document statistics successfully",
            internalDocumentService.getStatistics(fromDate, toDate, donViId, groupBy)
        );
    }

    @GetMapping("/statistics/my-uploaded-count")
    public ApiResponse<InternalDocumentResponses.MyUploadedDocumentCountResponse> getMyUploadedCount(@RequestParam Long userId) {
        return ApiResponse.success(
            "Get my uploaded document count successfully",
            internalDocumentService.getMyUploadedDocumentCount(userId)
        );
    }

    @GetMapping("/statistics/total-count")
    public ApiResponse<InternalDocumentResponses.TotalDocumentCountResponse> getTotalCount() {
        return ApiResponse.success("Get total document count successfully", internalDocumentService.getTotalDocumentCount());
    }

    @GetMapping("/overdue")
    public ApiResponse<InternalDocumentResponses.InternalOverdueDocumentsResponse> getOverdue(
        @RequestParam(required = false) Integer donViId,
        @RequestParam(required = false) Long nguoiXuLyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(
            "Get internal overdue documents successfully",
            internalDocumentService.getOverdueDocuments(donViId, nguoiXuLyId, page, size)
        );
    }
}
