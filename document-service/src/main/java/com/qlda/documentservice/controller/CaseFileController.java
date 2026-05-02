package com.qlda.documentservice.controller;

import com.qlda.documentservice.common.ApiResponse;
import com.qlda.documentservice.common.PageResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.service.CaseFileService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents/case-files")
@PreAuthorize("hasAnyRole('ADMIN','CHUYEN_VIEN','LANH_DAO')")
public class CaseFileController {
    private final CaseFileService caseFileService;

    public CaseFileController(CaseFileService caseFileService) {
        this.caseFileService = caseFileService;
    }

    @PostMapping
    public ApiResponse<DocumentResponses.CaseFileSimpleResponse> create(@Valid @RequestBody DocumentRequests.CaseFileCreateRequest request) {
        return ApiResponse.success("Create case file successfully", caseFileService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DocumentResponses.IdResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.CaseFileUpdateRequest request
    ) {
        return ApiResponse.success("Update case file successfully", caseFileService.update(id, request));
    }

    @PostMapping("/{id}/documents")
    public ApiResponse<DocumentResponses.CaseFileAttachResponse> attachDocument(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.CaseFileAttachDocumentRequest request
    ) {
        return ApiResponse.success("Attach document to case file successfully", caseFileService.attachDocument(id, request));
    }

    @GetMapping
    public ApiResponse<PageResponse<DocumentResponses.CaseFileListItemResponse>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer donViId,
        @RequestParam(required = false) Long nguoiPhuTrachId,
        @RequestParam(required = false) Integer trangThai,
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(
            "Get case files successfully",
            caseFileService.list(keyword, donViId, nguoiPhuTrachId, trangThai, pageable)
        );
    }

    @GetMapping("/classification")
    public ApiResponse<PageResponse<DocumentResponses.CaseFileClassificationItemResponse>> searchClassification(
        @RequestParam(required = false) String nhomHoSo,
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(
            "Search case files by classification successfully",
            caseFileService.searchClassification(nhomHoSo, pageable)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<DocumentResponses.CaseFileDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success("Get case file detail successfully", caseFileService.detail(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<DocumentResponses.IdResponse> delete(@PathVariable Long id) {
        return ApiResponse.success("Delete case file successfully", caseFileService.delete(id));
    }

    @PatchMapping("/{id}/classification")
    public ApiResponse<DocumentResponses.CaseFileClassificationResponse> classify(
        @PathVariable Long id,
        @Valid @RequestBody DocumentRequests.CaseFileClassificationRequest request
    ) {
        return ApiResponse.success("Classify case file successfully", caseFileService.classify(id, request));
    }
}

