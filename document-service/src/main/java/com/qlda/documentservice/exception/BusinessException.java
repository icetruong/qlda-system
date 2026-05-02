package com.qlda.documentservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final HttpStatus status;

    public BusinessException(ErrorCode errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public static BusinessException notFound(ErrorCode errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.NOT_FOUND);
    }

    public static BusinessException badRequest(ErrorCode errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException conflict(ErrorCode errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.CONFLICT);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(ErrorCode.FORBIDDEN, message, HttpStatus.FORBIDDEN);
    }
}

