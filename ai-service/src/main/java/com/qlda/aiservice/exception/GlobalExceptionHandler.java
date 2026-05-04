package com.qlda.ai_service.exception;

import com.qlda.ai_service.dto.ChatResponse;
import com.qlda.ai_service.exception.GeminiRateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChatResponse> handleAll(Exception e) {
        log.error("Unhandled error", e);
        return ResponseEntity.status(500)
                .body(new ChatResponse(
                    "Lỗi hệ thống. Vui lòng liên hệ quản trị viên.",
                    false, "SERVER_ERROR", null
                ));
    }

    @ExceptionHandler(GeminiRateLimitException.class)
    public ResponseEntity<ChatResponse> handleGeminiRateLimit(GeminiRateLimitException e) {
        log.warn("Gemini rate limit handled globally retryAfterMs={} message={}", e.getRetryAfterMs(), e.getMessage());
        return ResponseEntity.status(429)
                .body(new ChatResponse(
                        e.getMessage(),
                        false,
                        "RATE_LIMITED",
                        e.getRetryAfterMs()
                ));
    }
}