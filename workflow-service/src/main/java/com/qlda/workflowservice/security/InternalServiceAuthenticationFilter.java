package com.qlda.workflowservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.workflowservice.common.ApiResponse;
import com.qlda.workflowservice.config.InternalAuthProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION = "Authorization";
    private static final String SERVICE_NAME_HEADER = "X-Service-Name";
    private static final String INTERNAL_API_KEY_HEADER = "INTERNAL_API_KEY";
    private static final String BEARER_PREFIX = "Bearer ";

    private final InternalAuthProperties internalAuthProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(AUTHORIZATION);
        String internalApiKey = request.getHeader(INTERNAL_API_KEY_HEADER);
        String serviceName = request.getHeader(SERVICE_NAME_HEADER);

        boolean serviceTokenValid = isServiceTokenValid(authorization);
        boolean apiKeyValid = isApiKeyValid(internalApiKey);
        if (!serviceTokenValid && !apiKeyValid) {
            writeFailure(response, HttpStatus.UNAUTHORIZED, "Missing or invalid internal credentials", "FORBIDDEN");
            return;
        }

        if (!StringUtils.hasText(serviceName)) {
            writeFailure(response, HttpStatus.UNAUTHORIZED, "Missing service name header", "INVALID_REQUEST");
            return;
        }

        Set<String> allowedServices = Set.copyOf(internalAuthProperties.getAllowedServices());
        if (!allowedServices.contains(serviceName)) {
            writeFailure(response, HttpStatus.FORBIDDEN, "Service is not allowed", "FORBIDDEN");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isServiceTokenValid(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return false;
        }
        String token = authorization.substring(BEARER_PREFIX.length());
        return StringUtils.hasText(internalAuthProperties.getServiceToken())
                && internalAuthProperties.getServiceToken().equals(token);
    }

    private boolean isApiKeyValid(String internalApiKey) {
        return StringUtils.hasText(internalAuthProperties.getApiKey())
                && StringUtils.hasText(internalApiKey)
                && internalAuthProperties.getApiKey().equals(internalApiKey);
    }

    private void writeFailure(HttpServletResponse response, HttpStatus status, String message, String errorCode) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> payload = ApiResponse.fail(message, errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(payload));
    }
}
