package com.qlda.authservice.dto.system;

public record SystemConfigResponse(
        String appName,
        String environment,
        String maxUploadFileSize,
        long jwtExpiration,
        boolean backupEnabled
) {
}
