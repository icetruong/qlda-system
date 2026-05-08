package com.qlda.aiservice.dto.internal;

public record DocumentContentDto(
    Long documentId,
    String trichYeu,
    String noiDung,
    String ocrText,
    String language
) {
}

