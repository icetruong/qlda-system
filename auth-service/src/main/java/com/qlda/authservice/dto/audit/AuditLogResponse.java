package com.qlda.authservice.dto.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long nguoiDungId,
        String hoTen,
        String hanhDong,
        String doiTuong,
        Long doiTuongId,
        String noiDungChiTiet,
        String diaChiIp,
        LocalDateTime thoiGianThucHien,
        Integer trangThai
) {
}
