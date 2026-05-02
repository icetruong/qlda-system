package com.qlda.authservice.service;

import com.qlda.authservice.common.ErrorCode;
import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.dto.backup.BackupCreateResponse;
import com.qlda.authservice.dto.backup.BackupFileNameResponse;
import com.qlda.authservice.dto.backup.BackupItemResponse;
import com.qlda.authservice.dto.backup.CreateBackupRequest;
import com.qlda.authservice.dto.backup.RestoreBackupRequest;
import com.qlda.authservice.exception.ApiException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BackupService {

    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final Path backupDir;

    public BackupService(AuthProperties authProperties) {
        this.backupDir = Path.of(authProperties.getBackup().getDirectory()).toAbsolutePath().normalize();
    }

    public BackupCreateResponse createBackup(CreateBackupRequest request) {
        ensureBackupDirectory();
        String fileName = "backup_" + LocalDateTime.now().format(FILE_TIME_FORMATTER) + ".sql";
        Path backupPath = resolveBackupPath(fileName);
        String content = """
                -- TODO: integrate pg_dump or database native backup command.
                -- backupType=%s
                -- description=%s
                -- createdAt=%s
                """.formatted(request.backupType(), request.description(), Instant.now());
        try {
            Files.writeString(
                    backupPath,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, "Cannot create backup file");
        }
        return new BackupCreateResponse(fileName, "/backups/" + fileName);
    }

    public List<BackupItemResponse> getBackups() {
        ensureBackupDirectory();
        try (var stream = Files.list(backupDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(this::lastModified).reversed())
                    .map(path -> new BackupItemResponse(
                            path.getFileName().toString(),
                            getFileSize(path),
                            LocalDateTime.ofInstant(lastModified(path).toInstant(), ZoneId.systemDefault())
                    ))
                    .toList();
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, "Cannot list backups");
        }
    }

    public BackupFileNameResponse restoreBackup(RestoreBackupRequest request) {
        if (!Boolean.TRUE.equals(request.confirmRestore())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "confirmRestore must be true");
        }
        Path filePath = resolveBackupPath(request.fileName());
        if (!Files.exists(filePath)) {
            throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.INVALID_REQUEST, "Backup file not found");
        }
        // TODO: integrate psql restore or database native restore command.
        return new BackupFileNameResponse(request.fileName());
    }

    public BackupFileNameResponse deleteBackup(String fileName) {
        Path filePath = resolveBackupPath(fileName);
        try {
            if (!Files.exists(filePath)) {
                throw new ApiException(HttpStatus.NOT_FOUND, ErrorCode.INVALID_REQUEST, "Backup file not found");
            }
            Files.delete(filePath);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, "Cannot delete backup file");
        }
        return new BackupFileNameResponse(fileName);
    }

    private void ensureBackupDirectory() {
        try {
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, "Cannot create backup directory");
        }
    }

    private Path resolveBackupPath(String fileName) {
        Path resolved = backupDir.resolve(fileName).normalize();
        if (!resolved.startsWith(backupDir)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "Invalid file name");
        }
        return resolved;
    }

    private long getFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException exception) {
            return 0;
        }
    }

    private FileTime lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path);
        } catch (IOException exception) {
            return FileTime.fromMillis(0);
        }
    }
}
