package com.qlda.workflowservice.dto.response;

import java.time.LocalDateTime;

public record ProcessingDetailResponse(
        Long id,
        Long documentId,
        Long buocQuyTrinhId,
        Long nguoiGuiId,
        Long nguoiNhanId,
        Integer donViXuLyId,
        String hanhDongXuLy,
        String yKienXuLy,
        LocalDateTime ngayNhan,
        LocalDateTime hanXuLy,
        LocalDateTime ngayHoanThanh,
        Integer tyLeHoanThanh,
        Integer trangThaiXuLy,
        String tepKetQua
) {
}
