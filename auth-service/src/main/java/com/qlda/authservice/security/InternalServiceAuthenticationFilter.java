package com.qlda.authservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.authservice.common.ApiResponse;
import com.qlda.authservice.common.ErrorCode;
import com.qlda.authservice.config.InternalAuthProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_PREFIX = "/internal/";
    private static final String SERVICE_NAME_HEADER = "X-Service-Name";

    private final InternalAuthProperties internalAuthProperties;
    private final ObjectMapper objectMapper;

    public InternalServiceAuthenticationFilter(InternalAuthProperties internalAuthProperties, ObjectMapper objectMapper) {
        this.internalAuthProperties = internalAuthProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isInternalRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (!StringUtils.hasText(token) || !token.equals(internalAuthProperties.getToken())) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid internal token", ErrorCode.INVALID_INTERNAL_TOKEN);
            return;
        }

        String serviceName = request.getHeader(SERVICE_NAME_HEADER);
        if (!isAllowedService(serviceName)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Invalid service name", ErrorCode.INVALID_SERVICE_NAME);
            return;
        }

        setAuthentication(request, serviceName.trim());
        filterChain.doFilter(request, response);
    }

    private boolean isInternalRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith(INTERNAL_PREFIX);
    }

    private String extractBearerToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7).trim();
    }

    private boolean isAllowedService(String serviceName) {
        if (!StringUtils.hasText(serviceName)) {
            return false;
        }
        Set<String> allowedServices = internalAuthProperties.getAllowedServices().stream()
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        if (allowedServices.isEmpty()) {
            return false;
        }
        return allowedServices.contains(serviceName.trim().toLowerCase(Locale.ROOT));
    }

    private void setAuthentication(HttpServletRequest request, String serviceName) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                serviceName,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void writeError(HttpServletResponse response, int status, String message, ErrorCode errorCode) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Object> body = ApiResponse.failure(message, errorCode.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
