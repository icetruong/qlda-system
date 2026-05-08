package com.qlda.workflowservice.dto.internal.response;

public record InternalWorkflowMyDueSoonCountResponse(
        Long userId,
        Integer days,
        long count
) {
}
