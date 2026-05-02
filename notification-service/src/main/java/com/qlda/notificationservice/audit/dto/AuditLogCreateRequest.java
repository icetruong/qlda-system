package com.qlda.notificationservice.audit.dto;

import jakarta.validation.constraints.NotBlank;

public record AuditLogCreateRequest(
    Long nguoiDungId,
    @NotBlank(message = "hanhDong is required")
    String hanhDong,
    String doiTuong,
    Long doiTuongId,
    String noiDungChiTiet,
    String diaChiIP,
    Integer trangThai
) {
}

