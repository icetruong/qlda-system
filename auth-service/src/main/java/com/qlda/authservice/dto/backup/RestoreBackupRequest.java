package com.qlda.authservice.dto.backup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RestoreBackupRequest(
        @NotBlank(message = "fileName is required")
        String fileName,
        @NotNull(message = "confirmRestore is required")
        Boolean confirmRestore
) {
}
