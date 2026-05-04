package com.qlda.documentservice.security;

import com.qlda.documentservice.config.InternalAuthProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final InternalAuthProperties internalAuthProperties;

    public InternalServiceAuthenticationFilter(InternalAuthProperties internalAuthProperties) {
        this.internalAuthProperties = internalAuthProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String serviceName = request.getHeader("X-Service-Name");

        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (!internalAuthProperties.getServiceToken().equals(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid internal token");
            return;
        }

        if (!StringUtils.hasText(serviceName)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing X-Service-Name header");
            return;
        }

        if (!internalAuthProperties.getAllowedServices().contains(serviceName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Service is not allowed");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
