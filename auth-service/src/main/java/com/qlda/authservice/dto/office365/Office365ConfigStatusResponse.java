package com.qlda.authservice.dto.office365;

public record Office365ConfigStatusResponse(
        boolean tenantIdConfigured,
        boolean clientIdConfigured,
        boolean clientSecretConfigured,
        boolean sharePointConfigured,
        boolean teamsConfigured,
        boolean outlookConfigured
) {
}
