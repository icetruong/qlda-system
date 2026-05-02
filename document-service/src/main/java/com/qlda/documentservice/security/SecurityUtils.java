package com.qlda.documentservice.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public Optional<Long> getCurrentUserId() {
        return getJwt().map(jwt -> {
            Object value = jwt.getClaims().get("userId");
            if (value == null) {
                return null;
            }
            return Long.valueOf(value.toString());
        });
    }

    public Optional<String> getCurrentUsername() {
        return getJwt().map(jwt -> {
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            if (preferredUsername != null && !preferredUsername.isBlank()) {
                return preferredUsername;
            }
            String username = jwt.getClaimAsString("username");
            if (username != null && !username.isBlank()) {
                return username;
            }
            return jwt.getSubject();
        });
    }

    private Optional<Jwt> getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return Optional.of(token.getToken());
        }
        return Optional.empty();
    }
}

