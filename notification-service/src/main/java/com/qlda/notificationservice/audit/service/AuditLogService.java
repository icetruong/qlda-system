package com.qlda.notificationservice.audit.service;

import com.qlda.notificationservice.audit.dto.AuditLogCreateRequest;
import com.qlda.notificationservice.audit.dto.AuditLogResponse;
import com.qlda.notificationservice.audit.entity.LichSuHeThong;
import com.qlda.notificationservice.audit.repository.LichSuHeThongRepository;
import com.qlda.notificationservice.common.api.PageResponse;
import com.qlda.notificationservice.common.exception.AppException;
import com.qlda.notificationservice.common.exception.ErrorCode;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final LichSuHeThongRepository repository;

    public AuditLogService(LichSuHeThongRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AuditLogResponse create(AuditLogCreateRequest request) {
        LichSuHeThong entity = new LichSuHeThong();
        entity.setNguoiDungId(request.nguoiDungId());
        entity.setHanhDong(request.hanhDong());
        entity.setDoiTuong(request.doiTuong());
        entity.setDoiTuongId(request.doiTuongId());
        entity.setNoiDungChiTiet(request.noiDungChiTiet());
        entity.setDiaChiIP(request.diaChiIP());
        entity.setTrangThai(request.trangThai());
        entity.setThoiGianThucHien(LocalDateTime.now());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getLogs(
        Long nguoiDungId,
        String doiTuong,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        int page,
        int size
    ) {
        Page<LichSuHeThong> result = repository.filter(nguoiDungId, doiTuong, fromDate, toDate, PageRequest.of(page, size));
        return new PageResponse<>(
            result.getContent().stream().map(this::toResponse).toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getDetail(Long id) {
        LichSuHeThong entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.AUDIT_LOG_NOT_FOUND));
        return toResponse(entity);
    }

    private AuditLogResponse toResponse(LichSuHeThong entity) {
        return new AuditLogResponse(
            entity.getId(),
            entity.getNguoiDungId(),
            entity.getHanhDong(),
            entity.getDoiTuong(),
            entity.getDoiTuongId(),
            entity.getNoiDungChiTiet(),
            entity.getDiaChiIP(),
            entity.getThoiGianThucHien(),
            entity.getTrangThai()
        );
    }
}

