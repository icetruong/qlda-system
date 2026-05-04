package com.qlda.ai_service.controller;

import com.qlda.ai_service.dto.ChatRequest;
import com.qlda.ai_service.dto.ChatResponse;
import com.qlda.ai_service.exception.GeminiRateLimitException;
import com.qlda.ai_service.service.GeminiService;
import com.qlda.ai_service.service.RateLimiterService;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final GeminiService geminiService;
    private final RateLimiterService rateLimiterService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest) {

        String requestId = UUID.randomUUID().toString();
        long startedAt = System.nanoTime();

        // Dùng sessionId nếu có, fallback về IP
        String clientId = request.getSessionId() != null
                ? request.getSessionId()
                : getClientIp(httpRequest);

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            log.warn("Empty chat message requestId={} clientId={}", requestId, clientId);
            return ResponseEntity.badRequest()
                    .body(new ChatResponse(
                            "Tin nhắn không được để trống.",
                            false,
                            "EMPTY_MESSAGE",
                            null
                    ));
        }

        // Kiểm tra rate limit
        ConsumptionProbe probe = rateLimiterService.tryConsume(clientId);
        if (!probe.isConsumed()) {
            long retryAfterMs = TimeUnit.NANOSECONDS.toMillis(
                probe.getNanosToWaitForRefill()
            );
            log.warn("Rate limit exceeded requestId={} clientId={} retryAfterMs={}",
                    requestId, clientId, retryAfterMs);
            return ResponseEntity.status(429)
                    .body(new ChatResponse(
                        "Bạn đang gửi quá nhiều tin nhắn. Vui lòng chờ " 
                            + (retryAfterMs / 1000) + " giây.",
                        false,
                        "RATE_LIMITED",
                        retryAfterMs
                    ));
        }

        try {
            String reply = geminiService.chat(request.getMessage());
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        log.info("Chat success requestId={} clientId={} durationMs={} replyLength={}",
            requestId, clientId, durationMs, reply != null ? reply.length() : 0);
            return ResponseEntity.ok(new ChatResponse(reply, true, null, null));

    } catch (GeminiRateLimitException e) {
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        log.warn("Gemini rate limited requestId={} clientId={} durationMs={} retryAfterMs={} message={}",
            requestId, clientId, durationMs, e.getRetryAfterMs(), e.getMessage());
        return ResponseEntity.status(429)
            .body(new ChatResponse(
                e.getMessage(),
                false,
                "RATE_LIMITED",
                e.getRetryAfterMs()
            ));

        } catch (Exception e) {
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        log.error("Gemini API error requestId={} clientId={} durationMs={}",
            requestId, clientId, durationMs, e);
            return ResponseEntity.status(503)
                    .body(new ChatResponse(
                        "Dịch vụ AI tạm thời không khả dụng. Vui lòng thử lại sau.",
                        false,
                        "API_ERROR",
                        5000L
                    ));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0] : request.getRemoteAddr();
    }
}