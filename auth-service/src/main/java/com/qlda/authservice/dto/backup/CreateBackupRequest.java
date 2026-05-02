package com.qlda.authservice.dto.backup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBackupRequest(
        @NotBlank(message = "backupType is required")
        @Size(max = 20, message = "backupType must be <= 20 chars")
        String backupType,
        @Size(max = 500, message = "description must be <= 500 chars")
        String description
) {
}
