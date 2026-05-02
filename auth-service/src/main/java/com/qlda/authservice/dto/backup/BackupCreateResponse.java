package com.qlda.authservice.dto.backup;

public record BackupCreateResponse(
        String fileName,
        String fileUrl
) {
}
