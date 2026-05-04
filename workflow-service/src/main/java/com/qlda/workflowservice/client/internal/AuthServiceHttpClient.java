package com.qlda.workflowservice.client.internal;

import com.qlda.workflowservice.client.dto.AuthPermissionCheckRequest;
import com.qlda.workflowservice.client.dto.AuthPermissionCheckResponse;
import com.qlda.workflowservice.client.dto.AuthUnitDto;
import com.qlda.workflowservice.client.dto.AuthUserDto;
import com.qlda.workflowservice.client.dto.AuthUserRolesDto;
import com.qlda.workflowservice.client.dto.ValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "authServiceInternalClient", url = "${services.auth-service.base-url}")
public interface AuthServiceHttpClient {
    @GetMapping("/internal/auth/users/{id}")
    AuthUserDto getUserById(@PathVariable("id") Long id);

    @PostMapping("/internal/auth/users/validate")
    ValidationResponse validateUsers(@RequestBody List<Long> userIds);

    @GetMapping("/internal/auth/units/{id}")
    AuthUnitDto getUnitById(@PathVariable("id") Integer id);

    @PostMapping("/internal/auth/units/validate")
    ValidationResponse validateUnits(@RequestBody List<Integer> unitIds);

    @GetMapping("/internal/auth/users/{id}/roles")
    AuthUserRolesDto getUserRoles(@PathVariable("id") Long id);

    @PostMapping("/internal/auth/permissions/check")
    AuthPermissionCheckResponse checkPermission(@RequestBody AuthPermissionCheckRequest request);
}
