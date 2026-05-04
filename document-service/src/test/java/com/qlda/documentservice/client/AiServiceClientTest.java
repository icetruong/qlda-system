package com.qlda.documentservice.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.qlda.documentservice.client.dto.AiClientDtos;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class AiServiceClientTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private AiServiceClient aiServiceClient;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("services.ai-service.base-url", () -> mockWebServer.url("/").toString());
        registry.add("internal.auth.service-name", () -> "document-service");
        registry.add("internal.auth.service-token", () -> "internal-token");
    }

    @Test
    void summarize_shouldSendInternalHeaders_andParseResponse() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {"documentId":12,"summaryType":"SHORT","summary":"Tom tat","confidence":91.5}
                """));

        AiClientDtos.SummarizeResponse response = aiServiceClient.summarize(
            new AiClientDtos.SummarizeRequest(12L, "Noi dung", "SHORT")
        );

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/ai/summarize");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer internal-token");
        assertThat(request.getHeader("X-Service-Name")).isEqualTo("document-service");
        assertThat(response.documentId()).isEqualTo(12L);
        assertThat(response.summary()).isEqualTo("Tom tat");
    }
}
