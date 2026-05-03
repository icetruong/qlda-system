package com.ice.apigateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import com.ice.apigateway.filter.JwtForwardingFilter;
import com.ice.apigateway.filter.RequestResponseLoggingFilter;

import reactor.core.publisher.Mono;

@ExtendWith(OutputCaptureExtension.class)
class FilterTest {

    private final JwtForwardingFilter jwtForwardingFilter = new JwtForwardingFilter();
    private final RequestResponseLoggingFilter requestResponseLoggingFilter = new RequestResponseLoggingFilter();

    @Test
    void loggingFilterShouldLogRequestAndResponse(CapturedOutput output) {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/auth/login").build()
        );

        GatewayFilterChain chain = serverWebExchange -> {
            serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
            return serverWebExchange.getResponse().setComplete();
        };

        requestResponseLoggingFilter.filter(exchange, chain).block();

        assertThat(output.getOut()).contains("Incoming request - method");
        assertThat(output.getOut()).contains("Outgoing response - status");
    }

    @Test
    void authorizationHeaderShouldBeForwarded() {
        String bearerToken = "Bearer forwarded-token";
        AtomicReference<String> forwardedAuthorization = new AtomicReference<>();

        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/documents/1")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .build()
        );

        GatewayFilterChain chain = serverWebExchange -> {
            forwardedAuthorization.set(serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
            return Mono.empty();
        };

        jwtForwardingFilter.filter(exchange, chain).block();

        assertThat(forwardedAuthorization.get()).isEqualTo(bearerToken);
    }
}
