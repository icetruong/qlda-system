package com.qlda.authservice.dto.internal.response;

public record InternalUnitResponse(
        Integer id,
        String maDonVi,
        String tenDonVi,
        Integer donViChaId,
        Boolean suDung
) {
}
