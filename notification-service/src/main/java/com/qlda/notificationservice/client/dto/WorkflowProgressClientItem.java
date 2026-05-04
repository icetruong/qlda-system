package com.qlda.notificationservice.client.dto;

import java.time.LocalDateTime;

public record WorkflowProgressClientItem(
    Long documentId,
    String soKyHieu,
    String trichYeu,
    Long nguoiXuLyId,
    Integer trangThaiXuLy,
    Integer tyLeHoanThanh,
    LocalDateTime hanXuLy
) {
}
