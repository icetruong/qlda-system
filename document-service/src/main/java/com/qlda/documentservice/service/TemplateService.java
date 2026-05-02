package com.qlda.documentservice.service;

import com.qlda.documentservice.common.PageResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import org.springframework.data.domain.Pageable;

public interface TemplateService {
    DocumentResponses.TemplateSimpleResponse create(DocumentRequests.TemplateCreateRequest request);

    DocumentResponses.IdResponse update(Integer id, DocumentRequests.TemplateUpdateRequest request);

    DocumentResponses.IdResponse delete(Integer id);

    PageResponse<DocumentResponses.TemplateListItemResponse> list(String keyword, Integer loaiVanBanId, Boolean suDung, Pageable pageable);

    DocumentResponses.TemplateDetailResponse detail(Integer id);

    DocumentResponses.ApplyTemplateResponse apply(Integer templateId, DocumentRequests.ApplyTemplateRequest request);

    DocumentResponses.CreateFromTemplateResponse createFromTemplate(DocumentRequests.CreateFromTemplateRequest request);
}

