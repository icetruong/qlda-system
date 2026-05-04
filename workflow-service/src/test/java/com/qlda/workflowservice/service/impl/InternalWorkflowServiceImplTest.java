package com.qlda.workflowservice.service.impl;

import com.qlda.workflowservice.client.AuthServiceClient;
import com.qlda.workflowservice.client.DocumentServiceClient;
import com.qlda.workflowservice.client.dto.DocumentDetailDto;
import com.qlda.workflowservice.dto.internal.request.InternalWorkflowStartRequest;
import com.qlda.workflowservice.dto.internal.request.InternalWorkflowSubmitApprovalRequest;
import com.qlda.workflowservice.dto.internal.request.InternalWorkflowTransferRequest;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowProgressResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStartResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStatisticsResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStatusResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowSubmitApprovalResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowTimelineItemResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowTransferResponse;
import com.qlda.workflowservice.entity.BuocQuyTrinh;
import com.qlda.workflowservice.entity.QuyTrinh;
import com.qlda.workflowservice.entity.XuLyVanBan;
import com.qlda.workflowservice.event.publisher.NotificationEventPublisher;
import com.qlda.workflowservice.exception.ApiException;
import com.qlda.workflowservice.exception.ErrorCode;
import com.qlda.workflowservice.repository.BuocQuyTrinhRepository;
import com.qlda.workflowservice.repository.XuLyVanBanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalWorkflowServiceImplTest {

    @Mock
    private XuLyVanBanRepository xuLyVanBanRepository;
    @Mock
    private BuocQuyTrinhRepository buocQuyTrinhRepository;
    @Mock
    private DocumentServiceClient documentServiceClient;
    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    @InjectMocks
    private InternalWorkflowServiceImpl internalWorkflowService;

    private BuocQuyTrinh step;
    private XuLyVanBan processing;

    @BeforeEach
    void setUp() {
        QuyTrinh workflow = QuyTrinh.builder().id(1).tenQuyTrinh("Quy trinh").build();
        step = BuocQuyTrinh.builder().id(11L).quyTrinh(workflow).tenBuoc("Buoc dau tien").thuTuBuoc(1).build();
        processing = XuLyVanBan.builder()
                .id(20L)
                .vanBanId(1L)
                .buocQuyTrinh(step)
                .nguoiGuiId(100L)
                .nguoiNhanId(200L)
                .donViXuLyId(1)
                .hanhDongXuLy("TRANSFER")
                .ngayNhan(LocalDateTime.now().minusHours(1))
                .hanXuLy(LocalDateTime.now().plusHours(1))
                .trangThaiXuLy(1)
                .tyLeHoanThanh(60)
                .build();
    }

    @Test
    void startWorkflow_success() {
        when(documentServiceClient.getDocumentById(1L)).thenReturn(new DocumentDetailDto(1L, "SKH", "TY", 1, "Cong van", "INCOMING", 1, 2L, LocalDateTime.now().plusDays(1), 1, false, false));
        when(buocQuyTrinhRepository.findFirstByQuyTrinh_IdOrderByThuTuBuocAsc(1)).thenReturn(Optional.of(step));
        when(xuLyVanBanRepository.save(any())).thenAnswer(invocation -> {
            XuLyVanBan saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        InternalWorkflowStartResponse response = internalWorkflowService.startWorkflow(1L,
                new InternalWorkflowStartRequest(1, 100L, 1, "INCOMING", LocalDateTime.now().plusDays(1)));

        assertEquals(10L, response.processingId());
        assertEquals(1, response.trangThaiXuLy());
    }

    @Test
    void transferWorkflow_success_andIntegrationFlow() {
        when(documentServiceClient.getDocumentById(1L)).thenReturn(new DocumentDetailDto(1L, "SKH", "TY", 1, "Cong van", "INCOMING", 1, 2L, LocalDateTime.now().plusDays(1), 1, false, false));
        when(buocQuyTrinhRepository.findById(11L)).thenReturn(Optional.of(step));
        when(xuLyVanBanRepository.save(any())).thenAnswer(invocation -> {
            XuLyVanBan saved = invocation.getArgument(0);
            saved.setId(55L);
            return saved;
        });

        InternalWorkflowTransferResponse response = internalWorkflowService.transferWorkflow(1L,
                new InternalWorkflowTransferRequest(100L, 200L, 1, 11L, "Chuyen xu ly", LocalDateTime.now().plusDays(1)));

        assertEquals(55L, response.processingId());
        verify(authServiceClient).validateUsers(List.of(100L, 200L));
        verify(authServiceClient).validateUnits(List.of(1));
        verify(documentServiceClient).updateDocumentAssignee(eq(1L), any());
        verify(notificationEventPublisher).publish(any());
    }

    @Test
    void submitApproval_success_andIntegrationFlow() {
        when(documentServiceClient.getDocumentById(1L)).thenReturn(new DocumentDetailDto(1L, "SKH", "TY", 1, "Cong van", "INCOMING", 1, 2L, LocalDateTime.now().plusDays(1), 1, false, false));
        when(xuLyVanBanRepository.save(any())).thenAnswer(invocation -> {
            XuLyVanBan saved = invocation.getArgument(0);
            saved.setId(66L);
            return saved;
        });

        InternalWorkflowSubmitApprovalResponse response = internalWorkflowService.submitApproval(1L,
                new InternalWorkflowSubmitApprovalRequest(200L, 500L, "Trinh duyet"));

        assertEquals(66L, response.processingId());
        verify(authServiceClient).validateUsers(List.of(200L, 500L));
        verify(documentServiceClient).updateDocumentWorkflowStatus(eq(1L), any());
        verify(notificationEventPublisher).publish(any());
    }

    @Test
    void getStatus_success() {
        when(xuLyVanBanRepository.findTopByVanBanIdOrderByIdDesc(1L)).thenReturn(Optional.of(processing));

        InternalWorkflowStatusResponse response = internalWorkflowService.getStatus(1L);

        assertEquals(1L, response.documentId());
        assertEquals(60, response.tyLeHoanThanh());
    }

    @Test
    void getTimeline_success() {
        when(xuLyVanBanRepository.findByVanBanIdOrderByIdAsc(1L)).thenReturn(List.of(processing));

        List<InternalWorkflowTimelineItemResponse> response = internalWorkflowService.getTimeline(1L);

        assertEquals(1, response.size());
        assertEquals(20L, response.getFirst().processingId());
    }

    @Test
    void statistics_success() {
        when(xuLyVanBanRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(processing));

        InternalWorkflowStatisticsResponse response = internalWorkflowService.getStatistics(
                LocalDate.now().minusDays(1), LocalDate.now(), 1);

        assertEquals(1, response.totalTasks());
    }

    @Test
    void progress_success() {
        when(xuLyVanBanRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(processing));

        InternalWorkflowProgressResponse response = internalWorkflowService.getProgress(
                LocalDate.now().minusDays(1), LocalDate.now(), 1, 200L);

        assertEquals(1, response.items().size());
    }

    @Test
    void slaViolations_success() {
        XuLyVanBan overdue = XuLyVanBan.builder()
                .id(1L)
                .vanBanId(1L)
                .nguoiNhanId(2L)
                .hanXuLy(LocalDateTime.now().minusHours(3))
                .ngayHoanThanh(null)
                .build();
        when(xuLyVanBanRepository.findAll(any(Specification.class))).thenReturn(List.of(overdue));

        var response = internalWorkflowService.getSlaViolations(LocalDate.now().minusDays(1), LocalDate.now(), 1);

        assertEquals(1, response.size());
        assertTrue(response.getFirst().soGioTre() >= 0);
    }

    @Test
    void startWorkflow_documentNotFound_shouldThrow() {
        when(documentServiceClient.getDocumentById(1L))
                .thenThrow(new ApiException(ErrorCode.DOCUMENT_NOT_FOUND, HttpStatus.NOT_FOUND, "Document not found"));

        ApiException exception = assertThrows(ApiException.class, () -> internalWorkflowService.startWorkflow(1L,
                new InternalWorkflowStartRequest(1, 100L, 1, "INCOMING", LocalDateTime.now().plusDays(1))));

        assertEquals(ErrorCode.DOCUMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void transferWorkflow_authValidateFail_shouldThrow() {
        when(documentServiceClient.getDocumentById(1L)).thenReturn(new DocumentDetailDto(1L, "SKH", "TY", 1, "Cong van", "INCOMING", 1, 2L, LocalDateTime.now().plusDays(1), 1, false, false));
        doThrow(new ApiException(ErrorCode.USER_NOT_FOUND, HttpStatus.BAD_REQUEST, "Invalid users"))
                .when(authServiceClient).validateUsers(List.of(100L, 200L));

        ApiException exception = assertThrows(ApiException.class, () -> internalWorkflowService.transferWorkflow(1L,
                new InternalWorkflowTransferRequest(100L, 200L, 1, 11L, "Y", LocalDateTime.now().plusDays(1))));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
}
