package com.qlda.workflowservice.client.dto;

import java.time.LocalDateTime;

public record DocumentDetailDto(
        Long id,
        String soKyHieu,
        String trichYeu,
        Integer loaiVanBanId,
        String tenLoaiVanBan,
        String documentType,
        Integer donViChuTriId,
        Long nguoiTaoId,
        LocalDateTime hanXuLy,
        Integer trangThai,
        Boolean daOCR,
        Boolean daKySo
) {
}
