package com.qlda.aiservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.aiservice.dto.request.ChatbotAskRequest;
import com.qlda.aiservice.exception.AppException;
import com.qlda.aiservice.exception.ErrorCode;
import com.qlda.aiservice.exception.GlobalExceptionHandler;
import com.qlda.aiservice.service.chatbot.ChatbotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatbotControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = mock(ChatbotService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatbotController(chatbotService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void shouldReturn200WhenRequestValid() throws Exception {
        ChatbotAskRequest request = new ChatbotAskRequest(
            2L,
            "Tìm tài liệu về đào tạo người dùng",
            Map.of("module", "DOCUMENT", "documentId", 1L)
        );
        when(chatbotService.ask(any())).thenReturn(Map.of("resultId", 1L, "intent", "DOCUMENT_SEARCH", "answer", "ok"));

        mockMvc.perform(post("/api/ai/chatbot/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Chatbot response successfully"))
            .andExpect(jsonPath("$.data.resultId").value(1));
    }

    @Test
    void shouldReturnValidationErrorWhenQuestionBlank() throws Exception {
        ChatbotAskRequest request = new ChatbotAskRequest(2L, " ", Map.of("module", "DOCUMENT"));

        mockMvc.perform(post("/api/ai/chatbot/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("AI_PROCESSING_FAILED"));
    }

    @Test
    void shouldReturnValidationErrorWhenUserIdNull() throws Exception {
        ChatbotAskRequest request = new ChatbotAskRequest(null, "Tìm tài liệu", Map.of("module", "DOCUMENT"));

        mockMvc.perform(post("/api/ai/chatbot/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("AI_PROCESSING_FAILED"));
    }

    @Test
    void shouldReturnChatbotFailedWhenServiceThrows() throws Exception {
        ChatbotAskRequest request = new ChatbotAskRequest(2L, "Tìm tài liệu", Map.of("module", "DOCUMENT"));
        when(chatbotService.ask(any()))
            .thenThrow(new AppException(ErrorCode.CHATBOT_FAILED, HttpStatus.INTERNAL_SERVER_ERROR, "chatbot failed"));

        mockMvc.perform(post("/api/ai/chatbot/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("CHATBOT_FAILED"));
    }
}
