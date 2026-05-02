package com.qlda.documentservice.service;

import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import java.util.List;

public interface DocumentTypeService {
    DocumentResponses.DocumentTypeResponse create(DocumentRequests.DocumentTypeCreateRequest request);

    DocumentResponses.IdResponse update(Integer id, DocumentRequests.DocumentTypeUpdateRequest request);

    List<DocumentResponses.DocumentTypeResponse> list(String keyword, Boolean suDung);

    DocumentResponses.DocumentTypeResponse detail(Integer id);

    DocumentResponses.IdResponse delete(Integer id);
}

