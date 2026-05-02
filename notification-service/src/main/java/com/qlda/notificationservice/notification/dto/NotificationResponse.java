package com.qlda.notificationservice.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String tieuDe,
    String noiDung,
    Long nguoiNhanId,
    Long vanBanId,
    String loaiThongBao,
    String kenhGui,
    Boolean daDoc,
    LocalDateTime ngayGui,
    LocalDateTime ngayDoc
) {
}

