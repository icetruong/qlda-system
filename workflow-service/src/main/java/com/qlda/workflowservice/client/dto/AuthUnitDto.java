package com.qlda.workflowservice.client.dto;

public record AuthUnitDto(
        Integer id,
        String maDonVi,
        String tenDonVi,
        Integer donViChaId,
        Boolean suDung
) {
}
