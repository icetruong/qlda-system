package com.qlda.documentservice.service.impl;

import com.qlda.documentservice.client.AiServiceClient;
import com.qlda.documentservice.client.dto.AiClientDtos;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.entity.TepDinhKem;
import com.qlda.documentservice.entity.VanBan;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.mapper.DocumentMapper;
import com.qlda.documentservice.repository.TepDinhKemRepository;
import com.qlda.documentservice.repository.VanBanRepository;
import com.qlda.documentservice.security.SecurityUtils;
import com.qlda.documentservice.service.AttachmentService;
import com.qlda.documentservice.service.FileStorageService;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentServiceImpl implements AttachmentService {
    private static final Logger log = LoggerFactory.getLogger(AttachmentServiceImpl.class);
    private static final String SOURCE_SERVICE = "document-service";

    private final TepDinhKemRepository tepDinhKemRepository;
    private final VanBanRepository vanBanRepository;
    private final FileStorageService fileStorageService;
    private final DocumentMapper documentMapper;
    private final SecurityUtils securityUtils;
    private final AiServiceClient aiServiceClient;

    public AttachmentServiceImpl(
        TepDinhKemRepository tepDinhKemRepository,
        VanBanRepository vanBanRepository,
        FileStorageService fileStorageService,
        DocumentMapper documentMapper,
        SecurityUtils securityUtils,
        AiServiceClient aiServiceClient
    ) {
        this.tepDinhKemRepository = tepDinhKemRepository;
        this.vanBanRepository = vanBanRepository;
        this.fileStorageService = fileStorageService;
        this.documentMapper = documentMapper;
        this.securityUtils = securityUtils;
        this.aiServiceClient = aiServiceClient;
    }

    @Override
    @Transactional
    public DocumentResponses.AttachmentResponse upload(Long documentId, MultipartFile file) {
        VanBan vanBan = vanBanRepository.findByIdAndDaXoaFalse(documentId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.DOCUMENT_NOT_FOUND, "Document not found"));
        String fileUrl = fileStorageService.store(file);
        TepDinhKem tepDinhKem = new TepDinhKem();
        tepDinhKem.setVanBan(vanBan);
        tepDinhKem.setTenTep(file.getOriginalFilename());
        tepDinhKem.setDuongDanTep(fileUrl);
        tepDinhKem.setLoaiTep(resolveType(file.getOriginalFilename()));
        tepDinhKem.setKichThuoc(file.getSize());
        tepDinhKem.setNgayTaiLen(LocalDateTime.now());
        securityUtils.getCurrentUserId().ifPresent(tepDinhKem::setNguoiTaiLenId);
        TepDinhKem savedAttachment = tepDinhKemRepository.save(tepDinhKem);
        requestIndexDocument(documentId);
        return documentMapper.toAttachmentResponse(savedAttachment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponses.AttachmentResponse> list(Long documentId) {
        vanBanRepository.findByIdAndDaXoaFalse(documentId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.DOCUMENT_NOT_FOUND, "Document not found"));
        return tepDinhKemRepository.findByVanBan_Id(documentId).stream()
            .map(documentMapper::toAttachmentResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponses.AttachmentDownloadResponse download(Long attachmentId) {
        TepDinhKem attachment = tepDinhKemRepository.findById(attachmentId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.ATTACHMENT_NOT_FOUND, "Attachment not found"));
        return new DocumentResponses.AttachmentDownloadResponse(attachmentId, attachment.getDuongDanTep());
    }

    @Override
    @Transactional
    public DocumentResponses.IdResponse delete(Long attachmentId) {
        TepDinhKem attachment = tepDinhKemRepository.findById(attachmentId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.ATTACHMENT_NOT_FOUND, "Attachment not found"));
        fileStorageService.delete(attachment.getDuongDanTep());
        tepDinhKemRepository.delete(attachment);
        return new DocumentResponses.IdResponse(attachmentId);
    }

    private String resolveType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private void requestIndexDocument(Long documentId) {
        try {
            aiServiceClient.indexDocument(documentId, new AiClientDtos.IndexDocumentRequest(SOURCE_SERVICE));
        } catch (Exception ex) {
            log.warn("Index document failed after attachment upload: documentId={}", documentId, ex);
        }
    }
}
