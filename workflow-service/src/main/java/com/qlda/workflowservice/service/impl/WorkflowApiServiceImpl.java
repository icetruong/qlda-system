package com.qlda.workflowservice.service.impl;

import com.qlda.workflowservice.client.AuthServiceClient;
import com.qlda.workflowservice.client.DocumentServiceClient;
import com.qlda.workflowservice.client.dto.AuthUserDto;
import com.qlda.workflowservice.client.dto.DocumentAssigneeUpdateRequest;
import com.qlda.workflowservice.client.dto.DocumentStatusUpdateRequest;
import com.qlda.workflowservice.client.dto.DocumentWorkflowStatusUpdateRequest;
import com.qlda.workflowservice.common.PageResponse;
import com.qlda.workflowservice.dto.request.*;
import com.qlda.workflowservice.dto.response.*;
import com.qlda.workflowservice.entity.BuocQuyTrinh;
import com.qlda.workflowservice.entity.QuyTrinh;
import com.qlda.workflowservice.entity.XuLyVanBan;
import com.qlda.workflowservice.event.NotificationEvent;
import com.qlda.workflowservice.event.publisher.NotificationEventPublisher;
import com.qlda.workflowservice.exception.ApiException;
import com.qlda.workflowservice.exception.ErrorCode;
import com.qlda.workflowservice.repository.BuocQuyTrinhRepository;
import com.qlda.workflowservice.repository.QuyTrinhRepository;
import com.qlda.workflowservice.repository.XuLyVanBanRepository;
import com.qlda.workflowservice.service.WorkflowApiService;
import com.qlda.workflowservice.specification.QuyTrinhSpecification;
import com.qlda.workflowservice.specification.XuLyVanBanSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowApiServiceImpl implements WorkflowApiService {
    private static final String UNIT_HOUR = "HOUR";
    private static final String SOURCE_SERVICE = "workflow-service";
    private static final String WORKFLOW_STATUS_PROCESSING = "PROCESSING";
    private static final String WORKFLOW_STATUS_APPROVED = "APPROVED";
    private static final String WORKFLOW_STATUS_REJECTED = "REJECTED";
    private static final String WORKFLOW_STATUS_COMPLETED = "COMPLETED";
    private static final int STATUS_PENDING = 1;
    private static final int STATUS_APPROVED = 2;
    private static final int STATUS_REJECTED = 3;

    private final QuyTrinhRepository quyTrinhRepository;
    private final BuocQuyTrinhRepository buocQuyTrinhRepository;
    private final XuLyVanBanRepository xuLyVanBanRepository;
    private final DocumentServiceClient documentServiceClient;
    private final AuthServiceClient authServiceClient;
    private final NotificationEventPublisher notificationEventPublisher;

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
                .map(this::mapPendingApprovalWithEnrichment);
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
        documentServiceClient.getDocumentById(processing.getVanBanId());
        processing.setYKienXuLy(request.yKienXuLy());
        processing.setTrangThaiXuLy(STATUS_APPROVED);
        processing.setNgayHoanThanh(LocalDateTime.now());
        processing.setTyLeHoanThanh(100);
        xuLyVanBanRepository.save(processing);
        documentServiceClient.updateDocumentStatus(processing.getVanBanId(), new DocumentStatusUpdateRequest(processing.getTrangThaiXuLy()));
        documentServiceClient.updateDocumentWorkflowStatus(
                processing.getVanBanId(),
                new DocumentWorkflowStatusUpdateRequest(WORKFLOW_STATUS_APPROVED, processing.getId())
        );
        publishSafely(buildWorkflowEvent(
                "WORKFLOW_APPROVED",
                collectReceiverIds(processing.getNguoiGuiId()),
                "Van ban da duoc phe duyet",
                "Van ban ma ban theo doi da duoc phe duyet",
                processing.getId(),
                metadataOf(
                        "documentId", processing.getVanBanId(),
                        "processingId", processing.getId(),
                        "nguoiPheDuyetId", processing.getNguoiNhanId()
                )));
        return new ApprovalActionResponse(processing.getId(), processing.getVanBanId(), processing.getTrangThaiXuLy(), processing.getYKienXuLy(), processing.getNgayHoanThanh());
    }

    @Override
    @Transactional
    public ApprovalActionResponse rejectDocument(Long processingId, ApprovalRejectRequest request) {
        XuLyVanBan processing = getProcessingOrThrow(processingId);
        documentServiceClient.getDocumentById(processing.getVanBanId());
        processing.setYKienXuLy(request.lyDoTuChoi());
        processing.setTrangThaiXuLy(STATUS_REJECTED);
        processing.setNgayHoanThanh(LocalDateTime.now());
        xuLyVanBanRepository.save(processing);
        documentServiceClient.updateDocumentStatus(processing.getVanBanId(), new DocumentStatusUpdateRequest(processing.getTrangThaiXuLy()));
        documentServiceClient.updateDocumentWorkflowStatus(
                processing.getVanBanId(),
                new DocumentWorkflowStatusUpdateRequest(WORKFLOW_STATUS_REJECTED, processing.getId())
        );
        publishSafely(buildWorkflowEvent(
                "WORKFLOW_REJECTED",
                collectReceiverIds(processing.getNguoiGuiId()),
                "Van ban bi tu choi",
                "Van ban ma ban theo doi da bi tu choi",
                processing.getId(),
                metadataOf(
                        "documentId", processing.getVanBanId(),
                        "processingId", processing.getId(),
                        "nguoiPheDuyetId", processing.getNguoiNhanId()
                )));
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
        documentServiceClient.getDocumentById(documentId);
        return xuLyVanBanRepository.findByVanBanIdOrderByIdAsc(documentId).stream()
                .sorted(Comparator
                        .comparing(XuLyVanBan::getNgayNhan, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(XuLyVanBan::getId))
                .map(item -> new TimelineItemResponse(
                        item.getId(),
                        item.getBuocQuyTrinh() != null ? item.getBuocQuyTrinh().getTenBuoc() : null,
                        resolveUserDisplayName(item.getNguoiNhanId()),
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
        for (Long processingId : request.processingIds()) {
            XuLyVanBan processing = getProcessingOrThrow(processingId);
            List<Long> receiverIds = collectReceiverIds(processing.getNguoiNhanId(), processing.getNguoiGuiId());
            publishSafely(buildWorkflowEvent(
                    "WORKFLOW_SLA_VIOLATED",
                    receiverIds,
                    "Canh bao SLA",
                    request.noiDung() == null ? "Co van ban co nguy co vi pham SLA" : request.noiDung(),
                    processing.getId(),
                    metadataOf(
                            "documentId", processing.getVanBanId(),
                            "processingId", processing.getId(),
                            "hanXuLy", processing.getHanXuLy()
                    )
            ));
        }
        return new ReminderSendResponse(request.processingIds().size(), request.kenhGui());
    }

    @Override
    @Transactional
    public TransferResponse transferDocument(Long documentId, TransferDocumentRequest request) {
        documentServiceClient.getDocumentById(documentId);
        authServiceClient.validateUsers(List.of(request.nguoiGuiId(), request.nguoiNhanId()));
        if (request.donViXuLyId() != null) {
            authServiceClient.validateUnits(List.of(request.donViXuLyId()));
        }

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
        documentServiceClient.updateDocumentAssignee(
                documentId,
                new DocumentAssigneeUpdateRequest(saved.getNguoiNhanId(), saved.getDonViXuLyId())
        );
        documentServiceClient.updateDocumentWorkflowStatus(
                documentId,
                new DocumentWorkflowStatusUpdateRequest(WORKFLOW_STATUS_PROCESSING, saved.getId())
        );
        publishSafely(buildWorkflowEvent(
                "WORKFLOW_TRANSFERRED",
                collectReceiverIds(saved.getNguoiNhanId()),
                "Ban co van ban moi can xu ly",
                "Mot van ban vua duoc chuyen den ban de xu ly",
                saved.getId(),
                metadataOf(
                        "documentId", documentId,
                        "processingId", saved.getId(),
                        "nguoiGuiId", saved.getNguoiGuiId(),
                        "nguoiNhanId", saved.getNguoiNhanId()
                )
        ));

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
        documentServiceClient.updateDocumentWorkflowStatus(
                processing.getVanBanId(),
                new DocumentWorkflowStatusUpdateRequest(WORKFLOW_STATUS_COMPLETED, processing.getId())
        );
        if (isDocumentFullyCompleted(processing.getVanBanId())) {
            documentServiceClient.updateDocumentStatus(processing.getVanBanId(), new DocumentStatusUpdateRequest(processing.getTrangThaiXuLy()));
        }
        publishSafely(buildWorkflowEvent(
                "WORKFLOW_COMPLETED",
                collectReceiverIds(processing.getNguoiGuiId(), processing.getNguoiNhanId()),
                "Van ban da hoan thanh xu ly",
                "Quy trinh xu ly van ban da duoc hoan tat",
                processing.getId(),
                metadataOf(
                        "documentId", processing.getVanBanId(),
                        "processingId", processing.getId(),
                        "tyLeHoanThanh", processing.getTyLeHoanThanh()
                )
        ));
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

    private PendingApprovalResponse mapPendingApprovalWithEnrichment(XuLyVanBan entity) {
        String soKyHieu = null;
        String trichYeu = null;
        String nguoiGuiTen = null;

        try {
            var document = documentServiceClient.getDocumentById(entity.getVanBanId());
            soKyHieu = document.soKyHieu();
            trichYeu = document.trichYeu();
        } catch (Exception exception) {
            log.warn("Pending approvals enrich document failed: processingId={}, documentId={}",
                    entity.getId(), entity.getVanBanId(), exception);
        }

        try {
            AuthUserDto sender = authServiceClient.getUserById(entity.getNguoiGuiId());
            nguoiGuiTen = sender != null ? sender.hoTen() : null;
        } catch (Exception exception) {
            log.warn("Pending approvals enrich sender failed: processingId={}, nguoiGuiId={}",
                    entity.getId(), entity.getNguoiGuiId(), exception);
        }

        return new PendingApprovalResponse(
                entity.getId(),
                entity.getVanBanId(),
                soKyHieu,
                trichYeu,
                entity.getNguoiGuiId(),
                nguoiGuiTen,
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

    private String resolveUserDisplayName(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            AuthUserDto user = authServiceClient.getUserById(userId);
            return user != null ? user.hoTen() : null;
        } catch (Exception exception) {
            log.warn("Resolve user display name failed: userId={}", userId, exception);
            return null;
        }
    }

    private boolean isDocumentFullyCompleted(Long documentId) {
        return xuLyVanBanRepository.findByVanBanIdOrderByIdAsc(documentId).stream()
                .noneMatch(item -> Integer.valueOf(STATUS_PENDING).equals(item.getTrangThaiXuLy()));
    }

    private List<Long> collectReceiverIds(Long... userIds) {
        Set<Long> ids = new HashSet<>();
        for (Long userId : userIds) {
            if (userId != null) {
                ids.add(userId);
            }
        }
        return List.copyOf(ids);
    }

    private Map<String, Object> metadataOf(Object... keyValues) {
        Map<String, Object> metadata = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = (String) keyValues[i];
            Object value = keyValues[i + 1];
            metadata.put(key, value);
        }
        return metadata;
    }

    private NotificationEvent buildWorkflowEvent(
            String eventType,
            List<Long> nguoiNhanIds,
            String tieuDe,
            String noiDung,
            Long referenceId,
            Map<String, Object> metadata
    ) {
        return new NotificationEvent(
                UUID.randomUUID().toString(),
                eventType,
                SOURCE_SERVICE,
                nguoiNhanIds,
                tieuDe,
                noiDung,
                "NHAC_VIEC",
                List.of("SYSTEM", "EMAIL"),
                "WORKFLOW",
                referenceId,
                metadata,
                LocalDateTime.now()
        );
    }

    private void publishSafely(NotificationEvent event) {
        try {
            notificationEventPublisher.publish(event);
        } catch (Exception exception) {
            log.warn("Publish notification event failed: type={}, referenceId={}", event.eventType(), event.referenceId(), exception);
        }
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
