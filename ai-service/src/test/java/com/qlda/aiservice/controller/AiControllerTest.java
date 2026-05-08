package com.qlda.aiservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.aiservice.dto.request.SummarizeRequest;
import com.qlda.aiservice.exception.AppException;
import com.qlda.aiservice.exception.ErrorCode;
import com.qlda.aiservice.exception.GlobalExceptionHandler;
import com.qlda.aiservice.service.AiApplicationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private AiApplicationService aiApplicationService;

    @BeforeEach
    void setUp() {
        aiApplicationService = mock(AiApplicationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AiController(aiApplicationService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void shouldReturn400WhenSummarizeRequestInvalid() throws Exception {
        SummarizeRequest request = new SummarizeRequest(1L, 2L, "", "SHORT", "vi");

        mockMvc.perform(post("/api/ai/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("AI_PROCESSING_FAILED"));
    }

    @Test
    void shouldReturnSuccessEnvelopeForSummarize() throws Exception {
        SummarizeRequest request = new SummarizeRequest(1L, 2L, "Noi dung", "SHORT", "vi");
        when(aiApplicationService.summarize(any())).thenReturn(Map.of("resultId", 1L, "summary", "abc"));

        mockMvc.perform(post("/api/ai/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.resultId").value(1));
    }

    @Test
    void shouldReturnErrorEnvelopeWhenServiceThrows() throws Exception {
        SummarizeRequest request = new SummarizeRequest(1L, 2L, "Noi dung", "SHORT", "vi");
        when(aiApplicationService.summarize(any()))
            .thenThrow(new AppException(ErrorCode.SUMMARY_FAILED, HttpStatus.INTERNAL_SERVER_ERROR, "Summary failed"));

        mockMvc.perform(post("/api/ai/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("SUMMARY_FAILED"));
    }

    @Test
    void shouldReturn400WhenProcessTypeInvalid() throws Exception {
        when(aiApplicationService.getResultsByDocument(1L, "INVALID_TYPE", 0, 10))
            .thenThrow(new IllegalArgumentException("No enum constant"));

        mockMvc.perform(get("/api/ai/results/documents/1")
                .param("loaiXuLyAI", "INVALID_TYPE")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}
