package com.qlda.workflowservice.client;

import com.qlda.workflowservice.client.dto.DocumentAssigneeUpdateRequest;
import com.qlda.workflowservice.client.dto.DocumentDetailDto;
import com.qlda.workflowservice.client.dto.DocumentStatusUpdateRequest;
import com.qlda.workflowservice.client.dto.DocumentWorkflowStatusUpdateRequest;

public interface DocumentServiceClient {
    DocumentDetailDto getDocumentById(Long id);

    void updateDocumentStatus(Long id, DocumentStatusUpdateRequest request);

    void updateDocumentAssignee(Long id, DocumentAssigneeUpdateRequest request);

    void updateDocumentWorkflowStatus(Long id, DocumentWorkflowStatusUpdateRequest request);
}
