package com.qlda.documentservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.entity.LoaiVanBan;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.mapper.DocumentMapper;
import com.qlda.documentservice.repository.LoaiVanBanRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentTypeServiceImplTest {

    @Mock
    private LoaiVanBanRepository loaiVanBanRepository;

    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentTypeServiceImpl service;

    @Test
    void create_shouldThrowConflict_whenCodeExists() {
        DocumentRequests.DocumentTypeCreateRequest request = new DocumentRequests.DocumentTypeCreateRequest(
            "CV", "Cong van", null, true
        );
        when(loaiVanBanRepository.existsByMaLoaiVanBan("CV")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> {
                BusinessException businessException = (BusinessException) ex;
                assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
            });
    }

    @Test
    void create_shouldSaveAndMap_whenValid() {
        DocumentRequests.DocumentTypeCreateRequest request = new DocumentRequests.DocumentTypeCreateRequest(
            "CV", "Cong van", "Mo ta", null
        );
        LoaiVanBan saved = new LoaiVanBan();
        saved.setId(1);
        saved.setMaLoaiVanBan("CV");
        saved.setTenLoaiVanBan("Cong van");
        saved.setSuDung(true);
        DocumentResponses.DocumentTypeResponse mapped = new DocumentResponses.DocumentTypeResponse(1, "CV", "Cong van", "Mo ta", true);

        when(loaiVanBanRepository.existsByMaLoaiVanBan("CV")).thenReturn(false);
        when(loaiVanBanRepository.save(org.mockito.ArgumentMatchers.any(LoaiVanBan.class))).thenReturn(saved);
        when(documentMapper.toDocumentTypeResponse(saved)).thenReturn(mapped);

        DocumentResponses.DocumentTypeResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.suDung()).isTrue();
    }

    @Test
    void update_shouldThrowNotFound_whenMissing() {
        when(loaiVanBanRepository.findById(99)).thenReturn(Optional.empty());
        DocumentRequests.DocumentTypeUpdateRequest request = new DocumentRequests.DocumentTypeUpdateRequest("Ten moi", "Mo ta", true);

        assertThatThrownBy(() -> service.update(99, request))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_TYPE_NOT_FOUND));
    }

    @Test
    void update_shouldModifyFields_whenFound() {
        LoaiVanBan existing = new LoaiVanBan();
        existing.setId(1);
        existing.setTenLoaiVanBan("Old");
        existing.setSuDung(false);
        when(loaiVanBanRepository.findById(1)).thenReturn(Optional.of(existing));

        DocumentResponses.IdResponse response = service.update(1, new DocumentRequests.DocumentTypeUpdateRequest("New", "Desc", true));

        assertThat(response.id()).isEqualTo(1);
        assertThat(existing.getTenLoaiVanBan()).isEqualTo("New");
        assertThat(existing.getSuDung()).isTrue();
        verify(loaiVanBanRepository).save(existing);
    }

    @Test
    void list_shouldFilterByKeywordAndUsage() {
        LoaiVanBan a = new LoaiVanBan();
        a.setId(1);
        a.setMaLoaiVanBan("CV");
        a.setTenLoaiVanBan("Cong Van");
        a.setSuDung(true);
        LoaiVanBan b = new LoaiVanBan();
        b.setId(2);
        b.setMaLoaiVanBan("TB");
        b.setTenLoaiVanBan("Thong Bao");
        b.setSuDung(false);
        when(loaiVanBanRepository.findAll()).thenReturn(List.of(a, b));
        when(documentMapper.toDocumentTypeResponse(a)).thenReturn(new DocumentResponses.DocumentTypeResponse(1, "CV", "Cong Van", null, true));

        List<DocumentResponses.DocumentTypeResponse> responses = service.list("cong", true);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(1);
    }

    @Test
    void detail_shouldReturnMappedType() {
        LoaiVanBan type = new LoaiVanBan();
        type.setId(5);
        when(loaiVanBanRepository.findById(5)).thenReturn(Optional.of(type));
        when(documentMapper.toDocumentTypeResponse(type)).thenReturn(new DocumentResponses.DocumentTypeResponse(5, "CV", "Cong Van", null, true));

        DocumentResponses.DocumentTypeResponse response = service.detail(5);

        assertThat(response.id()).isEqualTo(5);
    }

    @Test
    void delete_shouldSoftDisableType() {
        LoaiVanBan type = new LoaiVanBan();
        type.setId(7);
        type.setSuDung(true);
        when(loaiVanBanRepository.findById(7)).thenReturn(Optional.of(type));

        DocumentResponses.IdResponse response = service.delete(7);

        assertThat(response.id()).isEqualTo(7);
        assertThat(type.getSuDung()).isFalse();
        verify(loaiVanBanRepository).save(type);
    }
}
