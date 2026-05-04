package com.qlda.notificationservice.client.dto;

import java.util.List;

public record WorkflowProgressClientResponse(
    Integer totalTasks,
    Integer completedTasks,
    Integer processingTasks,
    List<WorkflowProgressClientItem> items
) {
}
