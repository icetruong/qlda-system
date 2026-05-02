package com.qlda.authservice.service;

import com.qlda.authservice.common.ErrorCode;
import com.qlda.authservice.dto.audit.AuditLogExportResponse;
import com.qlda.authservice.dto.audit.AuditLogResponse;
import com.qlda.authservice.dto.common.PageData;
import com.qlda.authservice.exception.ApiException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuditLogService {

    private final AtomicLong idSequence = new AtomicLong(0);
    private final CopyOnWriteArrayList<AuditLogResponse> store = new CopyOnWriteArrayList<>();

    public AuditLogService() {
        store.add(new AuditLogResponse(
                idSequence.incrementAndGet(),
                1L,
                "System",
                "BOOT",
                "AuthService",
                0L,
                "Service initialized",
                "127.0.0.1",
                LocalDateTime.now(),
                1
        ));
    }

    public void log(
            Long userId,
            String hoTen,
            String action,
            String objectName,
            Long objectId,
            String detail,
            String ip,
            Integer status
    ) {
        AuditLogResponse entry = new AuditLogResponse(
                idSequence.incrementAndGet(),
                userId,
                hoTen,
                action,
                objectName,
                objectId,
                detail,
                ip,
                LocalDateTime.now(),
                status
        );
        store.add(entry);
    }

    public PageData<AuditLogResponse> getAuditLogs(
            Pageable pageable,
            Long userId,
            String hanhDong,
            String doiTuong,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Stream<AuditLogResponse> stream = store.stream();
        if (userId != null) {
            stream = stream.filter(entry -> userId.equals(entry.nguoiDungId()));
        }
        if (StringUtils.hasText(hanhDong)) {
            String action = hanhDong.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(entry -> entry.hanhDong() != null
                    && entry.hanhDong().toLowerCase(Locale.ROOT).contains(action));
        }
        if (StringUtils.hasText(doiTuong)) {
            String objectFilter = doiTuong.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(entry -> entry.doiTuong() != null
                    && entry.doiTuong().toLowerCase(Locale.ROOT).contains(objectFilter));
        }
        if (fromDate != null) {
            LocalDateTime from = fromDate.atStartOfDay();
            stream = stream.filter(entry -> entry.thoiGianThucHien() != null && !entry.thoiGianThucHien().isBefore(from));
        }
        if (toDate != null) {
            LocalDateTime to = LocalDateTime.of(toDate, LocalTime.MAX);
            stream = stream.filter(entry -> entry.thoiGianThucHien() != null && !entry.thoiGianThucHien().isAfter(to));
        }

        List<AuditLogResponse> filtered = stream
                .sorted(Comparator.comparing(AuditLogResponse::thoiGianThucHien).reversed())
                .toList();

        int start = Math.min((int) pageable.getOffset(), filtered.size());
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        Page<AuditLogResponse> page = new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
        return new PageData<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public AuditLogResponse getAuditLogById(Long id) {
        return store.stream()
                .filter(entry -> entry.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        ErrorCode.AUDIT_LOG_NOT_FOUND,
                        "Audit log not found"
                ));
    }

    public AuditLogExportResponse exportLogs(String format, LocalDate fromDate, LocalDate toDate) {
        String suffix = "excel".equalsIgnoreCase(format) ? "xlsx" : "csv";
        String fileName = "audit-log-" + LocalDate.now() + "." + suffix;
        // TODO: integrate real export file generation.
        return new AuditLogExportResponse("/files/exports/" + fileName);
    }
}
