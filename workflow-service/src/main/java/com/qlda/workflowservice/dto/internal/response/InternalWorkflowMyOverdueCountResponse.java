package com.qlda.workflowservice.dto.internal.response;

public record InternalWorkflowMyOverdueCountResponse(
        Long userId,
        long count
) {
}
