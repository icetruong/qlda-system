package com.qlda.notificationservice.client.dto;

public record AuthUserResponse(
    Long id,
    String username,
    String hoTen,
    String email,
    Integer donViId,
    String tenDonVi
) {
}
