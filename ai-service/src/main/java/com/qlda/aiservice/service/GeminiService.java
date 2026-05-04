package com.qlda.ai_service.service;

import com.qlda.ai_service.config.GeminiConfig;
import com.qlda.ai_service.exception.GeminiRateLimitException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;
    private Semaphore requestSemaphore;

    @org.springframework.beans.factory.annotation.Value("${gemini.api.max-concurrent-requests:1}")
    private int maxConcurrentRequests;

    @org.springframework.beans.factory.annotation.Value("${gemini.api.max-wait-for-permit-ms:2000}")
    private long maxWaitForPermitMs;

    @org.springframework.beans.factory.annotation.Value("${gemini.api.max-retries:2}")
    private int maxRetries;

    @org.springframework.beans.factory.annotation.Value("${gemini.api.initial-backoff-ms:1000}")
    private long initialBackoffMs;

    @org.springframework.beans.factory.annotation.Value("${gemini.api.max-backoff-ms:5000}")
    private long maxBackoffMs;

    // System prompt cho context hệ thống QLDA
    private static final String SYSTEM_CONTEXT = """
        Bạn là trợ lý AI của hệ thống Quản lý Dự án (QLDA). 
        Nhiệm vụ: hỗ trợ người dùng thao tác trong hệ thống như tải lên văn bản, 
        tìm kiếm, phê duyệt, theo dõi dự án.
        Trả lời ngắn gọn, rõ ràng bằng tiếng Việt. 
        Không trả lời các câu hỏi ngoài phạm vi hệ thống.
        """;

    public String chat(String userMessage) {
        String requestId = UUID.randomUUID().toString();
        String url = geminiConfig.buildApiUrl();
        long startedAt = System.nanoTime();

        if (!acquirePermit(requestId)) {
            throw new GeminiRateLimitException(
                    "Hệ thống đang xử lý quá nhiều yêu cầu AI cùng lúc. Vui lòng thử lại sau.",
                    maxWaitForPermitMs
            );
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "system_instruction", Map.of(
                            "parts", List.of(Map.of("text", SYSTEM_CONTEXT))
                    ),
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", userMessage)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "maxOutputTokens", 256,
                            "temperature", 0.7
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            long delayMs = initialBackoffMs;

            for (int attempt = 0; attempt <= maxRetries; attempt++) {
                try {
                    log.debug("Gemini request start requestId={} attempt={} url={} messageLength={}",
                            requestId, attempt + 1, url, userMessage.length());

                    ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
                    long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
                    log.debug("Gemini request success requestId={} status={} durationMs={}",
                            requestId, response.getStatusCode(), durationMs);
                    return extractText(response.getBody(), requestId);

                } catch (HttpClientErrorException.TooManyRequests e) {
                    long retryAfterMs = resolveRetryAfterMs(e, delayMs);
                    String errorBody = e.getResponseBodyAsString();
                    boolean isQuotaExceeded = errorBody.contains("Quota exceeded for metric") || errorBody.contains("limit: 0");
                    
                    log.warn("Gemini 429 requestId={} attempt={}/{} quotaExceeded={} retryAfterMs={} body={}",
                            requestId, attempt + 1, maxRetries + 1, isQuotaExceeded, retryAfterMs, safeBody(errorBody));

                    // Không retry nếu hết quota (sẽ vô ích)
                    if (isQuotaExceeded || attempt >= maxRetries) {
                        throw new GeminiRateLimitException(
                                "Gemini đang giới hạn request, vui lòng thử lại sau.",
                                retryAfterMs,
                                e
                        );
                    }
                    
                    // Retry nếu transient error và còn attempt
                    sleepWithJitter(Math.min(delayMs, maxBackoffMs));
                    delayMs = Math.min(delayMs * 2, maxBackoffMs);
                } catch (HttpClientErrorException e) {
                    log.error("Gemini client error requestId={} status={} body={}",
                            requestId, e.getStatusCode(), safeBody(e.getResponseBodyAsString()), e);
                    throw e;
                }
            }

            throw new RuntimeException("Max retries exceeded");
        } finally {
            requestSemaphore.release();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            log.debug("Gemini request finished requestId={} durationMs={}", requestId, durationMs);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> body, String requestId) {
        try {
            if (body == null) {
                throw new IllegalStateException("Empty response body");
            }
            var candidates = (List<Map<String, Object>>) body.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new IllegalStateException("Missing candidates");
            }
            var content = (Map<String, Object>) candidates.get(0).get("content");
            if (content == null) {
                throw new IllegalStateException("Missing content");
            }
            var parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                throw new IllegalStateException("Missing parts");
            }
            Object text = parts.get(0).get("text");
            return Objects.toString(text, "Xin lỗi, tôi không thể xử lý yêu cầu này lúc này.");
        } catch (Exception e) {
            log.error("Parse Gemini response error requestId={} bodyKeys={}",
                    requestId, body != null ? body.keySet() : null, e);
            return "Xin lỗi, tôi không thể xử lý yêu cầu này lúc này.";
        }
    }

    @PostConstruct
    void init() {
        requestSemaphore = new Semaphore(Math.max(1, maxConcurrentRequests), true);
    }

    private boolean acquirePermit(String requestId) {
        try {
            boolean acquired = requestSemaphore.tryAcquire(maxWaitForPermitMs, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.warn("Gemini queue full requestId={} waitMs={}", requestId, maxWaitForPermitMs);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private long resolveRetryAfterMs(HttpClientErrorException.TooManyRequests e, long fallbackMs) {
        try {
            String retryAfter = e.getResponseHeaders() != null ? e.getResponseHeaders().getFirst(HttpHeaders.RETRY_AFTER) : null;
            if (retryAfter == null || retryAfter.isBlank()) {
                return fallbackMs;
            }

            long retryAfterSeconds = Long.parseLong(retryAfter.trim());
            return TimeUnit.SECONDS.toMillis(retryAfterSeconds);
        } catch (Exception ignored) {
            return fallbackMs;
        }
    }

    private void sleepWithJitter(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String safeBody(String body) {
        if (body == null) {
            return null;
        }
        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
    }
}