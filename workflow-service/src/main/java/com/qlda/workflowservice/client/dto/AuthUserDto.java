package com.qlda.workflowservice.client.dto;

import java.util.List;

public record AuthUserDto(
        Long userId,
        String username,
        List<String> roles
) {
}
