package com.qlda.documentservice.service;

import com.qlda.documentservice.dto.internal.InternalDocumentRequests;
import com.qlda.documentservice.dto.internal.InternalDocumentResponses;
import java.time.LocalDate;
import java.util.List;

public interface InternalDocumentService {
    InternalDocumentResponses.InternalDocumentResponse getInternalDocument(Long id);

    InternalDocumentResponses.InternalDocumentContentResponse getDocumentContent(Long id);

    List<InternalDocumentResponses.InternalAttachmentResponse> getDocumentAttachments(Long id);

    InternalDocumentResponses.UpdateStatusResponse updateDocumentStatus(Long id, InternalDocumentRequests.UpdateStatusRequest request);

    InternalDocumentResponses.UpdateAssigneeResponse updateDocumentAssignee(Long id, InternalDocumentRequests.UpdateAssigneeRequest request);

    InternalDocumentResponses.UpdateWorkflowStatusResponse updateWorkflowStatus(Long id, InternalDocumentRequests.UpdateWorkflowStatusRequest request);

    InternalDocumentResponses.UpdateOcrStatusResponse updateOcrStatus(Long id, InternalDocumentRequests.UpdateOcrStatusRequest request);

    InternalDocumentResponses.InternalDocumentStatisticsResponse getStatistics(
        LocalDate fromDate,
        LocalDate toDate,
        Integer donViId,
        String groupBy
    );

    InternalDocumentResponses.InternalOverdueDocumentsResponse getOverdueDocuments(
        Integer donViId,
        Long nguoiXuLyId,
        int page,
        int size
    );
}
