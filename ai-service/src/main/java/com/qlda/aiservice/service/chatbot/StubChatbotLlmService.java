package com.qlda.aiservice.service.chatbot;

import com.qlda.aiservice.service.AiModelService;
import com.qlda.aiservice.service.ChatbotOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StubChatbotLlmService implements ChatbotLlmService {

    private final AiModelService aiModelService;

    @Override
    public ChatbotLlmResponse generateAnswer(String systemPrompt, String userPrompt, String question) {
        String context = systemPrompt + "\n\n" + userPrompt;
        ChatbotOutput output = aiModelService.answerWithContext(question, context);
        return new ChatbotLlmResponse(output.answer(), output.confidence(), output.modelUsed());
    }
}
