package com.qlda.authservice.controller;

import com.qlda.authservice.common.ApiResponse;
import com.qlda.authservice.dto.auth.AuthTokenResponse;
import com.qlda.authservice.dto.auth.AzureLoginRequest;
import com.qlda.authservice.dto.auth.CurrentUserResponse;
import com.qlda.authservice.dto.auth.LogoutRequest;
import com.qlda.authservice.dto.auth.RefreshTokenRequest;
import com.qlda.authservice.dto.auth.RefreshTokenResponse;
import com.qlda.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/azure")
    public ApiResponse<AuthTokenResponse> loginAzure(
            @Valid @RequestBody AzureLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.success("Azure login successfully", authService.loginAzure(request, httpServletRequest.getRemoteAddr()));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Refresh token successfully", authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout(@Valid @RequestBody LogoutRequest request, HttpServletRequest httpServletRequest) {
        authService.logout(request, httpServletRequest.getRemoteAddr());
        return ApiResponse.success("Logout successfully", Map.of());
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.success("Get current user successfully", authService.getCurrentUser());
    }
}
