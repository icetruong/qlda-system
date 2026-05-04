package com.qlda.documentservice.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.qlda.documentservice.client.dto.NotificationClientDtos;
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
class NotificationServiceClientTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private NotificationServiceClient notificationServiceClient;

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
        registry.add("services.notification-service.base-url", () -> mockWebServer.url("/").toString());
        registry.add("internal.auth.service-name", () -> "document-service");
        registry.add("internal.auth.service-token", () -> "internal-token");
    }

    @Test
    void sendNotification_shouldSendInternalHeaders_andParseResponse() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody("""
                {"notificationId":1,"nguoiNhanId":2,"sentChannels":["SYSTEM","EMAIL"]}
                """));

        NotificationClientDtos.SendNotificationResponse response = notificationServiceClient.send(
            new NotificationClientDtos.SendNotificationRequest(2L, "Tieu de", "Noi dung", "SYSTEM")
        );

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/internal/notifications/send");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer internal-token");
        assertThat(request.getHeader("X-Service-Name")).isEqualTo("document-service");
        assertThat(response.notificationId()).isEqualTo(1L);
        assertThat(response.nguoiNhanId()).isEqualTo(2L);
    }
}
