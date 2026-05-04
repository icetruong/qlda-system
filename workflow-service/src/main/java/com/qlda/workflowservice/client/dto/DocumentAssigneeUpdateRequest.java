package com.qlda.workflowservice.client.dto;

public record DocumentAssigneeUpdateRequest(
        Long nguoiXuLyId,
        Integer donViXuLyId
) {
}
