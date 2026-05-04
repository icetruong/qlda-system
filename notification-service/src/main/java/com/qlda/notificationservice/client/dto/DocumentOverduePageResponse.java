package com.qlda.notificationservice.client.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DocumentOverduePageResponse(
    List<OverdueDocumentClientItem> content,
    Integer page,
    Integer size,
    Long totalElements
) {
    public record OverdueDocumentClientItem(
        Long documentId,
        String soKyHieu,
        String trichYeu,
        Long nguoiXuLyId,
        LocalDateTime hanXuLy,
        Integer soNgayTre,
        Integer trangThai
    ) {
    }
}
