package com.qlda.authservice.dto.internal.response;

import java.util.List;

public record ValidateUnitsResponse(
        boolean valid,
        List<Integer> invalidUnitIds
) {
}
