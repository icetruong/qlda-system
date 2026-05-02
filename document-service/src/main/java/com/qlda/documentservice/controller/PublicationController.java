package com.qlda.documentservice.controller;

import com.qlda.documentservice.common.ApiResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.service.DocumentWorkflowService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
@PreAuthorize("hasAnyRole('ADMIN','CHUYEN_VIEN','LANH_DAO')")
public class PublicationController {
    private final DocumentWorkflowService documentWorkflowService;

    public PublicationController(DocumentWorkflowService documentWorkflowService) {
        this.documentWorkflowService = documentWorkflowService;
    }

    @PostMapping("/{id}/digital-sign")
    public ApiResponse<DocumentResponses.DigitalSignResponse> digitalSign(
        @PathVariable Long id,
        @RequestBody DocumentRequests.DigitalSignRequest request
    ) {
        return ApiResponse.success("Digital sign document successfully", documentWorkflowService.digitalSign(id, request));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<DocumentResponses.PublishResponse> publish(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.PublishRequest request
    ) {
        return ApiResponse.success("Publish document successfully", documentWorkflowService.publish(id, request));
    }

    @PostMapping("/{id}/send")
    public ApiResponse<DocumentResponses.SendDocumentResponse> send(
        @PathVariable Long id,
        @RequestBody DocumentRequests.SendDocumentRequest request
    ) {
        return ApiResponse.success("Send document successfully", documentWorkflowService.send(id, request));
    }
}

