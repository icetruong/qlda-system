package com.qlda.aiservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ChatbotAskRequest(
    @NotNull Long userId,
    @NotBlank String question,
    Map<String, Object> context
) {
}

