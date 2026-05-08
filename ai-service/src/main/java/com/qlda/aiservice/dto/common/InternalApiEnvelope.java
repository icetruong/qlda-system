package com.qlda.aiservice.dto.common;

public record InternalApiEnvelope<T>(
    boolean success,
    String message,
    T data
) {
}

