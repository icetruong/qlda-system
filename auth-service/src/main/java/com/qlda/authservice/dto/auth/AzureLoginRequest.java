package com.qlda.authservice.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AzureLoginRequest(
        @NotBlank(message = "authorizationCode is required")
        String authorizationCode,
        @NotBlank(message = "redirectUri is required")
        String redirectUri
) {
}
