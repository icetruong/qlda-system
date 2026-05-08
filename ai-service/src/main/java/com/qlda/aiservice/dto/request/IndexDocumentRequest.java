package com.qlda.aiservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record IndexDocumentRequest(
    @NotNull Long documentId,
    Long attachmentId,
    @NotBlank String text,
    Map<String, Object> metadata
) {
}

