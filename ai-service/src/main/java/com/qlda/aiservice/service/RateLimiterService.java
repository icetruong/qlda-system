package com.qlda.ai_service.service;

import io.github.bucket4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    // Mỗi user/IP có 1 bucket riêng
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${rate-limit.requests-per-minute:10}")
    private int requestsPerMinute;

    /**
     * Kiểm tra và consume 1 token.
     * @return ConsumptionProbe chứa thông tin còn token không và chờ bao lâu
     */
    public ConsumptionProbe tryConsume(String clientId) {
        Bucket bucket = buckets.computeIfAbsent(clientId, this::createBucket);
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    private Bucket createBucket(String clientId) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(requestsPerMinute)
                        .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                        .initialTokens(requestsPerMinute)
                        .build())
                // Burst nhỏ: tối đa 3 request/10s để tránh spam
                .addLimit(Bandwidth.builder()
                        .capacity(3)
                        .refillGreedy(3, Duration.ofSeconds(10))
                        .initialTokens(3)
                        .build())
                .build();
    }
}