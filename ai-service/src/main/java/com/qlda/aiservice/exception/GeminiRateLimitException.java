package com.qlda.ai_service.exception;

public class GeminiRateLimitException extends RuntimeException {

    private final long retryAfterMs;

    public GeminiRateLimitException(String message, long retryAfterMs) {
        super(message);
        this.retryAfterMs = retryAfterMs;
    }

    public GeminiRateLimitException(String message, long retryAfterMs, Throwable cause) {
        super(message, cause);
        this.retryAfterMs = retryAfterMs;
    }

    public long getRetryAfterMs() {
        return retryAfterMs;
    }
}