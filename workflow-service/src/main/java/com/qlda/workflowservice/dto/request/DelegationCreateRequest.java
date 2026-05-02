package com.qlda.workflowservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DelegationCreateRequest(
        @NotNull Long nguoiUyQuyenId,
        @NotNull Long nguoiDuocUyQuyenId,
        @NotNull LocalDate tuNgay,
        @NotNull LocalDate denNgay,
        @Size(max = 100) String phamViUyQuyen,
        @Size(max = 500) String ghiChu
) {
}
