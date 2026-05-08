package com.qlda.aiservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SuggestionHandlingRequest(
    @NotNull Long documentId,
    @NotNull Long userId,
    @NotBlank String text,
    Map<String, Object> context
) {
}

