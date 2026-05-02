package com.qlda.authservice.controller;

import com.qlda.authservice.common.ApiResponse;
import com.qlda.authservice.dto.backup.BackupCreateResponse;
import com.qlda.authservice.dto.backup.BackupFileNameResponse;
import com.qlda.authservice.dto.backup.BackupItemResponse;
import com.qlda.authservice.dto.backup.CreateBackupRequest;
import com.qlda.authservice.dto.backup.RestoreBackupRequest;
import com.qlda.authservice.service.BackupService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/backups")
public class BackupController {

    private final BackupService backupService;

    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping
    public ApiResponse<BackupCreateResponse> createBackup(@Valid @RequestBody CreateBackupRequest request) {
        return ApiResponse.success("Create backup successfully", backupService.createBackup(request));
    }

    @GetMapping
    public ApiResponse<List<BackupItemResponse>> getBackups() {
        return ApiResponse.success("Get backup list successfully", backupService.getBackups());
    }

    @PostMapping("/restore")
    public ApiResponse<BackupFileNameResponse> restoreBackup(@Valid @RequestBody RestoreBackupRequest request) {
        return ApiResponse.success("Restore database successfully", backupService.restoreBackup(request));
    }

    @DeleteMapping("/{fileName}")
    public ApiResponse<BackupFileNameResponse> deleteBackup(@PathVariable String fileName) {
        return ApiResponse.success("Delete backup successfully", backupService.deleteBackup(fileName));
    }
}
