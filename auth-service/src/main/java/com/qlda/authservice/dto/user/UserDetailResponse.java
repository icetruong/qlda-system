package com.qlda.authservice.dto.user;

public record UserDetailResponse(
        Long id,
        String username,
        String hoTen,
        String email,
        String dienThoai,
        Integer donViId,
        String chucVu,
        Integer nhomQuyenId,
        String azureAdId,
        Integer trangThai
) {
}
