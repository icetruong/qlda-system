package com.qlda.authservice.dto.user;

public record UserSummaryResponse(
        Long id,
        String username,
        String hoTen,
        String email,
        String dienThoai,
        Integer donViId,
        String tenDonVi,
        String chucVu,
        Integer nhomQuyenId,
        String tenNhomQuyen,
        Integer trangThai
) {
}
