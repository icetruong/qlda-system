package com.qlda.documentservice.controller;

import com.qlda.documentservice.common.ApiResponse;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.service.AttachmentService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@PreAuthorize("hasAnyRole('ADMIN','CHUYEN_VIEN','LANH_DAO')")
public class AttachmentController {
    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/{id}/attachments")
    public ApiResponse<DocumentResponses.AttachmentResponse> upload(
        @PathVariable Long id,
        @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success("Upload attachment successfully", attachmentService.upload(id, file));
    }

    @GetMapping("/{id}/attachments")
    public ApiResponse<List<DocumentResponses.AttachmentResponse>> list(@PathVariable Long id) {
        return ApiResponse.success("Get attachments successfully", attachmentService.list(id));
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ApiResponse<DocumentResponses.AttachmentDownloadResponse> download(@PathVariable Long attachmentId) {
        return ApiResponse.success("Get attachment download link successfully", attachmentService.download(attachmentId));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ApiResponse<DocumentResponses.IdResponse> delete(@PathVariable Long attachmentId) {
        return ApiResponse.success("Delete attachment successfully", attachmentService.delete(attachmentId));
    }
}

