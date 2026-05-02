package com.qlda.authservice.service;

import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.dto.system.SystemConfigResponse;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    private final AuthProperties authProperties;

    public SystemConfigService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public SystemConfigResponse getSystemConfigs() {
        AuthProperties.SystemConfig config = authProperties.getSystemConfig();
        return new SystemConfigResponse(
                config.getAppName(),
                config.getEnvironment(),
                config.getMaxUploadFileSize(),
                authProperties.getJwt().getAccessTokenSeconds(),
                config.isBackupEnabled()
        );
    }
}
