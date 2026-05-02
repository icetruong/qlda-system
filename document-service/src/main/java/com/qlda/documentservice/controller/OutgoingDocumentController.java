package com.qlda.documentservice.controller;

import com.qlda.documentservice.common.ApiResponse;
import com.qlda.documentservice.common.PageResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.service.DocumentWorkflowService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents/outgoing")
@PreAuthorize("hasAnyRole('ADMIN','CHUYEN_VIEN','LANH_DAO')")
public class OutgoingDocumentController {
    private final DocumentWorkflowService documentWorkflowService;

    public OutgoingDocumentController(DocumentWorkflowService documentWorkflowService) {
        this.documentWorkflowService = documentWorkflowService;
    }

    @PostMapping
    public ApiResponse<DocumentResponses.DocumentSimpleResponse> create(@Valid @RequestBody DocumentRequests.OutgoingDocumentRequest request) {
        return ApiResponse.success("Create outgoing document successfully", documentWorkflowService.createOutgoing(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DocumentResponses.DocumentSimpleResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.OutgoingDocumentRequest request
    ) {
        return ApiResponse.success("Update outgoing document successfully", documentWorkflowService.updateOutgoing(id, request));
    }

    @PostMapping("/{id}/submit-approval")
    public ApiResponse<DocumentResponses.SubmitApprovalResponse> submitApproval(
        @PathVariable Long id,
        @RequestBody DocumentRequests.SubmitApprovalRequest request
    ) {
        return ApiResponse.success(
            "Submit outgoing document for approval successfully",
            documentWorkflowService.submitOutgoingApproval(id, request)
        );
    }

    @GetMapping
    public ApiResponse<PageResponse<DocumentResponses.DocumentListItemResponse>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer loaiVanBanId,
        @RequestParam(required = false) Integer trangThai,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(
            "Get outgoing documents successfully",
            documentWorkflowService.listOutgoing(keyword, loaiVanBanId, trangThai, fromDate, toDate, pageable)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<DocumentResponses.DocumentDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success("Get outgoing document detail successfully", documentWorkflowService.getOutgoingDetail(id));
    }
}

