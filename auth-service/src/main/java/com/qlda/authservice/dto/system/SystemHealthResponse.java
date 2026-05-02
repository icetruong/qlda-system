package com.qlda.authservice.dto.system;

import java.time.LocalDateTime;

public record SystemHealthResponse(
        String service,
        String status,
        String database,
        LocalDateTime timestamp
) {
}
