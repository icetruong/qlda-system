package com.qlda.workflowservice.service.impl;

import com.qlda.workflowservice.client.AuthServiceClient;
import com.qlda.workflowservice.client.DocumentServiceClient;
import com.qlda.workflowservice.client.dto.DocumentAssigneeUpdateRequest;
import com.qlda.workflowservice.client.dto.DocumentWorkflowStatusUpdateRequest;
import com.qlda.workflowservice.dto.internal.request.InternalWorkflowStartRequest;
import com.qlda.workflowservice.dto.internal.request.InternalWorkflowSubmitApprovalRequest;
import com.qlda.workflowservice.dto.internal.request.InternalWorkflowTransferRequest;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowProgressItemResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowProgressResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStartResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStatisticsResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowStatusResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowSubmitApprovalResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowTimelineItemResponse;
import com.qlda.workflowservice.dto.internal.response.InternalWorkflowTransferResponse;
import com.qlda.workflowservice.dto.response.SlaViolationResponse;
import com.qlda.workflowservice.entity.BuocQuyTrinh;
import com.qlda.workflowservice.entity.XuLyVanBan;
import com.qlda.workflowservice.event.NotificationEvent;
import com.qlda.workflowservice.event.publisher.NotificationEventPublisher;
import com.qlda.workflowservice.exception.ApiException;
import com.qlda.workflowservice.exception.ErrorCode;
import com.qlda.workflowservice.repository.BuocQuyTrinhRepository;
import com.qlda.workflowservice.repository.XuLyVanBanRepository;
import com.qlda.workflowservice.service.InternalWorkflowService;
import com.qlda.workflowservice.specification.XuLyVanBanSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalWorkflowServiceImpl implements InternalWorkflowService {
    private static final int STATUS_PROCESSING = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final String SOURCE_SERVICE = "workflow-service";

    private final XuLyVanBanRepository xuLyVanBanRepository;
    private final BuocQuyTrinhRepository buocQuyTrinhRepository;
    private final DocumentServiceClient documentServiceClient;
    private final AuthServiceClient authServiceClient;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    @Transactional
    public InternalWorkflowStartResponse startWorkflow(Long documentId, InternalWorkflowStartRequest request) {
        documentServiceClient.getDocumentById(documentId);
        BuocQuyTrinh firstStep = buocQuyTrinhRepository.findFirstByQuyTrinh_IdOrderByThuTuBuocAsc(request.workflowId())
                .orElse(null);

        XuLyVanBan saved = xuLyVanBanRepository.save(XuLyVanBan.builder()
                .vanBanId(documentId)
                .buocQuyTrinh(firstStep)
                .nguoiGuiId(request.nguoiTaoId())
                .nguoiNhanId(request.nguoiTaoId())
                .donViXuLyId(request.donViChuTriId())
                .hanhDongXuLy("START")
                .ngayNhan(LocalDateTime.now())
                .hanXuLy(request.hanXuLy())
                .trangThaiXuLy(STATUS_PROCESSING)
                .tyLeHoanThanh(0)
                .build());

        documentServiceClient.updateDocumentWorkflowStatus(documentId,
                new DocumentWorkflowStatusUpdateRequest("PROCESSING", saved.getId()));

        return new InternalWorkflowStartResponse(
                documentId,
                request.workflowId(),
                saved.getId(),
                firstStep != null ? firstStep.getTenBuoc() : null,
                saved.getTrangThaiXuLy()
        );
    }

    @Override
    @Transactional
    public InternalWorkflowTransferResponse transferWorkflow(Long documentId, InternalWorkflowTransferRequest request) {
        documentServiceClient.getDocumentById(documentId);
        authServiceClient.validateUsers(List.of(request.nguoiGuiId(), request.nguoiNhanId()));
        if (request.donViXuLyId() != null) {
            authServiceClient.validateUnits(List.of(request.donViXuLyId()));
        }

        BuocQuyTrinh step = getStepIfExists(request.buocQuyTrinhId());

        XuLyVanBan saved = xuLyVanBanRepository.save(XuLyVanBan.builder()
                .vanBanId(documentId)
                .buocQuyTrinh(step)
                .nguoiGuiId(request.nguoiGuiId())
                .nguoiNhanId(request.nguoiNhanId())
                .donViXuLyId(request.donViXuLyId())
                .hanhDongXuLy("TRANSFER")
                .yKienXuLy(request.yKienXuLy())
                .ngayNhan(LocalDateTime.now())
                .hanXuLy(request.hanXuLy())
                .trangThaiXuLy(STATUS_PROCESSING)
                .tyLeHoanThanh(0)
                .build());

        documentServiceClient.updateDocumentAssignee(documentId,
                new DocumentAssigneeUpdateRequest(saved.getNguoiNhanId(), saved.getDonViXuLyId()));

        publishSafely(new NotificationEvent(
                UUID.randomUUID().toString(),
                "WORKFLOW_TRANSFERRED",
                SOURCE_SERVICE,
                List.of(saved.getNguoiNhanId()),
                "Ban co van ban moi can xu ly",
                "Mot van ban vua duoc chuyen den ban de xu ly",
                "NHAC_VIEC",
                List.of("SYSTEM", "EMAIL"),
                "WORKFLOW",
                saved.getId(),
                Map.of(
                        "documentId", documentId,
                        "processingId", saved.getId(),
                        "nguoiGuiId", saved.getNguoiGuiId(),
                        "nguoiNhanId", saved.getNguoiNhanId()
                ),
                LocalDateTime.now()
        ));

        return new InternalWorkflowTransferResponse(
                saved.getId(),
                saved.getVanBanId(),
                saved.getNguoiNhanId(),
                saved.getTrangThaiXuLy()
        );
    }

    @Override
    @Transactional
    public InternalWorkflowSubmitApprovalResponse submitApproval(Long documentId, InternalWorkflowSubmitApprovalRequest request) {
        documentServiceClient.getDocumentById(documentId);
        authServiceClient.validateUsers(List.of(request.nguoiTrinhId(), request.nguoiPheDuyetId()));

        XuLyVanBan saved = xuLyVanBanRepository.save(XuLyVanBan.builder()
                .vanBanId(documentId)
                .nguoiGuiId(request.nguoiTrinhId())
                .nguoiNhanId(request.nguoiPheDuyetId())
                .hanhDongXuLy("SUBMIT_APPROVAL")
                .yKienXuLy(request.noiDungTrinh())
                .ngayNhan(LocalDateTime.now())
                .trangThaiXuLy(STATUS_PROCESSING)
                .tyLeHoanThanh(0)
                .build());

        documentServiceClient.updateDocumentWorkflowStatus(documentId,
                new DocumentWorkflowStatusUpdateRequest("APPROVAL_PENDING", saved.getId()));

        publishSafely(new NotificationEvent(
                UUID.randomUUID().toString(),
                "WORKFLOW_APPROVAL_REQUESTED",
                SOURCE_SERVICE,
                List.of(saved.getNguoiNhanId()),
                "Ban co van ban can phe duyet",
                "Mot van ban dang cho ban phe duyet",
                "PHE_DUYET",
                List.of("SYSTEM", "EMAIL"),
                "WORKFLOW",
                saved.getId(),
                Map.of(
                        "documentId", documentId,
                        "processingId", saved.getId(),
                        "nguoiTrinhId", saved.getNguoiGuiId(),
                        "nguoiPheDuyetId", saved.getNguoiNhanId()
                ),
                LocalDateTime.now()
        ));

        return new InternalWorkflowSubmitApprovalResponse(
                documentId,
                saved.getId(),
                saved.getNguoiNhanId(),
                saved.getTrangThaiXuLy()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public InternalWorkflowStatusResponse getStatus(Long documentId) {
        XuLyVanBan latest = xuLyVanBanRepository.findTopByVanBanIdOrderByIdDesc(documentId)
                .orElseThrow(() -> new ApiException(ErrorCode.DOCUMENT_NOT_FOUND, HttpStatus.NOT_FOUND, "Document workflow not found"));
        boolean overdue = latest.getHanXuLy() != null
                && latest.getHanXuLy().isBefore(LocalDateTime.now())
                && latest.getNgayHoanThanh() == null;
        return new InternalWorkflowStatusResponse(
                latest.getVanBanId(),
                latest.getBuocQuyTrinh() != null ? latest.getBuocQuyTrinh().getTenBuoc() : null,
                latest.getTrangThaiXuLy(),
                latest.getTyLeHoanThanh(),
                latest.getHanXuLy(),
                overdue
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternalWorkflowTimelineItemResponse> getTimeline(Long documentId) {
        return xuLyVanBanRepository.findByVanBanIdOrderByIdAsc(documentId).stream()
                .sorted(Comparator
                        .comparing(XuLyVanBan::getNgayNhan, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(XuLyVanBan::getId))
                .map(item -> new InternalWorkflowTimelineItemResponse(
                        item.getId(),
                        item.getBuocQuyTrinh() != null ? item.getBuocQuyTrinh().getTenBuoc() : null,
                        item.getNguoiNhanId(),
                        item.getHanhDongXuLy(),
                        item.getNgayNhan(),
                        item.getNgayHoanThanh(),
                        item.getTrangThaiXuLy()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InternalWorkflowStatisticsResponse getStatistics(LocalDate fromDate, LocalDate toDate, Integer donViId) {
        List<XuLyVanBan> records = xuLyVanBanRepository.findAll(
                XuLyVanBanSpecification.internalFilter(fromDate, toDate, donViId, null));
        long completed = records.stream().filter(it -> Integer.valueOf(STATUS_COMPLETED).equals(it.getTrangThaiXuLy())).count();
        long processing = records.stream().filter(it -> Integer.valueOf(STATUS_PROCESSING).equals(it.getTrangThaiXuLy())).count();
        long overdue = records.stream()
                .filter(it -> it.getHanXuLy() != null && it.getHanXuLy().isBefore(LocalDateTime.now()) && it.getNgayHoanThanh() == null)
                .count();
        return new InternalWorkflowStatisticsResponse(records.size(), completed, processing, overdue);
    }

    @Override
    @Transactional(readOnly = true)
    public InternalWorkflowProgressResponse getProgress(LocalDate fromDate, LocalDate toDate, Integer donViId, Long nguoiXuLyId) {
        List<XuLyVanBan> records = xuLyVanBanRepository.findAll(
                XuLyVanBanSpecification.internalFilter(fromDate, toDate, donViId, nguoiXuLyId));
        long completed = records.stream().filter(it -> Integer.valueOf(STATUS_COMPLETED).equals(it.getTrangThaiXuLy())).count();
        long processing = records.stream().filter(it -> Integer.valueOf(STATUS_PROCESSING).equals(it.getTrangThaiXuLy())).count();
        List<InternalWorkflowProgressItemResponse> items = records.stream()
                .sorted(Comparator.comparing(XuLyVanBan::getId))
                .map(item -> new InternalWorkflowProgressItemResponse(
                        item.getVanBanId(),
                        item.getId(),
                        item.getNguoiNhanId(),
                        item.getTrangThaiXuLy(),
                        item.getTyLeHoanThanh(),
                        item.getHanXuLy()
                ))
                .toList();
        return new InternalWorkflowProgressResponse(records.size(), completed, processing, items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlaViolationResponse> getSlaViolations(LocalDate fromDate, LocalDate toDate, Integer donViId) {
        return xuLyVanBanRepository.findAll(XuLyVanBanSpecification.slaViolations(fromDate, toDate, donViId)).stream()
                .sorted(Comparator.comparing(XuLyVanBan::getId))
                .map(item -> {
                    LocalDateTime end = item.getNgayHoanThanh() == null ? LocalDateTime.now() : item.getNgayHoanThanh();
                    long soGioTre = Duration.between(item.getHanXuLy(), end).toHours();
                    return new SlaViolationResponse(
                            item.getId(),
                            item.getVanBanId(),
                            null,
                            item.getNguoiNhanId(),
                            item.getHanXuLy(),
                            item.getNgayHoanThanh(),
                            Math.max(0, soGioTre)
                    );
                })
                .toList();
    }

    private BuocQuyTrinh getStepIfExists(Long stepId) {
        if (stepId == null) {
            return null;
        }
        return buocQuyTrinhRepository.findById(stepId)
                .orElseThrow(() -> new ApiException(ErrorCode.WORKFLOW_STEP_NOT_FOUND, HttpStatus.NOT_FOUND, "Workflow step not found"));
    }

    private void publishSafely(NotificationEvent event) {
        try {
            notificationEventPublisher.publish(event);
        } catch (Exception exception) {
            log.warn("Publish notification event failed: type={}, referenceId={}", event.eventType(), event.referenceId(), exception);
        }
    }
}
