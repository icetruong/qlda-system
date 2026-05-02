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
import com.qlda.documentservice.entity.TepDinhKem;
import com.qlda.documentservice.entity.VanBan;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.mapper.DocumentMapper;
import com.qlda.documentservice.repository.LoaiVanBanRepository;
import com.qlda.documentservice.repository.TepDinhKemRepository;
import com.qlda.documentservice.repository.VanBanRepository;
import com.qlda.documentservice.security.SecurityUtils;
import com.qlda.documentservice.service.FileStorageService;
import java.time.LocalDate;
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
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentWorkflowServiceImplTest {

    @Mock
    private VanBanRepository vanBanRepository;
    @Mock
    private LoaiVanBanRepository loaiVanBanRepository;
    @Mock
    private TepDinhKemRepository tepDinhKemRepository;
    @Mock
    private DocumentMapper documentMapper;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private DocumentWorkflowServiceImpl service;

    @Test
    void createIncoming_shouldCreateDocumentWithIncomingType() {
        LoaiVanBan type = new LoaiVanBan();
        type.setId(1);
        when(loaiVanBanRepository.findById(1)).thenReturn(Optional.of(type));
        when(securityUtils.getCurrentUserId()).thenReturn(Optional.of(12L));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> {
            VanBan entity = invocation.getArgument(0);
            entity.setId(100L);
            return entity;
        });
        when(documentMapper.toDocumentSimpleResponse(any(VanBan.class)))
            .thenReturn(new DocumentResponses.DocumentSimpleResponse(100L, "01/CV/2026", "Trich yeu", 1, 0, null, null));

        DocumentResponses.DocumentSimpleResponse response = service.createIncoming(incomingRequest());

        assertThat(response.id()).isEqualTo(100L);
        ArgumentCaptor<VanBan> captor = ArgumentCaptor.forClass(VanBan.class);
        verify(vanBanRepository).save(captor.capture());
        assertThat(captor.getValue().getPhanLoaiVanBan()).isEqualTo(DocumentConstants.PHAN_LOAI_VAN_BAN_DEN);
        assertThat(captor.getValue().getTrangThai()).isEqualTo(DocumentConstants.TRANG_THAI_NHAP);
        assertThat(captor.getValue().getNguoiTaoId()).isEqualTo(12L);
    }

    @Test
    void updateIncoming_shouldThrowNotFound_whenMissingDocument() {
        when(vanBanRepository.findByIdAndDaXoaFalse(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateIncoming(9L, incomingRequest()))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    @Test
    void listIncoming_shouldReturnPagedResult() {
        VanBan vanBan = new VanBan();
        vanBan.setId(1L);
        when(vanBanRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(vanBan), PageRequest.of(0, 10), 1));
        when(documentMapper.toDocumentListItemResponse(vanBan))
            .thenReturn(new DocumentResponses.DocumentListItemResponse(1L, "01/CV/2026", "TY", null, null, null, null, null, 0));

        PageResponse<DocumentResponses.DocumentListItemResponse> response = service.listIncoming(
            "CV", 1, 2, 0, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), PageRequest.of(0, 10)
        );

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void getIncomingDetail_shouldIncludeAttachments() {
        VanBan vanBan = new VanBan();
        vanBan.setId(8L);
        TepDinhKem attachment = new TepDinhKem();
        attachment.setId(3L);
        attachment.setVanBan(vanBan);
        when(vanBanRepository.findByIdAndDaXoaFalse(8L)).thenReturn(Optional.of(vanBan));
        when(tepDinhKemRepository.findByVanBan_Id(8L)).thenReturn(List.of(attachment));
        when(documentMapper.toDocumentDetailResponse(vanBan, List.of(attachment)))
            .thenReturn(new DocumentResponses.DocumentDetailResponse(8L, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, List.of()));

        DocumentResponses.DocumentDetailResponse response = service.getIncomingDetail(8L);

        assertThat(response.id()).isEqualTo(8L);
    }

    @Test
    void createOutgoing_shouldCreateOutgoingDocument() {
        LoaiVanBan type = new LoaiVanBan();
        type.setId(3);
        when(loaiVanBanRepository.findById(3)).thenReturn(Optional.of(type));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> {
            VanBan entity = invocation.getArgument(0);
            entity.setId(13L);
            return entity;
        });
        when(documentMapper.toDocumentSimpleResponse(any(VanBan.class)))
            .thenReturn(new DocumentResponses.DocumentSimpleResponse(13L, "02/OUT/2026", "Out", 2, 0, null, null));

        DocumentResponses.DocumentSimpleResponse response = service.createOutgoing(outgoingRequest());

        assertThat(response.id()).isEqualTo(13L);
        ArgumentCaptor<VanBan> captor = ArgumentCaptor.forClass(VanBan.class);
        verify(vanBanRepository).save(captor.capture());
        assertThat(captor.getValue().getPhanLoaiVanBan()).isEqualTo(DocumentConstants.PHAN_LOAI_VAN_BAN_DI);
    }

    @Test
    void uploadOcrFile_and_saveOcr_shouldSetDaOcr() {
        VanBan vanBan = new VanBan();
        vanBan.setId(20L);
        MockMultipartFile file = new MockMultipartFile("file", "scan.pdf", "application/pdf", "abc".getBytes());
        when(vanBanRepository.findByIdAndDaXoaFalse(20L)).thenReturn(Optional.of(vanBan));
        when(fileStorageService.store(file)).thenReturn("/uploads/scan.pdf");
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponses.OcrUploadResponse uploadResponse = service.uploadOcrFile(20L, file);
        DocumentResponses.OcrSaveResponse saveResponse = service.saveOcr(
            20L,
            new DocumentRequests.OcrSaveRequest("text", 98.0)
        );

        assertThat(uploadResponse.fileUrl()).isEqualTo("/uploads/scan.pdf");
        assertThat(saveResponse.daOCR()).isTrue();
    }

    @Test
    void assignNumber_shouldThrowConflict_whenDuplicate() {
        when(vanBanRepository.existsBySoKyHieuAndDaXoaFalse("01/CV/2026")).thenReturn(true);

        assertThatThrownBy(() -> service.assignNumber(1L, new DocumentRequests.AssignNumberRequest("01/CV/2026")))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_DOCUMENT_NUMBER));
    }

    @Test
    void assignNumber_shouldUpdateNumber_whenValid() {
        VanBan vanBan = new VanBan();
        vanBan.setId(1L);
        when(vanBanRepository.existsBySoKyHieuAndDaXoaFalse("02/CV/2026")).thenReturn(false);
        when(vanBanRepository.findByIdAndDaXoaFalse(1L)).thenReturn(Optional.of(vanBan));
        when(vanBanRepository.save(vanBan)).thenReturn(vanBan);

        DocumentResponses.NumberAssignResponse response = service.assignNumber(1L, new DocumentRequests.AssignNumberRequest("02/CV/2026"));

        assertThat(response.soKyHieu()).isEqualTo("02/CV/2026");
        assertThat(vanBan.getSoKyHieu()).isEqualTo("02/CV/2026");
    }

    @Test
    void generateNumber_shouldUseTypeCodeAndCurrentCount() {
        LoaiVanBan type = new LoaiVanBan();
        type.setId(3);
        type.setMaLoaiVanBan("QD");
        VanBan existing = new VanBan();
        existing.setSoKyHieu("01/QD/2026");
        existing.setDaXoa(false);
        when(loaiVanBanRepository.findById(3)).thenReturn(Optional.of(type));
        when(vanBanRepository.findAll()).thenReturn(List.of(existing));

        DocumentResponses.NumberGenerateResponse response = service.generateNumber(
            new DocumentRequests.GenerateNumberRequest(3, null, 2026)
        );

        assertThat(response.soKyHieu()).isEqualTo("02/QD/2026");
    }

    @Test
    void checkNumber_shouldReturnExistsStatus() {
        when(vanBanRepository.existsBySoKyHieuAndDaXoaFalse("03/CV/2026")).thenReturn(true);

        DocumentResponses.NumberCheckResponse response = service.checkNumber("03/CV/2026");

        assertThat(response.exists()).isTrue();
    }

    @Test
    void versionLifecycle_shouldCreateListCompareRestoreDelete() {
        VanBan vanBan = new VanBan();
        vanBan.setId(30L);
        when(vanBanRepository.findByIdAndDaXoaFalse(30L)).thenReturn(Optional.of(vanBan));

        service.createVersion(30L, new DocumentRequests.DocumentVersionCreateRequest("v1", "old", "/f1"));
        service.createVersion(30L, new DocumentRequests.DocumentVersionCreateRequest("v2", "new", "/f2"));

        List<DocumentResponses.DocumentVersionResponse> versions = service.listVersions(30L);
        DocumentResponses.DocumentVersionCompareResponse compare = service.compareVersions(30L, "v1", "v2");
        DocumentResponses.DocumentVersionRestoreResponse restore = service.restoreVersion(
            30L, new DocumentRequests.DocumentVersionRestoreRequest("v1")
        );
        DocumentResponses.DocumentVersionDeleteResponse delete = service.deleteVersion(30L, "v1");

        assertThat(versions).hasSize(2);
        assertThat(compare.differences()).isNotEmpty();
        assertThat(restore.restoredVersion()).isEqualTo("v1");
        assertThat(delete.versionName()).isEqualTo("v1");
    }

    @Test
    void compareVersions_shouldThrowBadRequest_whenVersionMissing() {
        VanBan vanBan = new VanBan();
        vanBan.setId(31L);
        when(vanBanRepository.findByIdAndDaXoaFalse(31L)).thenReturn(Optional.of(vanBan));
        service.createVersion(31L, new DocumentRequests.DocumentVersionCreateRequest("v1", "old", "/f1"));

        assertThatThrownBy(() -> service.compareVersions(31L, "v1", "v9"))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
    }

    @Test
    void publish_sign_send_shouldUpdateAndReturnResult() {
        VanBan vanBan = new VanBan();
        vanBan.setId(40L);
        when(vanBanRepository.findByIdAndDaXoaFalse(40L)).thenReturn(Optional.of(vanBan));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponses.DigitalSignResponse sign = service.digitalSign(40L, new DocumentRequests.DigitalSignRequest(1L, "USB", null));
        DocumentResponses.PublishResponse publish = service.publish(40L, new DocumentRequests.PublishRequest(LocalDate.of(2026, 5, 1), "ok"));
        DocumentResponses.SendDocumentResponse send = service.send(
            40L,
            new DocumentRequests.SendDocumentRequest(List.of(1L, 2L), List.of(3), "EMAIL", "Noi dung")
        );

        assertThat(sign.daKySo()).isTrue();
        assertThat(publish.trangThai()).isEqualTo(DocumentConstants.TRANG_THAI_DA_PHAT_HANH);
        assertThat(send.totalReceivers()).isEqualTo(3);
    }

    private static DocumentRequests.IncomingDocumentRequest incomingRequest() {
        return new DocumentRequests.IncomingDocumentRequest(
            "01/CV/2026",
            "Trich yeu",
            1,
            "DV",
            "Nguoi ky",
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 2),
            "THUONG",
            "KHAN",
            5,
            null,
            null
        );
    }

    private static DocumentRequests.OutgoingDocumentRequest outgoingRequest() {
        return new DocumentRequests.OutgoingDocumentRequest(
            "02/OUT/2026",
            "Out",
            3,
            "Nguoi ky",
            LocalDate.of(2026, 2, 1),
            "MAT",
            "KHAN",
            8,
            null
        );
    }
}
