package com.qlda.documentservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.qlda.documentservice.common.DocumentConstants;
import com.qlda.documentservice.dto.internal.InternalDocumentRequests;
import com.qlda.documentservice.dto.internal.InternalDocumentResponses;
import com.qlda.documentservice.entity.LoaiVanBan;
import com.qlda.documentservice.entity.TepDinhKem;
import com.qlda.documentservice.entity.VanBan;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.repository.TepDinhKemRepository;
import com.qlda.documentservice.repository.VanBanRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternalDocumentServiceImplTest {

    @Mock
    private VanBanRepository vanBanRepository;
    @Mock
    private TepDinhKemRepository tepDinhKemRepository;

    @InjectMocks
    private InternalDocumentServiceImpl service;

    @Test
    void getInternalDocument_shouldReturnSuccess() {
        VanBan vanBan = createDocument(1L);
        when(vanBanRepository.findByIdAndDaXoaFalse(1L)).thenReturn(Optional.of(vanBan));

        InternalDocumentResponses.InternalDocumentResponse response = service.getInternalDocument(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.documentType()).isEqualTo("INCOMING");
    }

    @Test
    void getInternalDocument_shouldThrowNotFound() {
        when(vanBanRepository.findByIdAndDaXoaFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getInternalDocument(99L))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    @Test
    void getContent_shouldReturnSuccess() {
        VanBan vanBan = createDocument(2L);
        when(vanBanRepository.findByIdAndDaXoaFalse(2L)).thenReturn(Optional.of(vanBan));

        InternalDocumentResponses.InternalDocumentContentResponse response = service.getDocumentContent(2L);

        assertThat(response.documentId()).isEqualTo(2L);
        assertThat(response.noiDung()).isNotBlank();
    }

    @Test
    void getAttachments_shouldReturnSuccess() {
        VanBan vanBan = createDocument(3L);
        TepDinhKem tep = new TepDinhKem();
        tep.setId(10L);
        tep.setVanBan(vanBan);
        tep.setTenTep("a.pdf");
        tep.setDuongDanTep("/uploads/a.pdf");
        tep.setLoaiTep("pdf");
        tep.setKichThuoc(100L);
        when(vanBanRepository.findByIdAndDaXoaFalse(3L)).thenReturn(Optional.of(vanBan));
        when(tepDinhKemRepository.findByVanBan_Id(3L)).thenReturn(List.of(tep));

        List<InternalDocumentResponses.InternalAttachmentResponse> response = service.getDocumentAttachments(3L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(10L);
    }

    @Test
    void updateStatus_shouldReturnSuccess() {
        VanBan vanBan = createDocument(4L);
        when(vanBanRepository.findByIdAndDaXoaFalse(4L)).thenReturn(Optional.of(vanBan));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InternalDocumentResponses.UpdateStatusResponse response = service.updateDocumentStatus(
            4L,
            new InternalDocumentRequests.UpdateStatusRequest(3, "done", "workflow-service")
        );

        assertThat(response.trangThai()).isEqualTo(3);
        assertThat(vanBan.getTrangThai()).isEqualTo(3);
    }

    @Test
    void updateOcrStatus_shouldReturnSuccess() {
        VanBan vanBan = createDocument(5L);
        when(vanBanRepository.findByIdAndDaXoaFalse(5L)).thenReturn(Optional.of(vanBan));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InternalDocumentResponses.UpdateOcrStatusResponse response = service.updateOcrStatus(
            5L,
            new InternalDocumentRequests.UpdateOcrStatusRequest(true)
        );

        assertThat(response.daOCR()).isTrue();
        assertThat(vanBan.getDaOCR()).isTrue();
    }

    @Test
    void statistics_shouldReturnSuccess() {
        VanBan incoming = createDocument(6L);
        incoming.setPhanLoaiVanBan(DocumentConstants.PHAN_LOAI_VAN_BAN_DEN);
        VanBan outgoing = createDocument(7L);
        outgoing.setPhanLoaiVanBan(DocumentConstants.PHAN_LOAI_VAN_BAN_DI);
        when(vanBanRepository.findAll()).thenReturn(List.of(incoming, outgoing));

        InternalDocumentResponses.InternalDocumentStatisticsResponse response = service.getStatistics(
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 30),
            1,
            "status"
        );

        assertThat(response.totalDocuments()).isEqualTo(2);
        assertThat(response.incomingDocuments()).isEqualTo(1);
    }

    @Test
    void overdue_shouldReturnSuccess() {
        VanBan overdue = createDocument(8L);
        overdue.setHanXuLy(LocalDateTime.now().minusDays(5));
        overdue.setTrangThai(DocumentConstants.TRANG_THAI_DANG_XU_LY);
        when(vanBanRepository.findAll()).thenReturn(List.of(overdue));

        InternalDocumentResponses.InternalOverdueDocumentsResponse response = service.getOverdueDocuments(1, 2L, 0, 10);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().soNgayTre()).isGreaterThanOrEqualTo(1);
    }

    private VanBan createDocument(Long id) {
        VanBan vanBan = new VanBan();
        vanBan.setId(id);
        vanBan.setSoKyHieu("123/CV-ABC");
        vanBan.setTrichYeu("Trich yeu");
        LoaiVanBan loaiVanBan = new LoaiVanBan();
        loaiVanBan.setId(1);
        loaiVanBan.setTenLoaiVanBan("Cong van");
        vanBan.setLoaiVanBan(loaiVanBan);
        vanBan.setPhanLoaiVanBan(DocumentConstants.PHAN_LOAI_VAN_BAN_DEN);
        vanBan.setDonViChuTriId(1);
        vanBan.setNguoiTaoId(2L);
        vanBan.setHanXuLy(LocalDateTime.now().plusDays(2));
        vanBan.setTrangThai(DocumentConstants.TRANG_THAI_DANG_XU_LY);
        vanBan.setDaOCR(false);
        vanBan.setDaKySo(false);
        vanBan.setDaXoa(false);
        vanBan.setNgayTao(LocalDateTime.of(2026, 4, 20, 9, 0));
        return vanBan;
    }
}
