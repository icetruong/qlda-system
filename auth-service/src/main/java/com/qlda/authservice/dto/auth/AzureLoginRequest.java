package com.qlda.authservice.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record AzureLoginRequest(
        @NotBlank(message = "authorizationCode is required")
        @JsonAlias({"authorization_code", "code"})
        String authorizationCode,
        @NotBlank(message = "redirectUri is required")
        @JsonAlias("redirect_uri")
        String redirectUri,
        @JsonAlias("code_verifier")
        String codeVerifier
) {
}
