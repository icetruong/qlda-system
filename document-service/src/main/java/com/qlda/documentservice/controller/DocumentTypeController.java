package com.qlda.documentservice.controller;

import com.qlda.documentservice.common.ApiResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.service.DocumentTypeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents/types")
@PreAuthorize("hasAnyRole('ADMIN','CHUYEN_VIEN','LANH_DAO')")
public class DocumentTypeController {
    private final DocumentTypeService documentTypeService;

    public DocumentTypeController(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    @PostMapping
    public ApiResponse<DocumentResponses.DocumentTypeResponse> create(
        @Valid @RequestBody DocumentRequests.DocumentTypeCreateRequest request
    ) {
        return ApiResponse.success("Create document type successfully", documentTypeService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DocumentResponses.IdResponse> update(
        @PathVariable Integer id,
        @Valid @RequestBody DocumentRequests.DocumentTypeUpdateRequest request
    ) {
        return ApiResponse.success("Update document type successfully", documentTypeService.update(id, request));
    }

    @GetMapping
    public ApiResponse<List<DocumentResponses.DocumentTypeResponse>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean suDung
    ) {
        return ApiResponse.success("Get document types successfully", documentTypeService.list(keyword, suDung));
    }

    @GetMapping("/{id}")
    public ApiResponse<DocumentResponses.DocumentTypeResponse> detail(@PathVariable Integer id) {
        return ApiResponse.success("Get document type detail successfully", documentTypeService.detail(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<DocumentResponses.IdResponse> delete(@PathVariable Integer id) {
        return ApiResponse.success("Delete document type successfully", documentTypeService.delete(id));
    }
}

