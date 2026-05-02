package com.qlda.notificationservice.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    NOTIFICATION_NOT_FOUND("NOTIFICATION_NOT_FOUND", "Khong tim thay thong bao", HttpStatus.NOT_FOUND),
    AUDIT_LOG_NOT_FOUND("AUDIT_LOG_NOT_FOUND", "Khong tim thay lich su he thong", HttpStatus.NOT_FOUND),
    INVALID_REQUEST("INVALID_REQUEST", "Du lieu yeu cau khong hop le", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Loi he thong", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

