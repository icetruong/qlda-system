package com.qlda.workflowservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityUtils {

    public Long getCurrentUserId() {
        Jwt jwt = getCurrentJwt();
        Object userId = jwt.getClaims().get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        if (userId instanceof String text && !text.isBlank()) {
            return Long.valueOf(text);
        }
        return null;
    }

    public String getCurrentUsername() {
        Jwt jwt = getCurrentJwt();
        return jwt.getClaimAsString("username");
    }

    public List<String> getCurrentRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return List.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    private Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("No JWT principal in security context");
        }
        return jwt;
    }
}
