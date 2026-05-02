package com.qlda.notificationservice.audit.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
    Long id,
    Long nguoiDungId,
    String hanhDong,
    String doiTuong,
    Long doiTuongId,
    String noiDungChiTiet,
    String diaChiIP,
    LocalDateTime thoiGianThucHien,
    Integer trangThai
) {
}

