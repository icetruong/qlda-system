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
class GatewaySecurityTest {

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
    void requestWithoutTokenShouldReturn401() {
        webTestClient.get()
            .uri("/internal/protected")
            .exchange()
            .expectStatus()
            .isUnauthorized();
    }

    @Test
    void requestWithTokenShouldPassAuthentication() {
        webTestClient
            .mutateWith(mockJwt().jwt(jwt -> jwt.claim("roles", "USER")))
            .get()
            .uri("/internal/protected")
            .exchange()
            .expectStatus()
            .isNotFound();
    }
}
