package com.qlda.documentservice.service.impl;

import com.qlda.documentservice.config.AppProperties;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.service.FileStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Path uploadPath;

    public LocalFileStorageService(AppProperties appProperties) {
        this.uploadPath = Paths.get(appProperties.getUploadDir()).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest(ErrorCode.FILE_UPLOAD_FAILED, "File is empty");
        }
        try {
            Files.createDirectories(uploadPath);
            String original = file.getOriginalFilename();
            String extension = "";
            if (original != null && original.contains(".")) {
                extension = original.substring(original.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + extension;
            Path targetPath = uploadPath.resolve(storedName).normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return buildFileUrl(storedName);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "Upload file failed", org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void delete(String path) {
        if (path == null || path.isBlank()) {
            return;
        }
        String filename = path.replace("\\", "/");
        if (filename.startsWith("/uploads/")) {
            filename = filename.substring("/uploads/".length());
        }
        Path targetPath = uploadPath.resolve(filename).normalize();
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException ignored) {
            // ignore delete error because DB state is authoritative
        }
    }

    @Override
    public String buildFileUrl(String filename) {
        return "/uploads/" + filename;
    }
}

