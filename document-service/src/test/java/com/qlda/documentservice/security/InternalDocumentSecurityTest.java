package com.qlda.documentservice.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "internal.auth.service-token=internal-token",
    "internal.auth.allowed-services[0]=workflow-service",
    "internal.auth.allowed-services[1]=ai-service"
})
class InternalDocumentSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReject_whenMissingInternalHeaders() throws Exception {
        mockMvc.perform(get("/internal/documents/statistics"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReject_whenHeaderTokenInvalid() throws Exception {
        mockMvc.perform(get("/internal/documents/statistics")
                .header("Authorization", "Bearer wrong-token")
                .header("X-Service-Name", "workflow-service"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReject_whenServiceNameNotAllowed() throws Exception {
        mockMvc.perform(get("/internal/documents/statistics")
                .header("Authorization", "Bearer internal-token")
                .header("X-Service-Name", "unknown-service"))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldPassFilter_whenHeadersAreValid() throws Exception {
        mockMvc.perform(get("/internal/documents/statistics")
                .header("Authorization", "Bearer internal-token")
                .header("X-Service-Name", "workflow-service"))
            .andExpect(status().isOk());
    }
}
