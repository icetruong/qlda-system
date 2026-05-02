package com.qlda.authservice.dto.auth;

import java.util.List;

public record AuthUserResponse(
        Long id,
        String username,
        String hoTen,
        String email,
        List<String> roles
) {
}
