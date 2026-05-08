package com.qlda.documentservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.qlda.documentservice.service.FileStorageService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock
    private TepDinhKemRepository tepDinhKemRepository;
    @Mock
    private VanBanRepository vanBanRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private DocumentMapper documentMapper;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private AiServiceClient aiServiceClient;

    @InjectMocks
    private AttachmentServiceImpl service;

    @Test
    void upload_shouldSaveAttachment_whenDocumentFound() {
        VanBan vanBan = new VanBan();
        vanBan.setId(1L);
        MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", "abc".getBytes());
        when(vanBanRepository.findByIdAndDaXoaFalse(1L)).thenReturn(Optional.of(vanBan));
        when(fileStorageService.store(file)).thenReturn("/uploads/s1.pdf");
        when(securityUtils.getCurrentUserId()).thenReturn(Optional.of(99L));
        when(tepDinhKemRepository.save(any(TepDinhKem.class))).thenAnswer(invocation -> {
            TepDinhKem entity = invocation.getArgument(0);
            entity.setId(11L);
            return entity;
        });
        when(documentMapper.toAttachmentResponse(any(TepDinhKem.class)))
            .thenReturn(new DocumentResponses.AttachmentResponse(11L, 1L, "sample.pdf", "/uploads/s1.pdf", "pdf", 3L, null));

        DocumentResponses.AttachmentResponse response = service.upload(1L, file);

        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.documentId()).isEqualTo(1L);
        verify(aiServiceClient).indexDocument(1L, new AiClientDtos.IndexDocumentRequest("document-service"));
    }

    @Test
    void upload_shouldThrowNotFound_whenDocumentMissing() {
        MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", "abc".getBytes());
        when(vanBanRepository.findByIdAndDaXoaFalse(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.upload(1L, file))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    @Test
    void list_shouldReturnMappedAttachments() {
        VanBan vanBan = new VanBan();
        vanBan.setId(1L);
        TepDinhKem item = new TepDinhKem();
        item.setId(2L);
        item.setVanBan(vanBan);
        when(vanBanRepository.findByIdAndDaXoaFalse(1L)).thenReturn(Optional.of(vanBan));
        when(tepDinhKemRepository.findByVanBan_Id(1L)).thenReturn(List.of(item));
        when(documentMapper.toAttachmentResponse(item))
            .thenReturn(new DocumentResponses.AttachmentResponse(2L, 1L, "f", "u", "pdf", 2L, null));

        List<DocumentResponses.AttachmentResponse> response = service.list(1L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(2L);
    }

    @Test
    void download_shouldThrowNotFound_whenAttachmentMissing() {
        when(tepDinhKemRepository.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.download(44L))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.ATTACHMENT_NOT_FOUND));
    }

    @Test
    void delete_shouldDeleteFileAndRecord() {
        TepDinhKem attachment = new TepDinhKem();
        attachment.setId(7L);
        attachment.setDuongDanTep("/uploads/a.pdf");
        when(tepDinhKemRepository.findById(7L)).thenReturn(Optional.of(attachment));

        DocumentResponses.IdResponse response = service.delete(7L);

        assertThat(response.id()).isEqualTo(7L);
        verify(fileStorageService).delete("/uploads/a.pdf");
        verify(tepDinhKemRepository).delete(attachment);
    }
}
