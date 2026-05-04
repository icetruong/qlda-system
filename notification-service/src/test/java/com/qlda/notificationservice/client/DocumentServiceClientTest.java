package com.qlda.notificationservice.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentServiceClientTest {

    private MockWebServer mockWebServer;
    private DocumentServiceClient documentServiceClient;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        documentServiceClient = new DocumentServiceClient(
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
    void shouldSendInternalHeadersAndParseDocumentStatistics() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "totalDocuments":120,
                  "incomingDocuments":70,
                  "outgoingDocuments":50,
                  "items":[{"label":"Dang xu ly","value":30}]
                }
                """));

        var response = documentServiceClient.getDocumentStatistics("2026-04-01", "2026-04-30", 1L, "status");

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer internal-token");
        assertThat(request.getHeader("X-Service-Name")).isEqualTo("notification-service");
        assertThat(request.getPath()).contains("/internal/documents/statistics");
        assertThat(response.totalDocuments()).isEqualTo(120);
    }
}
