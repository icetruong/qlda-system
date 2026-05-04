package com.qlda.authservice.dto.internal.response;

import java.util.List;

public record ValidateUsersResponse(
        boolean valid,
        List<Long> invalidUserIds
) {
}
