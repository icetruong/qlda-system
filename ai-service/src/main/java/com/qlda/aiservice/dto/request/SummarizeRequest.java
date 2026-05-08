package com.qlda.aiservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SummarizeRequest(
    @NotNull Long documentId,
    @NotNull Long userId,
    @NotBlank String text,
    @NotBlank String summaryType,
    String language
) {
}

