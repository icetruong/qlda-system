package com.qlda.authservice.dto.auth;

import java.util.List;

public record CurrentUserResponse(
        Long id,
        String username,
        String hoTen,
        String email,
        Integer donViId,
        Integer nhomQuyenId,
        List<String> roles
) {
}
