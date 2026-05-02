package com.qlda.authservice.controller;

import com.qlda.authservice.common.ApiResponse;
import com.qlda.authservice.dto.security.PermissionCheckRequest;
import com.qlda.authservice.dto.security.PermissionCheckResponse;
import com.qlda.authservice.dto.security.SecurityPolicyResponse;
import com.qlda.authservice.service.SecurityPolicyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/security-policies")
public class SecurityPolicyController {

    private final SecurityPolicyService securityPolicyService;

    public SecurityPolicyController(SecurityPolicyService securityPolicyService) {
        this.securityPolicyService = securityPolicyService;
    }

    @GetMapping
    public ApiResponse<SecurityPolicyResponse> getSecurityPolicies() {
        return ApiResponse.success("Get security policies successfully", securityPolicyService.getSecurityPolicies());
    }

    @PostMapping("/check-permission")
    public ApiResponse<PermissionCheckResponse> checkPermission(@Valid @RequestBody PermissionCheckRequest request) {
        return ApiResponse.success("Check permission successfully", securityPolicyService.checkPermission(request));
    }
}
