package com.qlda.authservice.dto.security;

public record SecurityPolicyResponse(
        int sessionTimeoutMinutes,
        int maxLoginAttempts,
        boolean requireAuthentication,
        boolean enableCors,
        boolean enableIpWhitelist
) {
}
