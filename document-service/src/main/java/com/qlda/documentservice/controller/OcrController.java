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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@PreAuthorize("hasAnyRole('ADMIN','CHUYEN_VIEN','LANH_DAO')")
public class OcrController {
    private final DocumentWorkflowService documentWorkflowService;

    public OcrController(DocumentWorkflowService documentWorkflowService) {
        this.documentWorkflowService = documentWorkflowService;
    }

    @PostMapping("/{id}/ocr/upload")
    public ApiResponse<DocumentResponses.OcrUploadResponse> upload(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return ApiResponse.success("Upload OCR file successfully", documentWorkflowService.uploadOcrFile(id, file));
    }

    @PostMapping("/{id}/ocr/process")
    public ApiResponse<DocumentResponses.OcrProcessResponse> process(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.OcrProcessRequest request
    ) {
        return ApiResponse.success("OCR processed successfully", documentWorkflowService.processOcr(id, request));
    }

    @PostMapping("/{id}/ocr/save")
    public ApiResponse<DocumentResponses.OcrSaveResponse> save(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.OcrSaveRequest request
    ) {
        return ApiResponse.success("Save OCR result successfully", documentWorkflowService.saveOcr(id, request));
    }
}

