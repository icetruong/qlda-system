package com.qlda.aiservice.controller;

import com.qlda.aiservice.dto.common.ApiSuccessResponse;
import com.qlda.aiservice.dto.request.ChatbotAskRequest;
import com.qlda.aiservice.service.chatbot.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<ApiSuccessResponse<Map<String, Object>>> ask(@Valid @RequestBody ChatbotAskRequest request) {
        return ResponseEntity.ok(ApiSuccessResponse.of("Chatbot response successfully", chatbotService.ask(request)));
    }
}
