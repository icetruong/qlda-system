package com.qlda.aiservice.dto.internal;

public record DocumentMetadataDto(
    Long id,
    String trichYeu,
    Long loaiVanBanId,
    Long donViChuTriId,
    Long nguoiTaoId,
    String hanXuLy
) {
}
