package com.qlda.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.authservice.common.ApiResponse;
import com.qlda.authservice.common.ErrorCode;
import com.qlda.authservice.security.InternalServiceAuthenticationFilter;
import com.qlda.authservice.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final InternalServiceAuthenticationFilter internalServiceAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            InternalServiceAuthenticationFilter internalServiceAuthenticationFilter,
            ObjectMapper objectMapper
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.internalServiceAuthenticationFilter = internalServiceAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login/azure").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Unauthorized", ErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                                        "Forbidden", ErrorCode.FORBIDDEN))
                )
                .addFilterBefore(internalServiceAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, InternalServiceAuthenticationFilter.class);
        return http.build();
    }

    private void writeError(HttpServletResponse response, int status, String message, ErrorCode errorCode) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Object> body = ApiResponse.failure(message, errorCode.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
