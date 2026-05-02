package com.qlda.documentservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qlda.documentservice.common.DocumentConstants;
import com.qlda.documentservice.common.PageResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.entity.LoaiVanBan;
import com.qlda.documentservice.entity.TemplateVanBan;
import com.qlda.documentservice.entity.VanBan;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.mapper.DocumentMapper;
import com.qlda.documentservice.repository.LoaiVanBanRepository;
import com.qlda.documentservice.repository.TemplateVanBanRepository;
import com.qlda.documentservice.repository.VanBanRepository;
import com.qlda.documentservice.security.SecurityUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TemplateServiceImplTest {

    @Mock
    private TemplateVanBanRepository templateVanBanRepository;
    @Mock
    private LoaiVanBanRepository loaiVanBanRepository;
    @Mock
    private VanBanRepository vanBanRepository;
    @Mock
    private DocumentMapper documentMapper;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private TemplateServiceImpl service;

    @Test
    void create_shouldThrowConflict_whenTemplateCodeExists() {
        DocumentRequests.TemplateCreateRequest request = new DocumentRequests.TemplateCreateRequest(
            "TMP-01", "Template", null, null, null, true
        );
        when(templateVanBanRepository.existsByMaTemplate("TMP-01")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
    }

    @Test
    void create_shouldPersistTemplate_whenValid() {
        LoaiVanBan type = new LoaiVanBan();
        type.setId(1);
        DocumentRequests.TemplateCreateRequest request = new DocumentRequests.TemplateCreateRequest(
            "TMP-01", "Template", 1, "Hi {{name}}", "a.docx", null
        );
        when(templateVanBanRepository.existsByMaTemplate("TMP-01")).thenReturn(false);
        when(loaiVanBanRepository.findById(1)).thenReturn(Optional.of(type));
        when(securityUtils.getCurrentUserId()).thenReturn(Optional.of(99L));
        when(templateVanBanRepository.save(any(TemplateVanBan.class))).thenAnswer(invocation -> {
            TemplateVanBan entity = invocation.getArgument(0);
            entity.setId(10);
            return entity;
        });

        DocumentResponses.TemplateSimpleResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(10);
        assertThat(response.maTemplate()).isEqualTo("TMP-01");
    }

    @Test
    void update_shouldThrowNotFound_whenTemplateMissing() {
        when(templateVanBanRepository.findById(123)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(123, new DocumentRequests.TemplateUpdateRequest("T", null, null, null, true)))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.TEMPLATE_NOT_FOUND));
    }

    @Test
    void delete_shouldSetSuDungFalse() {
        TemplateVanBan template = new TemplateVanBan();
        template.setId(5);
        template.setSuDung(true);
        when(templateVanBanRepository.findById(5)).thenReturn(Optional.of(template));

        DocumentResponses.IdResponse response = service.delete(5);

        assertThat(response.id()).isEqualTo(5);
        assertThat(template.getSuDung()).isFalse();
    }

    @Test
    void list_shouldReturnPageResponse() {
        TemplateVanBan entity = new TemplateVanBan();
        entity.setId(1);
        when(templateVanBanRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));
        when(documentMapper.toTemplateListItemResponse(entity))
            .thenReturn(new DocumentResponses.TemplateListItemResponse(1, "TMP", "Template", null, null, true));

        PageResponse<DocumentResponses.TemplateListItemResponse> response = service.list("tmp", null, true, PageRequest.of(0, 10));

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void apply_shouldReplacePlaceholders() {
        TemplateVanBan template = new TemplateVanBan();
        template.setNoiDungMau("Xin chao {{name}} - ${unit}");
        when(templateVanBanRepository.findById(1)).thenReturn(Optional.of(template));

        DocumentResponses.ApplyTemplateResponse response = service.apply(
            1,
            new DocumentRequests.ApplyTemplateRequest(11L, Map.of("name", "An", "unit", "IT"))
        );

        assertThat(response.content()).contains("An").contains("IT");
    }

    @Test
    void createFromTemplate_shouldCreateDraftDocument() {
        TemplateVanBan template = new TemplateVanBan();
        template.setId(2);
        template.setNoiDungMau("Body {{x}}");
        LoaiVanBan type = new LoaiVanBan();
        type.setId(7);
        when(templateVanBanRepository.findById(2)).thenReturn(Optional.of(template));
        when(loaiVanBanRepository.findById(7)).thenReturn(Optional.of(type));
        when(securityUtils.getCurrentUserId()).thenReturn(Optional.of(100L));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> {
            VanBan vanBan = invocation.getArgument(0);
            vanBan.setId(999L);
            return vanBan;
        });

        DocumentResponses.CreateFromTemplateResponse response = service.createFromTemplate(
            new DocumentRequests.CreateFromTemplateRequest(2, "Trich yeu", 7, 3, Map.of("x", "1"))
        );

        assertThat(response.documentId()).isEqualTo(999L);
        assertThat(response.templateId()).isEqualTo(2);
        assertThat(response.trangThai()).isEqualTo(DocumentConstants.TRANG_THAI_NHAP);

        ArgumentCaptor<VanBan> captor = ArgumentCaptor.forClass(VanBan.class);
        verify(vanBanRepository).save(captor.capture());
        assertThat(captor.getValue().getPhanLoaiVanBan()).isEqualTo(DocumentConstants.PHAN_LOAI_VAN_BAN_NHAP);
    }

    @Test
    void createFromTemplate_shouldThrowNotFound_whenDocumentTypeMissing() {
        TemplateVanBan template = new TemplateVanBan();
        when(templateVanBanRepository.findById(3)).thenReturn(Optional.of(template));
        when(loaiVanBanRepository.findById(77)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createFromTemplate(
            new DocumentRequests.CreateFromTemplateRequest(3, "Test", 77, null, Map.of())
        )).isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_TYPE_NOT_FOUND));
    }
}
