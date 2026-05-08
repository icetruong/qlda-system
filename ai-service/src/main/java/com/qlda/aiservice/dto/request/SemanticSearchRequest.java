package com.qlda.aiservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SemanticSearchRequest(
    @NotBlank String keyword,
    @NotNull Long userId,
    Map<String, Object> filters,
    Integer page,
    Integer size
) {
}

