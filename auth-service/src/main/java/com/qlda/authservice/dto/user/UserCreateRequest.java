package com.qlda.authservice.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "username is required")
        @Size(max = 100, message = "username must be <= 100 chars")
        String username,
        @NotBlank(message = "hoTen is required")
        @Size(max = 255, message = "hoTen must be <= 255 chars")
        String hoTen,
        @NotBlank(message = "email is required")
        @Email(message = "email is invalid")
        @Size(max = 150, message = "email must be <= 150 chars")
        String email,
        @Size(max = 20, message = "dienThoai must be <= 20 chars")
        String dienThoai,
        @NotNull(message = "donViId is required")
        Integer donViId,
        @Size(max = 100, message = "chucVu must be <= 100 chars")
        String chucVu,
        @NotNull(message = "nhomQuyenId is required")
        Integer nhomQuyenId,
        @Size(max = 100, message = "azureAdId must be <= 100 chars")
        String azureAdId
) {
}
