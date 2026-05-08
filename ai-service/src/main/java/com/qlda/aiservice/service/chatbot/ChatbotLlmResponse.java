package com.qlda.aiservice.service.chatbot;

public record ChatbotLlmResponse(
    String answer,
    double confidence,
    String modelUsed
) {
}
