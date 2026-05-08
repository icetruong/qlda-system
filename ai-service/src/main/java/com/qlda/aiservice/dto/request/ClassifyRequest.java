package com.qlda.aiservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ClassifyRequest(
    @NotNull Long documentId,
    @NotNull Long userId,
    @NotBlank String text,
    @NotEmpty List<String> categories,
    String language
) {
}

