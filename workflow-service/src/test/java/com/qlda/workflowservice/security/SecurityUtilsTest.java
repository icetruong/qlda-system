package com.qlda.workflowservice.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    private final SecurityUtils securityUtils = new SecurityUtils();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReadCurrentUserClaims() {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("alg", "RS256"),
                Map.of("userId", 101L, "username", "alice"));
        var auth = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(
                jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(101L, securityUtils.getCurrentUserId());
        assertEquals("alice", securityUtils.getCurrentUsername());
        assertEquals(List.of("ROLE_ADMIN"), securityUtils.getCurrentRoles());
    }

    @Test
    void getCurrentUserId_shouldParseStringUserId() {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("alg", "RS256"),
                Map.of("userId", "200", "username", "bob"));
        var auth = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(200L, securityUtils.getCurrentUserId());
    }

    @Test
    void shouldThrowWhenNoJwtPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("plain-user", "N/A"));

        assertThrows(IllegalStateException.class, securityUtils::getCurrentUsername);
    }
}
