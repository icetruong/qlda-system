package com.qlda.authservice.service;

import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.dto.office365.Office365ConfigStatusResponse;
import com.qlda.authservice.dto.office365.Office365ConnectionCheckResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class Office365Service {

    private final AuthProperties authProperties;

    public Office365Service(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public Office365ConfigStatusResponse getConfigStatus() {
        AuthProperties.Office365 office365 = authProperties.getOffice365();
        return new Office365ConfigStatusResponse(
                hasText(office365.getTenantId()),
                hasText(office365.getClientId()),
                hasText(office365.getClientSecret()),
                hasText(office365.getSharePoint()),
                hasText(office365.getTeams()),
                hasText(office365.getOutlook())
        );
    }

    public Office365ConnectionCheckResponse checkConnection() {
        Office365ConfigStatusResponse status = getConfigStatus();
        // TODO: integrate Microsoft Graph API ping/health checks for each workload.
        boolean azureAd = status.tenantIdConfigured() && status.clientIdConfigured() && status.clientSecretConfigured();
        boolean sharePoint = azureAd && status.sharePointConfigured();
        boolean oneDrive = azureAd && status.sharePointConfigured();
        boolean teams = azureAd && status.teamsConfigured();
        boolean outlook = azureAd && status.outlookConfigured();
        return new Office365ConnectionCheckResponse(azureAd, sharePoint, oneDrive, teams, outlook);
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }
}
