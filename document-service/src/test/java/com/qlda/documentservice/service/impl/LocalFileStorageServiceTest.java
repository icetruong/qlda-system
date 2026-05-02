package com.qlda.documentservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.qlda.documentservice.config.AppProperties;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class LocalFileStorageServiceTest {

    private Path tempDir;
    private LocalFileStorageService service;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("doc-service-test");
        AppProperties appProperties = new AppProperties();
        appProperties.setUploadDir(tempDir.toString());
        service = new LocalFileStorageService(appProperties);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
        }
    }

    @Test
    void store_shouldSaveFileAndReturnUploadUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "hello".getBytes());

        String url = service.store(file);

        assertThat(url).startsWith("/uploads/").endsWith(".txt");
        String storedName = url.substring("/uploads/".length());
        assertThat(Files.exists(tempDir.resolve(storedName))).isTrue();
    }

    @Test
    void store_shouldThrowBadRequest_whenFileEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> service.store(file))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.FILE_UPLOAD_FAILED));
    }

    @Test
    void delete_shouldRemoveStoredFile() throws IOException {
        Path filePath = tempDir.resolve("x.pdf");
        Files.write(filePath, "abc".getBytes());

        service.delete("/uploads/x.pdf");

        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    void buildFileUrl_shouldPrefixUploadsPath() {
        assertThat(service.buildFileUrl("abc.pdf")).isEqualTo("/uploads/abc.pdf");
    }
}
