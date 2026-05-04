package com.qlda.workflowservice.client.internal;

import com.qlda.workflowservice.client.dto.DocumentAssigneeUpdateRequest;
import com.qlda.workflowservice.client.dto.DocumentDetailDto;
import com.qlda.workflowservice.client.dto.DocumentStatusUpdateRequest;
import com.qlda.workflowservice.client.dto.DocumentWorkflowStatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "documentServiceInternalClient", url = "${services.document-service.base-url}")
public interface DocumentServiceHttpClient {
    @GetMapping("/internal/documents/{id}")
    DocumentDetailDto getDocumentById(@PathVariable("id") Long id);

    @PatchMapping("/internal/documents/{id}/status")
    void updateDocumentStatus(@PathVariable("id") Long id, @RequestBody DocumentStatusUpdateRequest request);

    @PatchMapping("/internal/documents/{id}/assignee")
    void updateDocumentAssignee(@PathVariable("id") Long id, @RequestBody DocumentAssigneeUpdateRequest request);

    @PatchMapping("/internal/documents/{id}/workflow-status")
    void updateDocumentWorkflowStatus(@PathVariable("id") Long id, @RequestBody DocumentWorkflowStatusUpdateRequest request);
}
