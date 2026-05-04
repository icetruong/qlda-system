package com.qlda.workflowservice.dto.internal.response;

import java.util.List;

public record InternalWorkflowProgressResponse(
        long totalTasks,
        long completedTasks,
        long processingTasks,
        List<InternalWorkflowProgressItemResponse> items
) {
}
