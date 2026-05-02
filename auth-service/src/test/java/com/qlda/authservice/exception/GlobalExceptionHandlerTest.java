package com.qlda.authservice.exception;

import com.qlda.authservice.common.ApiResponse;
import com.qlda.authservice.common.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApiExceptionShouldReturnMappedStatusAndBody() {
        ApiException exception = new ApiException(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_EMAIL, "Email already exists");

        ResponseEntity<ApiResponse<Object>> response = handler.handleApiException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(false, response.getBody().success());
        assertEquals("Email already exists", response.getBody().message());
        assertEquals("DUPLICATE_EMAIL", response.getBody().errorCode());
    }

    @Test
    void handleMethodArgumentNotValidShouldReturnFormattedMessage() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "username", "username is required"));
        bindingResult.addError(new FieldError("request", "password", "password is required"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                mock(MethodParameter.class),
                bindingResult
        );

        ResponseEntity<ApiResponse<Object>> response = handler.handleMethodArgumentNotValid(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_REQUEST", response.getBody().errorCode());
        assertTrue(response.getBody().message().contains("username: username is required"));
        assertTrue(response.getBody().message().contains("password: password is required"));
    }

    @Test
    void handleConstraintViolationShouldReturnBadRequest() {
        ConstraintViolationException exception = new ConstraintViolationException("invalid params", null);

        ResponseEntity<ApiResponse<Object>> response = handler.handleConstraintViolation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_REQUEST", response.getBody().errorCode());
        assertEquals("invalid params", response.getBody().message());
    }

    @Test
    void handleUnexpectedShouldReturnInternalServerError() {
        ResponseEntity<ApiResponse<Object>> response = handler.handleUnexpected(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().errorCode());
        assertEquals("Internal server error", response.getBody().message());
    }
}
