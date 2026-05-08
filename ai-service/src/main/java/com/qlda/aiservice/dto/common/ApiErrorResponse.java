package com.qlda.aiservice.dto.common;

public record ApiErrorResponse(
    boolean success,
    String message,
    String errorCode
) {
    public static ApiErrorResponse of(String message, String errorCode) {
        return new ApiErrorResponse(false, message, errorCode);
    }
}

