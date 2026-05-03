package com.ice.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth-service", route -> route
                .path("/api/auth/**")
                .uri("lb://auth-service")
            )
            .route("document-service", route -> route
                .path("/api/documents/**")
                .uri("lb://document-service")
            )
            .route("workflow-service", route -> route
                .path("/api/workflows/**")
                .uri("lb://workflow-service")
            )
            .route("support-service", route -> route
                .path("/api/notifications/**", "/api/audit-logs/**", "/api/reports/**")
                .uri("lb://support-service")
            )
            .build();
    }
}
