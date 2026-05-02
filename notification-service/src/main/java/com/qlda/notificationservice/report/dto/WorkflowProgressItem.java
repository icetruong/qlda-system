package com.qlda.notificationservice.report.dto;

import java.time.LocalDateTime;

public record WorkflowProgressItem(
    Long documentId,
    String soKyHieu,
    String trichYeu,
    Long nguoiXuLyId,
    String nguoiXuLy,
    Integer trangThaiXuLy,
    Integer tyLeHoanThanh,
    LocalDateTime hanXuLy
) {
}

