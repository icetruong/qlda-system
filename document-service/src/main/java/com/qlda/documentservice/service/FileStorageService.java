package com.qlda.documentservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file);

    void delete(String path);

    String buildFileUrl(String filename);
}

