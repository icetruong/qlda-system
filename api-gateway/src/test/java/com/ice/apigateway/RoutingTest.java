package com.ice.apigateway;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(properties = {
    "eureka.client.enabled=false",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false"
})
class RoutingTest {

    @Autowired
    private ApplicationContext context;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToApplicationContext(context)
            .apply(springSecurity())
            .configureClient()
            .build();
    }

    @Test
    void authRouteShouldMapToAuthService() {
        webTestClient.get()
            .uri("/api/auth/login")
            .exchange()
            .expectStatus()
            .is5xxServerError();
    }

    @Test
    void documentRouteShouldMapToDocumentService() {
        webTestClient.mutateWith(mockJwt())
            .get()
            .uri("/api/documents/1")
            .exchange()
            .expectStatus()
            .is5xxServerError();
    }

    @Test
    void workflowRouteShouldMapToWorkflowService() {
        webTestClient.mutateWith(mockJwt())
            .get()
            .uri("/api/workflows/1")
            .exchange()
            .expectStatus()
            .is5xxServerError();
    }
}
