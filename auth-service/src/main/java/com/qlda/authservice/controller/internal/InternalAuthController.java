package com.qlda.authservice.controller.internal;

import com.qlda.authservice.common.ApiResponse;
import com.qlda.authservice.dto.internal.request.InternalPermissionCheckRequest;
import com.qlda.authservice.dto.internal.request.ValidateUnitsRequest;
import com.qlda.authservice.dto.internal.request.ValidateUsersRequest;
import com.qlda.authservice.dto.internal.response.InternalPermissionCheckResponse;
import com.qlda.authservice.dto.internal.response.InternalUnitResponse;
import com.qlda.authservice.dto.internal.response.InternalUserCountResponse;
import com.qlda.authservice.dto.internal.response.InternalUserResponse;
import com.qlda.authservice.dto.internal.response.InternalUserRolesResponse;
import com.qlda.authservice.dto.internal.response.ValidateUnitsResponse;
import com.qlda.authservice.dto.internal.response.ValidateUsersResponse;
import com.qlda.authservice.service.InternalAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {

    private final InternalAuthService internalAuthService;

    public InternalAuthController(InternalAuthService internalAuthService) {
        this.internalAuthService = internalAuthService;
    }

    @GetMapping("/users/{id}")
    public ApiResponse<InternalUserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.success("Get internal user successfully", internalAuthService.getInternalUser(id));
    }

    @PostMapping("/users/validate")
    public ApiResponse<ValidateUsersResponse> validateUsers(@Valid @RequestBody ValidateUsersRequest request) {
        return ApiResponse.success("Validate users successfully", internalAuthService.validateUsers(request));
    }

    @GetMapping("/units/{id}")
    public ApiResponse<InternalUnitResponse> getUnit(@PathVariable Integer id) {
        return ApiResponse.success("Get internal unit successfully", internalAuthService.getInternalUnit(id));
    }

    @PostMapping("/units/validate")
    public ApiResponse<ValidateUnitsResponse> validateUnits(@Valid @RequestBody ValidateUnitsRequest request) {
        return ApiResponse.success("Validate units successfully", internalAuthService.validateUnits(request));
    }

    @GetMapping("/users/{id}/roles")
    public ApiResponse<InternalUserRolesResponse> getUserRoles(@PathVariable Long id) {
        return ApiResponse.success("Get user roles successfully", internalAuthService.getUserRoles(id));
    }

    @GetMapping("/statistics/users/count")
    public ApiResponse<InternalUserCountResponse> getUserCount() {
        return ApiResponse.success("Get user count successfully", internalAuthService.getUserCount());
    }

    @PostMapping("/permissions/check")
    public ApiResponse<InternalPermissionCheckResponse> checkPermission(
            @Valid @RequestBody InternalPermissionCheckRequest request
    ) {
        return ApiResponse.success("Check permission successfully", internalAuthService.checkPermission(request));
    }
}
