package com.qlda.aiservice.service.chatbot;

public interface ChatbotLlmService {
    ChatbotLlmResponse generateAnswer(String systemPrompt, String userPrompt, String question);
}
