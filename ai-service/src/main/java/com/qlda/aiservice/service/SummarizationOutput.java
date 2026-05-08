package com.qlda.aiservice.service;

public record SummarizationOutput(
    String summary,
    double confidence,
    String modelUsed
) {
}

