package com.qlda.authservice.dto.office365;

public record Office365ConnectionCheckResponse(
        boolean azureAd,
        boolean sharePoint,
        boolean oneDrive,
        boolean teams,
        boolean outlook
) {
}
