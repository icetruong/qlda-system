package com.qlda.authservice.dto.internal.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ValidateUsersRequest(
        @NotEmpty(message = "userIds is required")
        List<@NotNull(message = "userId cannot be null") Long> userIds
) {
}
