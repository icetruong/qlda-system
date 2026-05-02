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
import com.qlda.documentservice.entity.HoSoCongViec;
import com.qlda.documentservice.entity.VanBan;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.mapper.DocumentMapper;
import com.qlda.documentservice.repository.HoSoCongViecRepository;
import com.qlda.documentservice.repository.VanBanRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CaseFileServiceImplTest {

    @Mock
    private HoSoCongViecRepository hoSoCongViecRepository;
    @Mock
    private VanBanRepository vanBanRepository;
    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private CaseFileServiceImpl service;

    @Test
    void create_shouldThrowConflict_whenMaHoSoExists() {
        when(hoSoCongViecRepository.existsByMaHoSo("HS-1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new DocumentRequests.CaseFileCreateRequest("HS-1", "Ho so", null, null, null, 1, null)))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
    }

    @Test
    void create_shouldAttachDocument_whenDocumentExists() {
        VanBan vanBan = new VanBan();
        vanBan.setId(2L);
        when(hoSoCongViecRepository.existsByMaHoSo("HS-2")).thenReturn(false);
        when(vanBanRepository.findByIdAndDaXoaFalse(2L)).thenReturn(Optional.of(vanBan));
        when(hoSoCongViecRepository.save(any(HoSoCongViec.class))).thenAnswer(invocation -> {
            HoSoCongViec caseFile = invocation.getArgument(0);
            caseFile.setId(10L);
            return caseFile;
        });

        DocumentResponses.CaseFileSimpleResponse response = service.create(
            new DocumentRequests.CaseFileCreateRequest("HS-2", "Ho so 2", 2L, 3L, 4, 1, "note")
        );

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.maHoSo()).isEqualTo("HS-2");
    }

    @Test
    void update_shouldModifyCaseFile() {
        HoSoCongViec caseFile = new HoSoCongViec();
        caseFile.setId(1L);
        when(hoSoCongViecRepository.findById(1L)).thenReturn(Optional.of(caseFile));

        DocumentResponses.IdResponse response = service.update(
            1L,
            new DocumentRequests.CaseFileUpdateRequest("Ten moi", 8L, 9, 2, "ghi chu")
        );

        assertThat(response.id()).isEqualTo(1L);
        assertThat(caseFile.getTenHoSo()).isEqualTo("Ten moi");
        verify(hoSoCongViecRepository).save(caseFile);
    }

    @Test
    void attachDocument_shouldThrowNotFound_whenDocumentMissing() {
        HoSoCongViec caseFile = new HoSoCongViec();
        when(hoSoCongViecRepository.findById(1L)).thenReturn(Optional.of(caseFile));
        when(vanBanRepository.findByIdAndDaXoaFalse(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.attachDocument(1L, new DocumentRequests.CaseFileAttachDocumentRequest(999L)))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    @Test
    void list_shouldReturnPagedResult() {
        HoSoCongViec caseFile = new HoSoCongViec();
        caseFile.setId(1L);
        when(hoSoCongViecRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(caseFile), PageRequest.of(0, 10), 1));
        when(documentMapper.toCaseFileListItemResponse(caseFile))
            .thenReturn(new DocumentResponses.CaseFileListItemResponse(1L, "HS", "Ten", 1L, 1, 1));

        PageResponse<DocumentResponses.CaseFileListItemResponse> response = service.list("HS", null, null, null, PageRequest.of(0, 10));

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void delete_shouldSoftDeleteCaseFile() {
        HoSoCongViec caseFile = new HoSoCongViec();
        caseFile.setId(5L);
        when(hoSoCongViecRepository.findById(5L)).thenReturn(Optional.of(caseFile));

        DocumentResponses.IdResponse response = service.delete(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(caseFile.getTrangThai()).isEqualTo(DocumentConstants.TRANG_THAI_HO_SO_DA_XOA);
        assertThat(caseFile.getNgayDongHoSo()).isNotNull();
    }

    @Test
    void classify_and_searchClassification_shouldReturnGroup() {
        HoSoCongViec caseFile = new HoSoCongViec();
        caseFile.setId(3L);
        caseFile.setMaHoSo("HS-3");
        caseFile.setTenHoSo("Ten 3");
        when(hoSoCongViecRepository.findById(3L)).thenReturn(Optional.of(caseFile));
        when(hoSoCongViecRepository.findAll()).thenReturn(List.of(caseFile));

        DocumentResponses.CaseFileClassificationResponse classifyResponse = service.classify(
            3L,
            new DocumentRequests.CaseFileClassificationRequest("NHOM-A", "Ghi chu")
        );
        PageResponse<DocumentResponses.CaseFileClassificationItemResponse> response = service.searchClassification(
            "NHOM-A",
            PageRequest.of(0, 10)
        );

        assertThat(classifyResponse.nhomHoSo()).isEqualTo("NHOM-A");
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().nhomHoSo()).isEqualTo("NHOM-A");
    }
}
