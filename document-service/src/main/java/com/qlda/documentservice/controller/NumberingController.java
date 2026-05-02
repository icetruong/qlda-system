package com.qlda.documentservice.controller;

import com.qlda.documentservice.common.ApiResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.service.DocumentWorkflowService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
@PreAuthorize("hasAnyRole('ADMIN','CHUYEN_VIEN','LANH_DAO')")
public class NumberingController {
    private final DocumentWorkflowService documentWorkflowService;

    public NumberingController(DocumentWorkflowService documentWorkflowService) {
        this.documentWorkflowService = documentWorkflowService;
    }

    @PostMapping("/numbering/generate")
    public ApiResponse<DocumentResponses.NumberGenerateResponse> generate(@RequestBody DocumentRequests.GenerateNumberRequest request) {
        return ApiResponse.success("Generate document number successfully", documentWorkflowService.generateNumber(request));
    }

    @GetMapping("/numbering/check")
    public ApiResponse<DocumentResponses.NumberCheckResponse> check(@RequestParam String soKyHieu) {
        return ApiResponse.success("Check document number successfully", documentWorkflowService.checkNumber(soKyHieu));
    }

    @PatchMapping("/{id}/number")
    public ApiResponse<DocumentResponses.NumberAssignResponse> assign(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.AssignNumberRequest request
    ) {
        return ApiResponse.success("Assign document number successfully", documentWorkflowService.assignNumber(id, request));
    }
}

