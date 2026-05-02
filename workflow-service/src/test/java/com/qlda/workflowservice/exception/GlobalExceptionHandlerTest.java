package com.qlda.workflowservice.exception;

import com.qlda.workflowservice.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApiException_shouldReturnExpectedCode() {
        ApiException ex = new ApiException(ErrorCode.INVALID_WORKFLOW_STATUS, HttpStatus.BAD_REQUEST, "Invalid status");

        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INVALID_WORKFLOW_STATUS", response.getBody().getErrorCode());
    }

    @Test
    void handleAccessDenied_shouldReturnForbidden() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDenied(new AccessDeniedException("deny"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("FORBIDDEN", response.getBody().getErrorCode());
    }
}
