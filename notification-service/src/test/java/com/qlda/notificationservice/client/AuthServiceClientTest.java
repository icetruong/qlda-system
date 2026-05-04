package com.qlda.notificationservice.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceClientTest {

    private MockWebServer mockWebServer;
    private AuthServiceClient authServiceClient;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        authServiceClient = new AuthServiceClient(
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
    void shouldSendInternalHeadersAndParseUser() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "id":2,
                  "username":"nva",
                  "hoTen":"Nguyen Van A",
                  "email":"nva@company.com",
                  "donViId":1,
                  "tenDonVi":"Phong Hanh chinh"
                }
                """));

        var response = authServiceClient.getUserById(2L);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer internal-token");
        assertThat(request.getHeader("X-Service-Name")).isEqualTo("notification-service");
        assertThat(request.getPath()).contains("/internal/auth/users/2");
        assertThat(response.hoTen()).isEqualTo("Nguyen Van A");
    }
}
