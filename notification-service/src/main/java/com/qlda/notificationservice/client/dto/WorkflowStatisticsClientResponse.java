package com.qlda.notificationservice.client.dto;

public record WorkflowStatisticsClientResponse(
    Integer totalTasks,
    Integer completedTasks,
    Integer processingTasks,
    Integer overdueTasks
) {
}
