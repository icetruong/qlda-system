package com.qlda.documentservice.service;

import com.qlda.documentservice.common.PageResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import org.springframework.data.domain.Pageable;

public interface CaseFileService {
    DocumentResponses.CaseFileSimpleResponse create(DocumentRequests.CaseFileCreateRequest request);

    DocumentResponses.IdResponse update(Long id, DocumentRequests.CaseFileUpdateRequest request);

    DocumentResponses.CaseFileAttachResponse attachDocument(Long id, DocumentRequests.CaseFileAttachDocumentRequest request);

    PageResponse<DocumentResponses.CaseFileListItemResponse> list(String keyword, Integer donViId, Long nguoiPhuTrachId, Integer trangThai, Pageable pageable);

    DocumentResponses.CaseFileDetailResponse detail(Long id);

    DocumentResponses.IdResponse delete(Long id);

    DocumentResponses.CaseFileClassificationResponse classify(Long id, DocumentRequests.CaseFileClassificationRequest request);

    PageResponse<DocumentResponses.CaseFileClassificationItemResponse> searchClassification(String nhomHoSo, Pageable pageable);
}

