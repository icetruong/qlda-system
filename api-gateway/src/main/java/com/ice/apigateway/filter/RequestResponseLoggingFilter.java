package com.ice.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        LOGGER.info(
            "Incoming request - method: {}, path: {}, headers: {}",
            exchange.getRequest().getMethod(),
            exchange.getRequest().getPath().value(),
            exchange.getRequest().getHeaders()
        );

        return chain.filter(exchange).doFinally(signalType -> {
            HttpStatusCode status = exchange.getResponse().getStatusCode();
            LOGGER.info("Outgoing response - status: {}", status);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
