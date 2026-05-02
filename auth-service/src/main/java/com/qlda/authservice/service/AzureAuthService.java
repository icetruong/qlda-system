package com.qlda.authservice.service;

import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.dto.auth.AzureLoginRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AzureAuthService {

    private final AuthProperties authProperties;

    public AzureAuthService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public AzureUserInfo exchangeCodeForUser(AzureLoginRequest request) {
        AuthProperties.Azure azure = authProperties.getAzure();
        // TODO: integrate real Microsoft OAuth2 + Graph API profile lookup.
        // Current implementation is a deterministic mock/skeleton based on authorization code.
        if (!StringUtils.hasText(request.authorizationCode())) {
            return null;
        }
        String normalized = request.authorizationCode().trim();
        String username = normalized.length() > 30 ? normalized.substring(0, 30) : normalized;
        return new AzureUserInfo(
                normalized,
                username + "@azure.mock",
                username,
                "Azure User " + username,
                StringUtils.hasText(azure.getTenantId())
                        && StringUtils.hasText(azure.getClientId())
                        && StringUtils.hasText(azure.getClientSecret())
        );
    }

    public record AzureUserInfo(
            String azureAdId,
            String email,
            String username,
            String displayName,
            boolean graphConfigured
    ) {
    }
}
