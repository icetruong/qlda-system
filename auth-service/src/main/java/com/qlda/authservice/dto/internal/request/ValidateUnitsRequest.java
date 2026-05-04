package com.qlda.authservice.dto.internal.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ValidateUnitsRequest(
        @NotEmpty(message = "unitIds is required")
        List<@NotNull(message = "unitId cannot be null") Integer> unitIds
) {
}
