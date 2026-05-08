package com.qlda.aiservice.service;

public record ClassificationOutput(
    String category,
    String categoryName,
    double confidence,
    String reason,
    String modelUsed
) {
}

