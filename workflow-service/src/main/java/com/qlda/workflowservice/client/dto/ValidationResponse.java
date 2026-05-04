package com.qlda.workflowservice.client.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public record ValidationResponse(
        boolean valid,
        @JsonAlias({"invalidIds", "invalidUserIds", "invalidUnitIds"})
        List<String> invalidIds
) {
}
