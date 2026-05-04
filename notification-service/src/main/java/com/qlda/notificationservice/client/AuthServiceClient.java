package com.qlda.notificationservice.client;

import com.qlda.notificationservice.client.dto.AuthUnitResponse;
import com.qlda.notificationservice.client.dto.AuthUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AuthServiceClient {

    private final WebClient webClient;

    public AuthServiceClient(
        WebClient.Builder webClientBuilder,
        @Value("${services.auth-service.base-url:http://localhost:8081}") String baseUrl,
        @Value("${internal.auth.service-name:notification-service}") String serviceName,
        @Value("${internal.auth.service-token:change-me-in-dev}") String serviceToken
    ) {
        this.webClient = webClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken)
            .defaultHeader("X-Service-Name", serviceName)
            .build();
    }

    public AuthUserResponse getUserById(Long id) {
        return webClient.get()
            .uri("/internal/auth/users/{id}", id)
            .retrieve()
            .bodyToMono(AuthUserResponse.class)
            .block();
    }

    public AuthUnitResponse getUnitById(Integer id) {
        return webClient.get()
            .uri("/internal/auth/units/{id}", id)
            .retrieve()
            .bodyToMono(AuthUnitResponse.class)
            .block();
    }
}
