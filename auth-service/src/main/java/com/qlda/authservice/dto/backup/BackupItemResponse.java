package com.qlda.authservice.dto.backup;

import java.time.LocalDateTime;

public record BackupItemResponse(
        String fileName,
        long fileSize,
        LocalDateTime createdAt
) {
}
