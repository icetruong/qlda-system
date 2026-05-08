package com.qlda.aiservice.service;

public record ChatbotOutput(
    String answer,
    double confidence,
    String modelUsed
) {
}

