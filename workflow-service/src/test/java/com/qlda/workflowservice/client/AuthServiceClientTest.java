package com.qlda.workflowservice.client;

import com.qlda.workflowservice.client.dto.AuthPermissionCheckRequest;
import com.qlda.workflowservice.client.dto.AuthPermissionDto;
import com.qlda.workflowservice.client.dto.AuthPermissionCheckResponse;
import com.qlda.workflowservice.client.dto.AuthUnitDto;
import com.qlda.workflowservice.client.dto.AuthUserDto;
import com.qlda.workflowservice.client.dto.AuthUserRolesDto;
import com.qlda.workflowservice.client.dto.ValidationResponse;
import com.qlda.workflowservice.client.impl.AuthServiceClientImpl;
import com.qlda.workflowservice.client.internal.AuthServiceHttpClient;
import com.qlda.workflowservice.exception.ApiException;
import com.qlda.workflowservice.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceClientTest {

    @Mock
    private AuthServiceHttpClient authServiceHttpClient;

    @InjectMocks
    private AuthServiceClientImpl authServiceClient;

    @Test
    void getUserAndUnit_success() {
        when(authServiceHttpClient.getUserById(1L)).thenReturn(new AuthUserDto(1L, "A", "Nguyen Van A", "a@x.com", 1, "Don vi", 2, "CHUYEN_VIEN", 1));
        when(authServiceHttpClient.getUnitById(1)).thenReturn(new AuthUnitDto(1, "DV", "Van phong", null, true));

        assertEquals(1L, authServiceClient.getUserById(1L).id());
        assertEquals(1, authServiceClient.getUnitById(1).id());
    }

    @Test
    void validateUsers_fail_shouldThrow() {
        when(authServiceHttpClient.validateUsers(List.of(1L, 2L)))
                .thenReturn(new ValidationResponse(false, List.of("2")));

        ApiException exception = assertThrows(ApiException.class, () -> authServiceClient.validateUsers(List.of(1L, 2L)));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void validateUnits_fail_shouldThrow() {
        when(authServiceHttpClient.validateUnits(List.of(1, 2)))
                .thenReturn(new ValidationResponse(false, List.of("2")));

        ApiException exception = assertThrows(ApiException.class, () -> authServiceClient.validateUnits(List.of(1, 2)));

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
    }

    @Test
    void getRolesAndPermission_success() {
        when(authServiceHttpClient.getUserRoles(1L)).thenReturn(new AuthUserRolesDto(1L, List.of("LANH_DAO"),
                List.of(new AuthPermissionDto("DOCUMENT_APPROVAL", true, false, true, false, true))));
        when(authServiceHttpClient.checkPermission(new AuthPermissionCheckRequest(1L, "DOCUMENT_APPROVAL", "IsApprove")))
                .thenReturn(new AuthPermissionCheckResponse(true, 1L, "DOCUMENT_APPROVAL", "IsApprove"));

        assertEquals(1, authServiceClient.getUserRoles(1L).roles().size());
        assertEquals(true, authServiceClient.checkPermission(1L, "DOCUMENT_APPROVAL", "IsApprove").allowed());
    }
}
