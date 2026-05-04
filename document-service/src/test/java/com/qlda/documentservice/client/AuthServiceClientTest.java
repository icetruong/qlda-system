package com.qlda.documentservice.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.qlda.documentservice.client.dto.AuthClientDtos;
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
class AuthServiceClientTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private AuthServiceClient authServiceClient;

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
        registry.add("services.auth-service.base-url", () -> mockWebServer.url("/").toString());
        registry.add("internal.auth.service-name", () -> "document-service");
        registry.add("internal.auth.service-token", () -> "internal-token");
    }

    @Test
    void getUserById_shouldSendInternalHeaders_andParseResponse() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {"id":1,"username":"nva","hoTen":"Nguyen Van A","email":"nva@company.com","donViId":1}
                """));

        AuthClientDtos.UserInfoResponse response = authServiceClient.getUserById(1L);

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/auth/users/1");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer internal-token");
        assertThat(request.getHeader("X-Service-Name")).isEqualTo("document-service");
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("nva");
    }
}
