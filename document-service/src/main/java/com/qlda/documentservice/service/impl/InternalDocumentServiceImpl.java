package com.qlda.documentservice.service.impl;

import com.qlda.documentservice.common.DocumentConstants;
import com.qlda.documentservice.dto.internal.InternalDocumentRequests;
import com.qlda.documentservice.dto.internal.InternalDocumentResponses;
import com.qlda.documentservice.entity.TepDinhKem;
import com.qlda.documentservice.entity.VanBan;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.repository.TepDinhKemRepository;
import com.qlda.documentservice.repository.VanBanRepository;
import com.qlda.documentservice.service.InternalDocumentService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InternalDocumentServiceImpl implements InternalDocumentService {

    private final VanBanRepository vanBanRepository;
    private final TepDinhKemRepository tepDinhKemRepository;

    public InternalDocumentServiceImpl(VanBanRepository vanBanRepository, TepDinhKemRepository tepDinhKemRepository) {
        this.vanBanRepository = vanBanRepository;
        this.tepDinhKemRepository = tepDinhKemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public InternalDocumentResponses.InternalDocumentResponse getInternalDocument(Long id) {
        VanBan vanBan = getDocumentOrThrow(id);
        return new InternalDocumentResponses.InternalDocumentResponse(
            vanBan.getId(),
            vanBan.getSoKyHieu(),
            vanBan.getTrichYeu(),
            vanBan.getLoaiVanBan() == null ? null : vanBan.getLoaiVanBan().getId(),
            vanBan.getLoaiVanBan() == null ? null : vanBan.getLoaiVanBan().getTenLoaiVanBan(),
            mapDocumentType(vanBan.getPhanLoaiVanBan()),
            vanBan.getDonViChuTriId(),
            vanBan.getNguoiTaoId(),
            vanBan.getHanXuLy(),
            vanBan.getTrangThai(),
            vanBan.getDaOCR(),
            vanBan.getDaKySo()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public InternalDocumentResponses.InternalDocumentContentResponse getDocumentContent(Long id) {
        VanBan vanBan = getDocumentOrThrow(id);
        // TODO: Current schema has no NoiDung or OCR text column, return minimal content for downstream services.
        return new InternalDocumentResponses.InternalDocumentContentResponse(
            vanBan.getId(),
            vanBan.getTrichYeu(),
            vanBan.getTrichYeu(),
            null,
            "vi"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternalDocumentResponses.InternalAttachmentResponse> getDocumentAttachments(Long id) {
        getDocumentOrThrow(id);
        return tepDinhKemRepository.findByVanBan_Id(id).stream()
            .map(this::mapAttachment)
            .toList();
    }

    @Override
    @Transactional
    public InternalDocumentResponses.UpdateStatusResponse updateDocumentStatus(Long id, InternalDocumentRequests.UpdateStatusRequest request) {
        VanBan vanBan = getDocumentOrThrow(id);
        vanBan.setTrangThai(request.trangThai());
        vanBan.setNgayCapNhat(LocalDateTime.now());
        vanBanRepository.save(vanBan);
        return new InternalDocumentResponses.UpdateStatusResponse(vanBan.getId(), vanBan.getTrangThai());
    }

    @Override
    @Transactional
    public InternalDocumentResponses.UpdateAssigneeResponse updateDocumentAssignee(Long id, InternalDocumentRequests.UpdateAssigneeRequest request) {
        VanBan vanBan = getDocumentOrThrow(id);
        if (request.donViXuLyId() != null) {
            vanBan.setDonViChuTriId(request.donViXuLyId());
        }
        if (request.hanXuLy() != null) {
            vanBan.setHanXuLy(request.hanXuLy());
        }
        vanBan.setNgayCapNhat(LocalDateTime.now());
        vanBanRepository.save(vanBan);
        // TODO: Current schema has no NguoiXuLyID column, return request value without persistence.
        return new InternalDocumentResponses.UpdateAssigneeResponse(vanBan.getId(), request.nguoiXuLyId(), request.donViXuLyId());
    }

    @Override
    @Transactional
    public InternalDocumentResponses.UpdateWorkflowStatusResponse updateWorkflowStatus(
        Long id,
        InternalDocumentRequests.UpdateWorkflowStatusRequest request
    ) {
        VanBan vanBan = getDocumentOrThrow(id);
        vanBan.setNgayCapNhat(LocalDateTime.now());
        vanBanRepository.save(vanBan);
        // TODO: Current schema has no workflowStatus/currentStep/processingId fields.
        return new InternalDocumentResponses.UpdateWorkflowStatusResponse(vanBan.getId(), request.workflowStatus(), request.processingId());
    }

    @Override
    @Transactional
    public InternalDocumentResponses.UpdateOcrStatusResponse updateOcrStatus(Long id, InternalDocumentRequests.UpdateOcrStatusRequest request) {
        VanBan vanBan = getDocumentOrThrow(id);
        vanBan.setDaOCR(request.daOCR());
        vanBan.setNgayCapNhat(LocalDateTime.now());
        vanBanRepository.save(vanBan);
        // TODO: Current schema has no OCR text/confidence columns.
        return new InternalDocumentResponses.UpdateOcrStatusResponse(vanBan.getId(), vanBan.getDaOCR());
    }

    @Override
    @Transactional(readOnly = true)
    public InternalDocumentResponses.InternalDocumentStatisticsResponse getStatistics(
        LocalDate fromDate,
        LocalDate toDate,
        Integer donViId,
        String groupBy
    ) {
        List<VanBan> filtered = vanBanRepository.findAll().stream()
            .filter(v -> !Boolean.TRUE.equals(v.getDaXoa()))
            .filter(v -> donViId == null || donViId.equals(v.getDonViChuTriId()))
            .filter(v -> inDateRange(v, fromDate, toDate))
            .toList();

        long total = filtered.size();
        long incoming = filtered.stream().filter(v -> Integer.valueOf(DocumentConstants.PHAN_LOAI_VAN_BAN_DEN).equals(v.getPhanLoaiVanBan())).count();
        long outgoing = filtered.stream().filter(v -> Integer.valueOf(DocumentConstants.PHAN_LOAI_VAN_BAN_DI).equals(v.getPhanLoaiVanBan())).count();

        List<InternalDocumentResponses.StatisticItemResponse> items = mapStatisticItems(filtered, groupBy);
        return new InternalDocumentResponses.InternalDocumentStatisticsResponse(total, incoming, outgoing, items);
    }

    @Override
    @Transactional(readOnly = true)
    public InternalDocumentResponses.InternalOverdueDocumentsResponse getOverdueDocuments(Integer donViId, Long nguoiXuLyId, int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        List<VanBan> overdue = vanBanRepository.findAll().stream()
            .filter(v -> !Boolean.TRUE.equals(v.getDaXoa()))
            .filter(v -> donViId == null || donViId.equals(v.getDonViChuTriId()))
            .filter(v -> v.getHanXuLy() != null && v.getHanXuLy().isBefore(now))
            .filter(v -> !Integer.valueOf(DocumentConstants.TRANG_THAI_DA_PHAT_HANH).equals(v.getTrangThai()))
            .sorted(Comparator.comparing(VanBan::getHanXuLy))
            .toList();

        // TODO: Current schema has no NguoiXuLyID field, nguoiXuLyId filter is currently not applied.
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        int fromIndex = Math.min(safePage * safeSize, overdue.size());
        int toIndex = Math.min(fromIndex + safeSize, overdue.size());
        List<InternalDocumentResponses.OverdueDocumentItemResponse> content = overdue.subList(fromIndex, toIndex).stream()
            .map(v -> mapOverdue(v, now))
            .toList();

        return new InternalDocumentResponses.InternalOverdueDocumentsResponse(content, safePage, safeSize, overdue.size());
    }

    private boolean inDateRange(VanBan vanBan, LocalDate fromDate, LocalDate toDate) {
        if (vanBan.getNgayTao() == null) {
            return true;
        }
        LocalDate value = vanBan.getNgayTao().toLocalDate();
        if (fromDate != null && value.isBefore(fromDate)) {
            return false;
        }
        return toDate == null || !value.isAfter(toDate);
    }

    private List<InternalDocumentResponses.StatisticItemResponse> mapStatisticItems(List<VanBan> filtered, String groupBy) {
        if (!"status".equalsIgnoreCase(groupBy)) {
            return List.of();
        }
        Map<Integer, Long> grouped = filtered.stream()
            .collect(Collectors.groupingBy(VanBan::getTrangThai, Collectors.counting()));
        return grouped.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.nullsLast(Integer::compareTo)))
            .map(entry -> new InternalDocumentResponses.StatisticItemResponse("TrangThai " + entry.getKey(), entry.getValue()))
            .toList();
    }

    private InternalDocumentResponses.InternalAttachmentResponse mapAttachment(TepDinhKem tepDinhKem) {
        return new InternalDocumentResponses.InternalAttachmentResponse(
            tepDinhKem.getId(),
            tepDinhKem.getTenTep(),
            tepDinhKem.getDuongDanTep(),
            tepDinhKem.getLoaiTep(),
            tepDinhKem.getKichThuoc()
        );
    }

    private InternalDocumentResponses.OverdueDocumentItemResponse mapOverdue(VanBan vanBan, LocalDateTime now) {
        long soNgayTre = Math.max(1, ChronoUnit.DAYS.between(vanBan.getHanXuLy().toLocalDate(), now.toLocalDate()));
        return new InternalDocumentResponses.OverdueDocumentItemResponse(
            vanBan.getId(),
            vanBan.getSoKyHieu(),
            vanBan.getTrichYeu(),
            vanBan.getHanXuLy(),
            soNgayTre,
            vanBan.getTrangThai()
        );
    }

    private String mapDocumentType(Integer phanLoaiVanBan) {
        if (Integer.valueOf(DocumentConstants.PHAN_LOAI_VAN_BAN_DEN).equals(phanLoaiVanBan)) {
            return "INCOMING";
        }
        if (Integer.valueOf(DocumentConstants.PHAN_LOAI_VAN_BAN_DI).equals(phanLoaiVanBan)) {
            return "OUTGOING";
        }
        if (Integer.valueOf(DocumentConstants.PHAN_LOAI_VAN_BAN_NHAP).equals(phanLoaiVanBan)) {
            return "DRAFT";
        }
        return "UNKNOWN";
    }

    private VanBan getDocumentOrThrow(Long id) {
        return vanBanRepository.findByIdAndDaXoaFalse(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.DOCUMENT_NOT_FOUND, "Document not found"));
    }
}
