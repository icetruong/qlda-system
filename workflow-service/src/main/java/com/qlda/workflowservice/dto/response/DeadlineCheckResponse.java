package com.qlda.workflowservice.dto.response;

public record DeadlineCheckResponse(
        long totalNearDeadline,
        long totalOverdue
) {
}
