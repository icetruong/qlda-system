package com.qlda.documentservice.controller;

import com.qlda.documentservice.common.ApiResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.service.DocumentWorkflowService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents/drafts")
@PreAuthorize("hasAnyRole('ADMIN','CHUYEN_VIEN','LANH_DAO')")
public class DraftController {
    private final DocumentWorkflowService documentWorkflowService;

    public DraftController(DocumentWorkflowService documentWorkflowService) {
        this.documentWorkflowService = documentWorkflowService;
    }

    @PostMapping
    public ApiResponse<DocumentResponses.DocumentSimpleResponse> create(@Valid @RequestBody DocumentRequests.DraftDocumentRequest request) {
        return ApiResponse.success("Create draft document successfully", documentWorkflowService.createDraft(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DocumentResponses.DocumentSimpleResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.DraftDocumentRequest request
    ) {
        return ApiResponse.success("Update draft document successfully", documentWorkflowService.updateDraft(id, request));
    }

    @PostMapping("/{id}/comments/request")
    public ApiResponse<DocumentResponses.DraftCommentResponse> requestComment(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.DraftCommentRequest request
    ) {
        return ApiResponse.success("Send comment request successfully", documentWorkflowService.requestDraftComment(id, request));
    }

    @PostMapping("/{id}/submit-signing")
    public ApiResponse<DocumentResponses.SubmitSigningResponse> submitSigning(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.SubmitSigningRequest request
    ) {
        return ApiResponse.success("Submit document for signing successfully", documentWorkflowService.submitDraftSigning(id, request));
    }
}

