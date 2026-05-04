package com.qlda.workflowservice.client.dto;

public record AuthUserDto(
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
