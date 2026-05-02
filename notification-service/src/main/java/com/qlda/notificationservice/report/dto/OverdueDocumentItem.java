package com.qlda.notificationservice.report.dto;

import java.time.LocalDateTime;

public record OverdueDocumentItem(
    Long documentId,
    String soKyHieu,
    String trichYeu,
    Long nguoiXuLyId,
    String nguoiXuLy,
    LocalDateTime hanXuLy,
    Integer soNgayTre,
    Integer trangThaiXuLy
) {
}

