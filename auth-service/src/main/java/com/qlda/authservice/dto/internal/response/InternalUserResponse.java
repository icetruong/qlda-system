package com.qlda.authservice.dto.internal.response;

public record InternalUserResponse(
        Long id,
        String username,
        String hoTen,
        String email,
        Integer donViId,
        String tenDonVi,
        Integer nhomQuyenId,
        String maNhomQuyen,
        Integer trangThai
) {
}
