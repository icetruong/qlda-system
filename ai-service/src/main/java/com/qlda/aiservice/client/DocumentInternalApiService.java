package com.qlda.aiservice.client;

import com.qlda.aiservice.dto.internal.DocumentAttachmentDto;
import com.qlda.aiservice.dto.internal.DocumentContentDto;
import com.qlda.aiservice.dto.internal.DocumentMetadataDto;

import java.util.List;
import java.util.Set;

public interface DocumentInternalApiService {
    DocumentMetadataDto getDocument(Long id);

    DocumentContentDto getDocumentContent(Long id);

    List<DocumentAttachmentDto> getDocumentAttachments(Long id);

    void updateOcrStatus(Long id, boolean daOcr);

    Set<Long> checkDocumentAccess(Long userId, List<Long> documentIds);

    long getMyUploadedDocumentCount(Long userId);

    long getTotalDocumentCount();
}
