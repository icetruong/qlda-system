package com.qlda.aiservice.service.chatbot;

public record IntentDetectionResult(
    ChatbotIntent intent,
    ChatbotMetricCode metricCode
) {
}
