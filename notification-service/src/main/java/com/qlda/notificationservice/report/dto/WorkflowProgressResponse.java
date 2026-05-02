package com.qlda.notificationservice.report.dto;

import java.util.List;

public record WorkflowProgressResponse(
    int totalTasks,
    int completedTasks,
    int processingTasks,
    int overdueTasks,
    List<WorkflowProgressItem> items
) {
}

