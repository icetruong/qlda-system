package com.qlda.workflowservice.security;

import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStatisticsResponse;
import com.qlda.workflowservice.service.InternalWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "internal.auth.service-token=test-token",
        "internal.auth.api-key=test-api-key",
        "internal.auth.allowed-services[0]=document-service"
})
class InternalWorkflowSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InternalWorkflowService internalWorkflowService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void missingHeaders_shouldBeRejected() throws Exception {
        mockMvc.perform(get("/internal/workflows/statistics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidToken_shouldBeRejected() throws Exception {
        mockMvc.perform(get("/internal/workflows/statistics")
                        .header("Authorization", "Bearer wrong-token")
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidServiceName_shouldBeRejected() throws Exception {
        mockMvc.perform(get("/internal/workflows/statistics")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Service-Name", "unknown-service"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validHeaders_shouldAllowRequest() throws Exception {
        when(internalWorkflowService.getStatistics(any(), any(), any()))
                .thenReturn(new InternalWorkflowStatisticsResponse(10, 5, 3, 2));

        mockMvc.perform(get("/internal/workflows/statistics")
                        .header("Authorization", "Bearer test-token")
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isOk());
    }

    @Test
    void validApiKey_shouldAllowRequest() throws Exception {
        when(internalWorkflowService.getStatistics(any(), any(), any()))
                .thenReturn(new InternalWorkflowStatisticsResponse(10, 5, 3, 2));

        mockMvc.perform(get("/internal/workflows/statistics")
                        .header("INTERNAL_API_KEY", "test-api-key")
                        .header("X-Service-Name", "document-service"))
                .andExpect(status().isOk());
    }
}
