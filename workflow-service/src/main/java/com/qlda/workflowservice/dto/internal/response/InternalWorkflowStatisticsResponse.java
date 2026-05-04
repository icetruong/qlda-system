package com.qlda.workflowservice.dto.internal.response;

public record InternalWorkflowStatisticsResponse(
        long totalTasks,
        long completedTasks,
        long processingTasks,
        long overdueTasks
) {
}
