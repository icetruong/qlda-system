package com.qlda.workflowservice.client.impl;

import com.qlda.workflowservice.client.AuthServiceClient;
import com.qlda.workflowservice.client.dto.AuthPermissionCheckRequest;
import com.qlda.workflowservice.client.dto.AuthPermissionCheckResponse;
import com.qlda.workflowservice.client.dto.AuthUnitDto;
import com.qlda.workflowservice.client.dto.AuthUserDto;
import com.qlda.workflowservice.client.dto.AuthUserRolesDto;
import com.qlda.workflowservice.client.dto.ValidationResponse;
import com.qlda.workflowservice.client.internal.AuthServiceHttpClient;
import com.qlda.workflowservice.exception.ApiException;
import com.qlda.workflowservice.exception.ErrorCode;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AuthServiceClientImpl implements AuthServiceClient {
    private final AuthServiceHttpClient authServiceHttpClient;

    @Override
    public AuthUserDto getUserById(Long id) {
        return execute(() -> authServiceHttpClient.getUserById(id));
    }

    @Override
    public void validateUsers(List<Long> userIds) {
        ValidationResponse result = execute(() -> authServiceHttpClient.validateUsers(userIds));
        if (result == null || !result.valid()) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, HttpStatus.BAD_REQUEST, "Invalid users");
        }
    }

    @Override
    public AuthUnitDto getUnitById(Integer id) {
        return execute(() -> authServiceHttpClient.getUnitById(id));
    }

    @Override
    public void validateUnits(List<Integer> unitIds) {
        ValidationResponse result = execute(() -> authServiceHttpClient.validateUnits(unitIds));
        if (result == null || !result.valid()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "Invalid units");
        }
    }

    @Override
    public AuthUserRolesDto getUserRoles(Long id) {
        return execute(() -> authServiceHttpClient.getUserRoles(id));
    }

    @Override
    public AuthPermissionCheckResponse checkPermission(Long userId, String maChucNang, String permission) {
        return execute(() -> authServiceHttpClient.checkPermission(new AuthPermissionCheckRequest(userId, maChucNang, permission)));
    }

    private <T> T execute(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (FeignException exception) {
            if (exception.status() == 404) {
                throw new ApiException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "Auth resource not found");
            }
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.BAD_GATEWAY, "Auth service request failed");
        }
    }
}
