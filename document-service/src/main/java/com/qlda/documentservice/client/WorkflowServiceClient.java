package com.qlda.documentservice.client;

import com.qlda.documentservice.client.dto.WorkflowClientDtos;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "workflow-service-client", url = "${services.workflow-service.base-url:http://localhost:8083}")
public interface WorkflowServiceClient {

    @PostMapping("/internal/workflows/documents/{documentId}/start")
    WorkflowClientDtos.StartWorkflowResponse startWorkflow(
        @PathVariable("documentId") Long documentId,
        @RequestBody WorkflowClientDtos.StartWorkflowRequest request
    );

    @PostMapping("/internal/workflows/documents/{documentId}/transfer")
    WorkflowClientDtos.TransferWorkflowResponse transferWorkflow(
        @PathVariable("documentId") Long documentId,
        @RequestBody WorkflowClientDtos.TransferWorkflowRequest request
    );

    @PostMapping("/internal/workflows/documents/{documentId}/submit-approval")
    WorkflowClientDtos.SubmitApprovalResponse submitApproval(
        @PathVariable("documentId") Long documentId,
        @RequestBody WorkflowClientDtos.SubmitApprovalRequest request
    );

    @GetMapping("/internal/workflows/documents/{documentId}/status")
    WorkflowClientDtos.WorkflowStatusResponse getWorkflowStatus(@PathVariable("documentId") Long documentId);

    @GetMapping("/internal/workflows/documents/{documentId}/timeline")
    List<WorkflowClientDtos.WorkflowTimelineItem> getWorkflowTimeline(@PathVariable("documentId") Long documentId);
}
