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
import com.qlda.workflowservice.service.WorkflowApiService;
import com.qlda.workflowservice.specification.QuyTrinhSpecification;
import com.qlda.workflowservice.specification.XuLyVanBanSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class WorkflowApiServiceImpl implements WorkflowApiService {
    private static final String UNIT_HOUR = "HOUR";
    private static final int STATUS_PENDING = 1;
    private static final int STATUS_APPROVED = 2;
    private static final int STATUS_REJECTED = 3;

    private final QuyTrinhRepository quyTrinhRepository;
    private final BuocQuyTrinhRepository buocQuyTrinhRepository;
    private final XuLyVanBanRepository xuLyVanBanRepository;

    // TODO: Replace with DB table when delegation schema is available.
    private final AtomicLong delegationIdSequence = new AtomicLong(0);
    private final Map<Long, DelegationRecord> delegationStore = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public WorkflowSummaryResponse createWorkflow(WorkflowCreateRequest request) {
        if (quyTrinhRepository.existsByMaQuyTrinh(request.maQuyTrinh())) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "MaQuyTrinh already exists");
        }

        QuyTrinh saved = quyTrinhRepository.save(QuyTrinh.builder()
                .maQuyTrinh(request.maQuyTrinh())
                .tenQuyTrinh(request.tenQuyTrinh())
                .loaiVanBanId(request.loaiVanBanId())
                .moTa(request.moTa())
                .soBuoc(0)
                .suDung(request.suDung() == null || request.suDung())
                .build());

        return mapWorkflowSummary(saved);
    }

    @Override
    @Transactional
    public IdResponse updateWorkflow(Integer id, WorkflowUpdateRequest request) {
        QuyTrinh workflow = getWorkflowOrThrow(id);
        workflow.setTenQuyTrinh(request.tenQuyTrinh());
        workflow.setLoaiVanBanId(request.loaiVanBanId());
        workflow.setMoTa(request.moTa());
        workflow.setSuDung(request.suDung() == null ? workflow.getSuDung() : request.suDung());
        quyTrinhRepository.save(workflow);
        return new IdResponse(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WorkflowSummaryResponse> getWorkflows(String keyword, Integer loaiVanBanId, Boolean suDung, Pageable pageable) {
        var page = quyTrinhRepository.findAll(QuyTrinhSpecification.filter(keyword, loaiVanBanId, suDung), pageable)
                .map(this::mapWorkflowSummary);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowDetailResponse getWorkflowDetail(Integer id) {
        QuyTrinh workflow = getWorkflowOrThrow(id);
        List<WorkflowStepResponse> steps = buocQuyTrinhRepository.findByQuyTrinh_IdOrderByThuTuBuocAsc(id)
                .stream()
                .map(this::mapStep)
                .toList();
        return new WorkflowDetailResponse(
                workflow.getId(),
                workflow.getMaQuyTrinh(),
                workflow.getTenQuyTrinh(),
                workflow.getLoaiVanBanId(),
                workflow.getMoTa(),
                workflow.getSoBuoc(),
                workflow.getSuDung(),
                steps
        );
    }

    @Override
    @Transactional
    public IdResponse deleteWorkflow(Integer id) {
        QuyTrinh workflow = getWorkflowOrThrow(id);
        workflow.setSuDung(false);
        quyTrinhRepository.save(workflow);
        return new IdResponse(id);
    }

    @Override
    @Transactional
    public WorkflowStepResponse createWorkflowStep(Integer workflowId, WorkflowStepCreateRequest request) {
        QuyTrinh workflow = getWorkflowOrThrow(workflowId);
        validateStepOrderCreate(workflowId, request.thuTuBuoc());

        BuocQuyTrinh step = BuocQuyTrinh.builder()
                .quyTrinh(workflow)
                .tenBuoc(request.tenBuoc())
                .thuTuBuoc(request.thuTuBuoc())
                .vaiTroXuLy(request.vaiTroXuLy())
                .thoiGianXuLy(request.thoiGianXuLy())
                .batBuocPheDuyet(request.batBuocPheDuyet() != null && request.batBuocPheDuyet())
                .ghiChu(request.ghiChu())
                .build();

        BuocQuyTrinh saved = buocQuyTrinhRepository.save(step);
        syncSoBuoc(workflowId);
        return mapStep(saved);
    }

    @Override
    @Transactional
    public IdResponse updateWorkflowStep(Integer workflowId, Long stepId, WorkflowStepUpdateRequest request) {
        getWorkflowOrThrow(workflowId);
        BuocQuyTrinh step = getStepOrThrow(workflowId, stepId);
        validateStepOrderUpdate(workflowId, stepId, request.thuTuBuoc());

        step.setTenBuoc(request.tenBuoc());
        step.setThuTuBuoc(request.thuTuBuoc());
        step.setVaiTroXuLy(request.vaiTroXuLy());
        step.setThoiGianXuLy(request.thoiGianXuLy());
        step.setBatBuocPheDuyet(request.batBuocPheDuyet());
        step.setGhiChu(request.ghiChu());
        buocQuyTrinhRepository.save(step);
        return new IdResponse(stepId);
    }

    @Override
    @Transactional
    public IdResponse deleteWorkflowStep(Integer workflowId, Long stepId) {
        getWorkflowOrThrow(workflowId);
        BuocQuyTrinh step = getStepOrThrow(workflowId, stepId);
        buocQuyTrinhRepository.delete(step);
        syncSoBuoc(workflowId);
        return new IdResponse(stepId);
    }

    @Override
    @Transactional
    public SlaResponse updateStepSla(Integer workflowId, Long stepId, SlaUpdateRequest request) {
        BuocQuyTrinh step = getStepOrThrow(workflowId, stepId);
        step.setThoiGianXuLy(request.thoiGianXuLy());
        buocQuyTrinhRepository.save(step);
        return mapSla(step);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PendingApprovalResponse> getPendingApprovals(Long nguoiDuyetId, String keyword, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        var page = xuLyVanBanRepository.findAll(XuLyVanBanSpecification.pendingApprovals(nguoiDuyetId, keyword, fromDate, toDate), pageable)
                .map(this::mapPendingApproval);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public ApprovalActionResponse commentApproval(Long processingId, ApprovalCommentRequest request) {
        XuLyVanBan processing = getProcessingOrThrow(processingId);
        processing.setYKienXuLy(request.noiDungGopY());
        xuLyVanBanRepository.save(processing);
        return new ApprovalActionResponse(processing.getId(), processing.getVanBanId(), processing.getTrangThaiXuLy(), request.noiDungGopY(), processing.getNgayHoanThanh());
    }

    @Override
    @Transactional
    public ApprovalActionResponse approveDocument(Long processingId, ApprovalApproveRequest request) {
        XuLyVanBan processing = getProcessingOrThrow(processingId);
        processing.setYKienXuLy(request.yKienXuLy());
        processing.setTrangThaiXuLy(STATUS_APPROVED);
        processing.setNgayHoanThanh(LocalDateTime.now());
        processing.setTyLeHoanThanh(100);
        xuLyVanBanRepository.save(processing);
        return new ApprovalActionResponse(processing.getId(), processing.getVanBanId(), processing.getTrangThaiXuLy(), processing.getYKienXuLy(), processing.getNgayHoanThanh());
    }

    @Override
    @Transactional
    public ApprovalActionResponse rejectDocument(Long processingId, ApprovalRejectRequest request) {
        XuLyVanBan processing = getProcessingOrThrow(processingId);
        processing.setYKienXuLy(request.lyDoTuChoi());
        processing.setTrangThaiXuLy(STATUS_REJECTED);
        processing.setNgayHoanThanh(LocalDateTime.now());
        xuLyVanBanRepository.save(processing);
        return new ApprovalActionResponse(processing.getId(), processing.getVanBanId(), processing.getTrangThaiXuLy(), request.lyDoTuChoi(), processing.getNgayHoanThanh());
    }

    @Override
    public DelegationResponse createDelegation(DelegationCreateRequest request) {
        long id = delegationIdSequence.incrementAndGet();
        DelegationRecord record = new DelegationRecord(
                id,
                request.nguoiUyQuyenId(),
                request.nguoiDuocUyQuyenId(),
                request.tuNgay(),
                request.denNgay(),
                request.phamViUyQuyen(),
                true
        );
        delegationStore.put(id, record);
        return mapDelegation(record);
    }

    @Override
    public PageResponse<DelegationResponse> getDelegations(Long nguoiUyQuyenId, Long nguoiDuocUyQuyenId, Boolean active, Pageable pageable) {
        List<DelegationResponse> filtered = delegationStore.values().stream()
                .filter(item -> nguoiUyQuyenId == null || item.nguoiUyQuyenId().equals(nguoiUyQuyenId))
                .filter(item -> nguoiDuocUyQuyenId == null || item.nguoiDuocUyQuyenId().equals(nguoiDuocUyQuyenId))
                .filter(item -> {
                    if (active == null) {
                        return true;
                    }
                    boolean isActive = item.active() && !LocalDate.now().isBefore(item.tuNgay()) && !LocalDate.now().isAfter(item.denNgay());
                    return active.equals(isActive);
                })
                .sorted(Comparator.comparing(DelegationRecord::id))
                .map(this::mapDelegation)
                .toList();
        return toPage(filtered, pageable);
    }

    @Override
    public IdResponse cancelDelegation(Long id) {
        if (delegationStore.remove(id) == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, HttpStatus.NOT_FOUND, "Delegation not found");
        }
        return new IdResponse(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentStatusResponse getDocumentStatus(Long documentId) {
        XuLyVanBan latest = xuLyVanBanRepository.findTopByVanBanIdOrderByIdDesc(documentId)
                .orElseThrow(() -> new ApiException(ErrorCode.DOCUMENT_NOT_FOUND, HttpStatus.NOT_FOUND, "Document processing not found"));
        String currentStep = latest.getBuocQuyTrinh() != null ? latest.getBuocQuyTrinh().getTenBuoc() : null;
        boolean isOverdue = latest.getHanXuLy() != null
                && latest.getHanXuLy().isBefore(LocalDateTime.now())
                && latest.getNgayHoanThanh() == null;
        return new DocumentStatusResponse(
                latest.getVanBanId(),
                currentStep,
                latest.getTrangThaiXuLy(),
                latest.getTyLeHoanThanh(),
                latest.getHanXuLy(),
                isOverdue
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineItemResponse> getDocumentTimeline(Long documentId) {
        return xuLyVanBanRepository.findByVanBanIdOrderByIdAsc(documentId).stream()
                .sorted(Comparator
                        .comparing(XuLyVanBan::getNgayNhan, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(XuLyVanBan::getId))
                .map(item -> new TimelineItemResponse(
                        item.getId(),
                        item.getBuocQuyTrinh() != null ? item.getBuocQuyTrinh().getTenBuoc() : null,
                        null, // TODO: Resolve display name from auth-service.
                        item.getHanhDongXuLy(),
                        item.getYKienXuLy(),
                        item.getNgayNhan(),
                        item.getNgayHoanThanh(),
                        item.getTrangThaiXuLy()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessingDetailResponse getProcessingDetail(Long processingId) {
        XuLyVanBan processing = getProcessingOrThrow(processingId);
        return mapProcessingDetail(processing);
    }

    @Override
    @Transactional(readOnly = true)
    public DeadlineCheckResponse checkDeadlines(ReminderCheckDeadlineRequest request) {
        long nearDeadline = xuLyVanBanRepository.count(XuLyVanBanSpecification.nearDeadline(request.checkDate(), request.beforeHours()));
        long overdue = xuLyVanBanRepository.count(XuLyVanBanSpecification.overdue(request.checkDate()));
        return new DeadlineCheckResponse(nearDeadline, overdue);
    }

    @Override
    public ReminderSendResponse sendReminders(ReminderSendRequest request) {
        return new ReminderSendResponse(request.processingIds().size(), request.kenhGui());
    }

    @Override
    @Transactional
    public TransferResponse transferDocument(Long documentId, TransferDocumentRequest request) {
        BuocQuyTrinh step = null;
        if (request.buocQuyTrinhId() != null) {
            step = buocQuyTrinhRepository.findById(request.buocQuyTrinhId())
                    .orElseThrow(() -> new ApiException(ErrorCode.WORKFLOW_STEP_NOT_FOUND, HttpStatus.NOT_FOUND, "Workflow step not found"));
        }

        XuLyVanBan saved = xuLyVanBanRepository.save(XuLyVanBan.builder()
                .vanBanId(documentId)
                .buocQuyTrinh(step)
                .nguoiGuiId(request.nguoiGuiId())
                .nguoiNhanId(request.nguoiNhanId())
                .donViXuLyId(request.donViXuLyId())
                .hanhDongXuLy(request.hanhDongXuLy())
                .yKienXuLy(request.yKienXuLy())
                .ngayNhan(LocalDateTime.now())
                .hanXuLy(request.hanXuLy())
                .trangThaiXuLy(STATUS_PENDING)
                .tyLeHoanThanh(0)
                .build());

        return new TransferResponse(saved.getId(), saved.getVanBanId(), saved.getNguoiNhanId(), saved.getTrangThaiXuLy());
    }

    @Override
    @Transactional
    public ReceiveResponse receiveDocument(Long processingId, ReceiveProcessingRequest request) {
        XuLyVanBan processing = getProcessingOrThrow(processingId);
        processing.setNguoiNhanId(request.nguoiNhanId());
        if (processing.getNgayNhan() == null) {
            processing.setNgayNhan(LocalDateTime.now());
        }
        processing.setTrangThaiXuLy(STATUS_PENDING);
        xuLyVanBanRepository.save(processing);
        return new ReceiveResponse(processing.getId(), processing.getNgayNhan(), processing.getTrangThaiXuLy());
    }

    @Override
    @Transactional
    public CompleteResponse completeProcessing(Long processingId, CompleteProcessingRequest request) {
        XuLyVanBan processing = getProcessingOrThrow(processingId);
        processing.setYKienXuLy(request.yKienXuLy());
        processing.setTepKetQua(request.tepKetQua());
        processing.setTyLeHoanThanh(request.tyLeHoanThanh() == null ? 100 : request.tyLeHoanThanh());
        processing.setNgayHoanThanh(LocalDateTime.now());
        processing.setTrangThaiXuLy(STATUS_APPROVED);
        xuLyVanBanRepository.save(processing);
        return new CompleteResponse(
                processing.getId(),
                processing.getNgayHoanThanh(),
                processing.getTyLeHoanThanh(),
                processing.getTrangThaiXuLy()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlaResponse> getSlaList(Integer workflowId) {
        if (workflowId != null) {
            getWorkflowOrThrow(workflowId);
            return buocQuyTrinhRepository.findByQuyTrinh_IdAndThoiGianXuLyIsNotNullOrderByThuTuBuocAsc(workflowId)
                    .stream()
                    .map(this::mapSla)
                    .toList();
        }
        return buocQuyTrinhRepository.findAll().stream()
                .filter(step -> step.getThoiGianXuLy() != null)
                .sorted(Comparator.comparing(BuocQuyTrinh::getId))
                .map(this::mapSla)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlaViolationResponse> getSlaViolations(LocalDate fromDate, LocalDate toDate, Integer donViId) {
        return xuLyVanBanRepository.findAll(XuLyVanBanSpecification.slaViolations(fromDate, toDate, donViId)).stream()
                .sorted(Comparator.comparing(XuLyVanBan::getId))
                .map(item -> {
                    LocalDateTime referenceTime = item.getNgayHoanThanh() == null ? LocalDateTime.now() : item.getNgayHoanThanh();
                    long lateHours = Duration.between(item.getHanXuLy(), referenceTime).toHours();
                    return new SlaViolationResponse(
                            item.getId(),
                            item.getVanBanId(),
                            null, // TODO: Resolve trichYeu from document-service.
                            item.getNguoiNhanId(),
                            item.getHanXuLy(),
                            item.getNgayHoanThanh(),
                            Math.max(lateHours, 0)
                    );
                })
                .toList();
    }

    private QuyTrinh getWorkflowOrThrow(Integer id) {
        return quyTrinhRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.WORKFLOW_NOT_FOUND, HttpStatus.NOT_FOUND, "Workflow not found"));
    }

    private BuocQuyTrinh getStepOrThrow(Integer workflowId, Long stepId) {
        return buocQuyTrinhRepository.findByIdAndQuyTrinh_Id(stepId, workflowId)
                .orElseThrow(() -> new ApiException(ErrorCode.WORKFLOW_STEP_NOT_FOUND, HttpStatus.NOT_FOUND, "Workflow step not found"));
    }

    private XuLyVanBan getProcessingOrThrow(Long processingId) {
        return xuLyVanBanRepository.findById(processingId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROCESSING_NOT_FOUND, HttpStatus.NOT_FOUND, "Processing not found"));
    }

    private void syncSoBuoc(Integer workflowId) {
        QuyTrinh workflow = getWorkflowOrThrow(workflowId);
        workflow.setSoBuoc((int) buocQuyTrinhRepository.countByQuyTrinh_Id(workflowId));
        quyTrinhRepository.save(workflow);
    }

    private void validateStepOrderCreate(Integer workflowId, Integer thuTuBuoc) {
        if (buocQuyTrinhRepository.existsByQuyTrinh_IdAndThuTuBuoc(workflowId, thuTuBuoc)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "ThuTuBuoc already exists in workflow");
        }
    }

    private void validateStepOrderUpdate(Integer workflowId, Long stepId, Integer thuTuBuoc) {
        boolean duplicate = buocQuyTrinhRepository.findByQuyTrinh_IdOrderByThuTuBuocAsc(workflowId).stream()
                .anyMatch(step -> !step.getId().equals(stepId) && step.getThuTuBuoc().equals(thuTuBuoc));
        if (duplicate) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "ThuTuBuoc already exists in workflow");
        }
    }

    private WorkflowSummaryResponse mapWorkflowSummary(QuyTrinh entity) {
        return new WorkflowSummaryResponse(
                entity.getId(),
                entity.getMaQuyTrinh(),
                entity.getTenQuyTrinh(),
                entity.getLoaiVanBanId(),
                entity.getSoBuoc(),
                entity.getSuDung()
        );
    }

    private WorkflowStepResponse mapStep(BuocQuyTrinh entity) {
        return new WorkflowStepResponse(
                entity.getId(),
                entity.getQuyTrinh().getId(),
                entity.getTenBuoc(),
                entity.getThuTuBuoc(),
                entity.getVaiTroXuLy(),
                entity.getThoiGianXuLy(),
                entity.getBatBuocPheDuyet(),
                entity.getGhiChu()
        );
    }

    private SlaResponse mapSla(BuocQuyTrinh entity) {
        return new SlaResponse(
                entity.getQuyTrinh().getId(),
                entity.getId(),
                entity.getTenBuoc(),
                entity.getThoiGianXuLy(),
                UNIT_HOUR
        );
    }

    private PendingApprovalResponse mapPendingApproval(XuLyVanBan entity) {
        return new PendingApprovalResponse(
                entity.getId(),
                entity.getVanBanId(),
                null, // TODO: Resolve from document-service.
                null, // TODO: Resolve from document-service.
                entity.getNguoiGuiId(),
                null, // TODO: Resolve from auth-service.
                entity.getNgayNhan(),
                entity.getHanXuLy(),
                entity.getTrangThaiXuLy()
        );
    }

    private ProcessingDetailResponse mapProcessingDetail(XuLyVanBan entity) {
        return new ProcessingDetailResponse(
                entity.getId(),
                entity.getVanBanId(),
                entity.getBuocQuyTrinh() != null ? entity.getBuocQuyTrinh().getId() : null,
                entity.getNguoiGuiId(),
                entity.getNguoiNhanId(),
                entity.getDonViXuLyId(),
                entity.getHanhDongXuLy(),
                entity.getYKienXuLy(),
                entity.getNgayNhan(),
                entity.getHanXuLy(),
                entity.getNgayHoanThanh(),
                entity.getTyLeHoanThanh(),
                entity.getTrangThaiXuLy(),
                entity.getTepKetQua()
        );
    }

    private DelegationResponse mapDelegation(DelegationRecord record) {
        return new DelegationResponse(
                record.id(),
                record.nguoiUyQuyenId(),
                record.nguoiDuocUyQuyenId(),
                record.tuNgay(),
                record.denNgay(),
                record.phamViUyQuyen(),
                record.active()
        );
    }

    private <T> PageResponse<T> toPage(List<T> data, Pageable pageable) {
        int start = (int) Math.min(pageable.getOffset(), data.size());
        int end = Math.min(start + pageable.getPageSize(), data.size());
        var page = new PageImpl<>(new ArrayList<>(data.subList(start, end)), pageable, data.size());
        return PageResponse.from(page);
    }

    private record DelegationRecord(
            Long id,
            Long nguoiUyQuyenId,
            Long nguoiDuocUyQuyenId,
            LocalDate tuNgay,
            LocalDate denNgay,
            String phamViUyQuyen,
            Boolean active
    ) {
    }
}
