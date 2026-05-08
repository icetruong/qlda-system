package com.qlda.aiservice.service;

import com.qlda.aiservice.exception.AppException;
import com.qlda.aiservice.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Service
public class StubOcrService implements OcrService {

    @Override
    public String extractText(MultipartFile file) {
        try {
            String text = new String(file.getBytes(), StandardCharsets.UTF_8).trim();
            return text.isEmpty() ? file.getOriginalFilename() : text;
        } catch (Exception ex) {
            throw new AppException(ErrorCode.OCR_FAILED, HttpStatus.INTERNAL_SERVER_ERROR, "OCR failed");
        }
    }
}

