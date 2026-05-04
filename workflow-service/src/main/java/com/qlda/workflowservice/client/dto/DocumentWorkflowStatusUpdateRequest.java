package com.qlda.workflowservice.client.dto;

public record DocumentWorkflowStatusUpdateRequest(
        String workflowStatus,
        Long processingId
) {
}
