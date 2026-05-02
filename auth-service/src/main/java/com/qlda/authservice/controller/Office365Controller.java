package com.qlda.authservice.controller;

import com.qlda.authservice.common.ApiResponse;
import com.qlda.authservice.dto.office365.Office365ConfigStatusResponse;
import com.qlda.authservice.dto.office365.Office365ConnectionCheckResponse;
import com.qlda.authservice.service.Office365Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/office365")
public class Office365Controller {

    private final Office365Service office365Service;

    public Office365Controller(Office365Service office365Service) {
        this.office365Service = office365Service;
    }

    @GetMapping("/config/status")
    public ApiResponse<Office365ConfigStatusResponse> getConfigStatus() {
        return ApiResponse.success("Get Office 365 config status successfully", office365Service.getConfigStatus());
    }

    @GetMapping("/connection/check")
    public ApiResponse<Office365ConnectionCheckResponse> checkConnection() {
        return ApiResponse.success("Check Office 365 connection successfully", office365Service.checkConnection());
    }
}
