package com.qlda.aiservice.controller;

import com.qlda.aiservice.service.AiApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRequireAuthenticationForAiEndpoints() throws Exception {
        mockMvc.perform(get("/api/ai/results/1"))
            .andExpect(status().isUnauthorized());
    }
}
