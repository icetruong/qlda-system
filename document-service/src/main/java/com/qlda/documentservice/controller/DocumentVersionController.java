package com.qlda.documentservice.controller;

import com.qlda.documentservice.common.ApiResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.service.DocumentWorkflowService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
@PreAuthorize("hasAnyRole('ADMIN','CHUYEN_VIEN','LANH_DAO')")
public class DocumentVersionController {
    private final DocumentWorkflowService documentWorkflowService;

    public DocumentVersionController(DocumentWorkflowService documentWorkflowService) {
        this.documentWorkflowService = documentWorkflowService;
    }

    @PostMapping("/{id}/versions")
    public ApiResponse<DocumentResponses.DocumentVersionResponse> create(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.DocumentVersionCreateRequest request
    ) {
        return ApiResponse.success("Create document version successfully", documentWorkflowService.createVersion(id, request));
    }

    @GetMapping("/{id}/versions")
    public ApiResponse<List<DocumentResponses.DocumentVersionResponse>> list(@PathVariable Long id) {
        return ApiResponse.success("Get document versions successfully", documentWorkflowService.listVersions(id));
    }

    @GetMapping("/{id}/versions/compare")
    public ApiResponse<DocumentResponses.DocumentVersionCompareResponse> compare(
        @PathVariable Long id,
        @RequestParam String fromVersion,
        @RequestParam String toVersion
    ) {
        return ApiResponse.success(
            "Compare document versions successfully",
            documentWorkflowService.compareVersions(id, fromVersion, toVersion)
        );
    }

    @PostMapping("/{id}/versions/restore")
    public ApiResponse<DocumentResponses.DocumentVersionRestoreResponse> restore(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.DocumentVersionRestoreRequest request
    ) {
        return ApiResponse.success("Restore document version successfully", documentWorkflowService.restoreVersion(id, request));
    }

    @DeleteMapping("/{id}/versions/{versionName}")
    public ApiResponse<DocumentResponses.DocumentVersionDeleteResponse> delete(
        @PathVariable Long id,
        @PathVariable String versionName
    ) {
        return ApiResponse.success("Delete document version successfully", documentWorkflowService.deleteVersion(id, versionName));
    }
}

