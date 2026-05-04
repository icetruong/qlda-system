package com.qlda.workflowservice.client.dto;

import java.util.List;

public record ValidationResponse(
        boolean valid,
        List<String> invalidIds
) {
}
