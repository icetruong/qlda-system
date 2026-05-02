package com.qlda.workflowservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Map;

@SpringBootTest
class WorkflowServiceApplicationTests {

    @TestConfiguration
    static class JwtTestConfig {
        @Bean
        JwtDecoder jwtDecoder() {
            return token -> new Jwt(
                    token,
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "RS256"),
                    Map.of("userId", 1L, "username", "test-user", "roles", java.util.List.of("ADMIN"))
            );
        }
    }

    @Test
    void contextLoads() {
    }

}
