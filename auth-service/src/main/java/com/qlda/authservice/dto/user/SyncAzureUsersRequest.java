package com.qlda.authservice.dto.user;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SyncAzureUsersRequest(
        @NotEmpty(message = "azureAdIds is required")
        List<String> azureAdIds
) {
}
