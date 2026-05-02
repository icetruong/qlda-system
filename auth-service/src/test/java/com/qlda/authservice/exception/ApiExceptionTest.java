package com.qlda.authservice.exception;

import com.qlda.authservice.common.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionTest {

    @Test
    void shouldExposeStatusAndErrorCode() {
        ApiException exception = new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Unauthorized");
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        assertEquals("Unauthorized", exception.getMessage());
    }
}
