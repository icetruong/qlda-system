package com.qlda.notificationservice.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class WorkflowServiceClientTest {

    private MockWebServer mockWebServer;
    private WorkflowServiceClient workflowServiceClient;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        workflowServiceClient = new WorkflowServiceClient(
            WebClient.builder(),
            mockWebServer.url("/").toString(),
            "notification-service",
            "internal-token"
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void shouldSendInternalHeadersAndParseWorkflowProgress() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "totalTasks":50,
                  "completedTasks":35,
                  "processingTasks":10,
                  "items":[
                    {
                      "documentId":1,
                      "processingId":20,
                      "nguoiXuLyId":2,
                      "trangThaiXuLy":1,
                      "tyLeHoanThanh":60,
                      "hanXuLy":"2026-05-02T17:00:00"
                    }
                  ]
                }
                """));

        var response = workflowServiceClient.getWorkflowProgress("2026-04-01", "2026-04-30", 1L, 2L);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer internal-token");
        assertThat(request.getHeader("X-Service-Name")).isEqualTo("notification-service");
        assertThat(request.getPath()).contains("/internal/workflows/progress");
        assertThat(response.totalTasks()).isEqualTo(50);
    }
}
