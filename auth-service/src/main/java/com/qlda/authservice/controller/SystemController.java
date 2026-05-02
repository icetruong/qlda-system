package com.qlda.authservice.controller;

import com.qlda.authservice.common.ApiResponse;
import com.qlda.authservice.dto.system.SystemConfigResponse;
import com.qlda.authservice.dto.system.SystemHealthResponse;
import com.qlda.authservice.service.SystemConfigService;
import com.qlda.authservice.service.SystemHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class SystemController {

    private final SystemConfigService systemConfigService;
    private final SystemHealthService systemHealthService;

    public SystemController(SystemConfigService systemConfigService, SystemHealthService systemHealthService) {
        this.systemConfigService = systemConfigService;
        this.systemHealthService = systemHealthService;
    }

    @GetMapping("/system-configs")
    public ApiResponse<SystemConfigResponse> getSystemConfigs() {
        return ApiResponse.success("Get system configs successfully", systemConfigService.getSystemConfigs());
    }

    @GetMapping("/system/health")
    public ApiResponse<SystemHealthResponse> getSystemHealth() {
        return ApiResponse.success("System is running", systemHealthService.getSystemHealth());
    }
}
