package com.qlda.workflowservice.service.impl;

import com.qlda.workflowservice.common.PageResponse;
import com.qlda.workflowservice.dto.request.*;
import com.qlda.workflowservice.dto.response.*;
import com.qlda.workflowservice.entity.BuocQuyTrinh;
import com.qlda.workflowservice.entity.QuyTrinh;
import com.qlda.workflowservice.entity.XuLyVanBan;
import com.qlda.workflowservice.exception.ApiException;
import com.qlda.workflowservice.exception.ErrorCode;
import com.qlda.workflowservice.repository.BuocQuyTrinhRepository;
import com.qlda.workflowservice.repository.QuyTrinhRepository;
import com.qlda.workflowservice.repository.XuLyVanBanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowApiServiceImplTest {

    @Mock
    private QuyTrinhRepository quyTrinhRepository;
    @Mock
    private BuocQuyTrinhRepository buocQuyTrinhRepository;
    @Mock
    private XuLyVanBanRepository xuLyVanBanRepository;

    @InjectMocks
    private WorkflowApiServiceImpl service;

    private QuyTrinh workflow;
    private BuocQuyTrinh step;
    private XuLyVanBan processing;

    @BeforeEach
    void setUp() {
        workflow = QuyTrinh.builder()
                .id(1)
                .maQuyTrinh("QT_01")
                .tenQuyTrinh("Quy trinh 1")
                .loaiVanBanId(10)
                .moTa("Mo ta")
                .soBuoc(1)
                .suDung(true)
                .build();

        step = BuocQuyTrinh.builder()
                .id(11L)
                .quyTrinh(workflow)
                .tenBuoc("Buoc 1")
                .thuTuBuoc(1)
                .vaiTroXuLy("ROLE_A")
                .thoiGianXuLy(24)
                .batBuocPheDuyet(true)
                .ghiChu("Ghi chu")
                .build();

        processing = XuLyVanBan.builder()
                .id(100L)
                .vanBanId(999L)
                .buocQuyTrinh(step)
                .nguoiGuiId(1L)
                .nguoiNhanId(2L)
                .donViXuLyId(5)
                .hanhDongXuLy("TRANSFER")
                .yKienXuLy("YK")
                .ngayNhan(LocalDateTime.now().minusHours(1))
                .hanXuLy(LocalDateTime.now().plusHours(3))
                .trangThaiXuLy(1)
                .tyLeHoanThanh(20)
                .build();
    }

    @Test
    void createWorkflow_success() {
        WorkflowCreateRequest request = new WorkflowCreateRequest("MA_01", "Ten", 3, "Mo ta", null);
        when(quyTrinhRepository.existsByMaQuyTrinh("MA_01")).thenReturn(false);
        when(quyTrinhRepository.save(any(QuyTrinh.class))).thenAnswer(invocation -> {
            QuyTrinh q = invocation.getArgument(0);
            q.setId(9);
            return q;
        });

        WorkflowSummaryResponse response = service.createWorkflow(request);

        assertEquals(9, response.id());
        assertEquals("MA_01", response.maQuyTrinh());
        assertTrue(response.suDung());
        verify(quyTrinhRepository).save(any(QuyTrinh.class));
    }

    @Test
    void createWorkflow_duplicateCode_throwsBadRequest() {
        when(quyTrinhRepository.existsByMaQuyTrinh("MA_01")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class,
                () -> service.createWorkflow(new WorkflowCreateRequest("MA_01", "Ten", 1, null, true)));

        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void updateWorkflow_success() {
        when(quyTrinhRepository.findById(1)).thenReturn(Optional.of(workflow));

        IdResponse response = service.updateWorkflow(1, new WorkflowUpdateRequest("Updated", 20, "Updated mo ta", false));

        assertEquals(1, response.id());
        assertEquals("Updated", workflow.getTenQuyTrinh());
        assertFalse(workflow.getSuDung());
        verify(quyTrinhRepository).save(workflow);
    }

    @Test
    void updateWorkflow_notFound() {
        when(quyTrinhRepository.findById(1)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> service.updateWorkflow(1, new WorkflowUpdateRequest("Updated", 20, "M", true)));

        assertEquals(ErrorCode.WORKFLOW_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getWorkflows_withPagingAndFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<QuyTrinh> page = new PageImpl<>(List.of(workflow), pageable, 1);
        when(quyTrinhRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResponse<WorkflowSummaryResponse> response = service.getWorkflows("qt", 10, true, pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals("QT_01", response.getContent().getFirst().maQuyTrinh());
    }

    @Test
    void getWorkflowDetail_success() {
        when(quyTrinhRepository.findById(1)).thenReturn(Optional.of(workflow));
        when(buocQuyTrinhRepository.findByQuyTrinh_IdOrderByThuTuBuocAsc(1)).thenReturn(List.of(step));

        WorkflowDetailResponse response = service.getWorkflowDetail(1);

        assertEquals(1, response.id());
        assertEquals(1, response.steps().size());
        assertEquals("Buoc 1", response.steps().getFirst().tenBuoc());
    }

    @Test
    void deleteWorkflow_softDelete() {
        when(quyTrinhRepository.findById(1)).thenReturn(Optional.of(workflow));

        IdResponse response = service.deleteWorkflow(1);

        assertEquals(1, response.id());
        assertFalse(workflow.getSuDung());
        verify(quyTrinhRepository).save(workflow);
    }

    @Test
    void createWorkflowStep_success_andSyncSoBuoc() {
        when(quyTrinhRepository.findById(1)).thenReturn(Optional.of(workflow));
        when(buocQuyTrinhRepository.existsByQuyTrinh_IdAndThuTuBuoc(1, 2)).thenReturn(false);
        when(buocQuyTrinhRepository.save(any(BuocQuyTrinh.class))).thenAnswer(i -> {
            BuocQuyTrinh s = i.getArgument(0);
            s.setId(77L);
            return s;
        });
        when(buocQuyTrinhRepository.countByQuyTrinh_Id(1)).thenReturn(2L);

        WorkflowStepResponse response = service.createWorkflowStep(1,
                new WorkflowStepCreateRequest("Buoc 2", 2, "ROLE_B", 12, true, "note"));

        assertEquals(77L, response.id());
        assertEquals(2, workflow.getSoBuoc());
        verify(quyTrinhRepository, atLeastOnce()).save(workflow);
    }

    @Test
    void createWorkflowStep_duplicateOrder_throwsInvalidRequest() {
        when(quyTrinhRepository.findById(1)).thenReturn(Optional.of(workflow));
        when(buocQuyTrinhRepository.existsByQuyTrinh_IdAndThuTuBuoc(1, 1)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> service.createWorkflowStep(1,
                new WorkflowStepCreateRequest("Buoc 1", 1, "ROLE_A", 8, true, null)));

        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    void updateWorkflowStep_success() {
        BuocQuyTrinh anotherStep = BuocQuyTrinh.builder().id(99L).quyTrinh(workflow).thuTuBuoc(3).build();
        when(quyTrinhRepository.findById(1)).thenReturn(Optional.of(workflow));
        when(buocQuyTrinhRepository.findByIdAndQuyTrinh_Id(11L, 1)).thenReturn(Optional.of(step));
        when(buocQuyTrinhRepository.findByQuyTrinh_IdOrderByThuTuBuocAsc(1)).thenReturn(List.of(step, anotherStep));

        IdResponse response = service.updateWorkflowStep(1, 11L,
                new WorkflowStepUpdateRequest("Updated", 2, "ROLE_U", 9, false, "updated"));

        assertEquals(11L, response.id());
        assertEquals("Updated", step.getTenBuoc());
        assertEquals(2, step.getThuTuBuoc());
        verify(buocQuyTrinhRepository).save(step);
    }

    @Test
    void deleteWorkflowStep_success() {
        when(quyTrinhRepository.findById(1)).thenReturn(Optional.of(workflow));
        when(buocQuyTrinhRepository.findByIdAndQuyTrinh_Id(11L, 1)).thenReturn(Optional.of(step));
        when(buocQuyTrinhRepository.countByQuyTrinh_Id(1)).thenReturn(0L);

        IdResponse response = service.deleteWorkflowStep(1, 11L);

        assertEquals(11L, response.id());
        verify(buocQuyTrinhRepository).delete(step);
    }

    @Test
    void getPendingApprovals_success() {
        Pageable pageable = PageRequest.of(0, 5);
        PageImpl<XuLyVanBan> page = new PageImpl<>(List.of(processing), pageable, 1);
        when(xuLyVanBanRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResponse<PendingApprovalResponse> response = service.getPendingApprovals(2L, "transfer",
                LocalDate.now().minusDays(1), LocalDate.now(), pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals(processing.getId(), response.getContent().getFirst().processingId());
    }

    @Test
    void commentApproval_success() {
        when(xuLyVanBanRepository.findById(100L)).thenReturn(Optional.of(processing));

        ApprovalActionResponse response = service.commentApproval(100L, new ApprovalCommentRequest("Need review"));

        assertEquals("Need review", response.noiDung());
        assertEquals("Need review", processing.getYKienXuLy());
    }

    @Test
    void approveDocument_success() {
        when(xuLyVanBanRepository.findById(100L)).thenReturn(Optional.of(processing));

        ApprovalActionResponse response = service.approveDocument(100L, new ApprovalApproveRequest("OK", true));

        assertEquals(2, response.trangThaiXuLy());
        assertEquals(100, processing.getTyLeHoanThanh());
        assertNotNull(processing.getNgayHoanThanh());
    }

    @Test
    void rejectDocument_success() {
        when(xuLyVanBanRepository.findById(100L)).thenReturn(Optional.of(processing));

        ApprovalActionResponse response = service.rejectDocument(100L, new ApprovalRejectRequest("Khong hop le"));

        assertEquals(3, response.trangThaiXuLy());
        assertEquals("Khong hop le", processing.getYKienXuLy());
    }

    @Test
    void createAndCancelDelegation_success() {
        DelegationResponse created = service.createDelegation(
                new DelegationCreateRequest(1L, 2L, LocalDate.now(), LocalDate.now().plusDays(3), "APPROVE", "N"));

        assertNotNull(created.id());

        IdResponse canceled = service.cancelDelegation(created.id());
        assertEquals(created.id(), canceled.id());
    }

    @Test
    void cancelDelegation_notFound() {
        ApiException ex = assertThrows(ApiException.class, () -> service.cancelDelegation(999L));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    void getDocumentStatus_overdueTrue() {
        processing.setHanXuLy(LocalDateTime.now().minusHours(1));
        processing.setNgayHoanThanh(null);
        when(xuLyVanBanRepository.findTopByVanBanIdOrderByIdDesc(999L)).thenReturn(Optional.of(processing));

        DocumentStatusResponse response = service.getDocumentStatus(999L);

        assertTrue(response.isOverdue());
        assertEquals("Buoc 1", response.currentStep());
    }

    @Test
    void getDocumentTimeline_sortedByNgayNhanThenId() {
        XuLyVanBan first = XuLyVanBan.builder().id(1L).vanBanId(999L).ngayNhan(LocalDateTime.now().minusHours(2)).trangThaiXuLy(2).build();
        XuLyVanBan second = XuLyVanBan.builder().id(2L).vanBanId(999L).ngayNhan(LocalDateTime.now().minusHours(1)).trangThaiXuLy(1).build();
        when(xuLyVanBanRepository.findByVanBanIdOrderByIdAsc(999L)).thenReturn(List.of(second, first));

        List<TimelineItemResponse> timeline = service.getDocumentTimeline(999L);

        assertEquals(1L, timeline.get(0).processingId());
        assertEquals(2L, timeline.get(1).processingId());
    }

    @Test
    void transferDocument_success() {
        when(buocQuyTrinhRepository.findById(11L)).thenReturn(Optional.of(step));
        when(xuLyVanBanRepository.save(any(XuLyVanBan.class))).thenAnswer(i -> {
            XuLyVanBan saved = i.getArgument(0);
            saved.setId(555L);
            return saved;
        });

        TransferResponse response = service.transferDocument(999L,
                new TransferDocumentRequest(1L, 2L, 3, 11L, "TRANSFER", "Y kien", LocalDateTime.now().plusHours(3)));

        assertEquals(555L, response.processingId());
        assertEquals(1, response.trangThaiXuLy());
    }

    @Test
    void receiveDocument_setsNgayNhanWhenNull() {
        processing.setNgayNhan(null);
        when(xuLyVanBanRepository.findById(100L)).thenReturn(Optional.of(processing));

        ReceiveResponse response = service.receiveDocument(100L, new ReceiveProcessingRequest(9L, "da nhan"));

        assertEquals(9L, processing.getNguoiNhanId());
        assertNotNull(response.receivedAt());
    }

    @Test
    void completeProcessing_whenPercentNull_defaults100() {
        when(xuLyVanBanRepository.findById(100L)).thenReturn(Optional.of(processing));

        CompleteResponse response = service.completeProcessing(100L, new CompleteProcessingRequest("xong", "/file", null));

        assertEquals(100, response.tyLeHoanThanh());
        assertEquals(2, response.trangThaiXuLy());
        assertNotNull(response.ngayHoanThanh());
    }

    @Test
    void checkDeadlines_countsFromRepository() {
        when(xuLyVanBanRepository.count(any(Specification.class))).thenReturn(5L, 2L);

        DeadlineCheckResponse response = service.checkDeadlines(
                new ReminderCheckDeadlineRequest(LocalDateTime.now(), 24));

        assertEquals(5L, response.totalNearDeadline());
        assertEquals(2L, response.totalOverdue());
    }

    @Test
    void sendReminders_returnsInputSize() {
        ReminderSendResponse response = service.sendReminders(
                new ReminderSendRequest(List.of(1L, 2L, 3L), List.of("SYSTEM", "EMAIL"), "Nhac viec"));

        assertEquals(3, response.totalSent());
        assertEquals(List.of("SYSTEM", "EMAIL"), response.kenhGui());
    }

    @Test
    void updateStepSla_success() {
        when(buocQuyTrinhRepository.findByIdAndQuyTrinh_Id(11L, 1)).thenReturn(Optional.of(step));

        SlaResponse response = service.updateStepSla(1, 11L, new SlaUpdateRequest(48, "HOUR", "sla"));

        assertEquals(48, response.thoiGianXuLy());
        assertEquals("HOUR", response.donViThoiGian());
    }

    @Test
    void getSlaViolations_calculatesLateHours() {
        XuLyVanBan item = XuLyVanBan.builder()
                .id(10L)
                .vanBanId(8L)
                .nguoiNhanId(2L)
                .hanXuLy(LocalDateTime.now().minusHours(5))
                .ngayHoanThanh(LocalDateTime.now().minusHours(2))
                .build();
        when(xuLyVanBanRepository.findAll(any(Specification.class))).thenReturn(List.of(item));

        List<SlaViolationResponse> result = service.getSlaViolations(null, null, null);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().soGioTre() >= 3);
    }

    @Test
    void getSlaList_byWorkflow() {
        when(quyTrinhRepository.findById(1)).thenReturn(Optional.of(workflow));
        when(buocQuyTrinhRepository.findByQuyTrinh_IdAndThoiGianXuLyIsNotNullOrderByThuTuBuocAsc(1))
                .thenReturn(List.of(step));

        List<SlaResponse> result = service.getSlaList(1);

        assertEquals(1, result.size());
        assertEquals(step.getId(), result.getFirst().stepId());
    }

    @Test
    void getProcessingDetail_notFound_throws() {
        when(xuLyVanBanRepository.findById(999L)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> service.getProcessingDetail(999L));
        assertEquals(ErrorCode.PROCESSING_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getDocumentStatus_notFound_throws() {
        when(xuLyVanBanRepository.findTopByVanBanIdOrderByIdDesc(404L)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> service.getDocumentStatus(404L));
        assertEquals(ErrorCode.DOCUMENT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void transferDocument_stepNotFound_throws() {
        when(buocQuyTrinhRepository.findById(777L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.transferDocument(1L,
                new TransferDocumentRequest(1L, 2L, 3, 777L, "T", "Y", null)));

        assertEquals(ErrorCode.WORKFLOW_STEP_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void updateWorkflowStep_duplicateOrder_throwsInvalidRequest() {
        BuocQuyTrinh other = BuocQuyTrinh.builder().id(99L).quyTrinh(workflow).thuTuBuoc(2).build();
        when(quyTrinhRepository.findById(1)).thenReturn(Optional.of(workflow));
        when(buocQuyTrinhRepository.findByIdAndQuyTrinh_Id(11L, 1)).thenReturn(Optional.of(step));
        when(buocQuyTrinhRepository.findByQuyTrinh_IdOrderByThuTuBuocAsc(1)).thenReturn(List.of(step, other));

        ApiException ex = assertThrows(ApiException.class, () -> service.updateWorkflowStep(1, 11L,
                new WorkflowStepUpdateRequest("X", 2, "R", 8, true, null)));

        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    void getDelegations_filterByUser() {
        service.createDelegation(new DelegationCreateRequest(1L, 2L, LocalDate.now(), LocalDate.now().plusDays(1), "APPROVE", null));
        service.createDelegation(new DelegationCreateRequest(3L, 4L, LocalDate.now(), LocalDate.now().plusDays(1), "APPROVE", null));

        PageResponse<DelegationResponse> result = service.getDelegations(1L, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().getFirst().nguoiUyQuyenId());
    }
}
