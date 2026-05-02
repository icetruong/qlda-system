package com.qlda.notificationservice.notification.service;

import com.qlda.notificationservice.common.api.PageResponse;
import com.qlda.notificationservice.common.exception.AppException;
import com.qlda.notificationservice.common.exception.ErrorCode;
import com.qlda.notificationservice.notification.dto.NotificationCreateRequest;
import com.qlda.notificationservice.notification.dto.NotificationReadRequest;
import com.qlda.notificationservice.notification.dto.NotificationReadResponse;
import com.qlda.notificationservice.notification.dto.NotificationResponse;
import com.qlda.notificationservice.notification.dto.NotificationSendRequest;
import com.qlda.notificationservice.notification.dto.NotificationSendResponse;
import com.qlda.notificationservice.notification.entity.ThongBao;
import com.qlda.notificationservice.notification.repository.ThongBaoRepository;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final ThongBaoRepository thongBaoRepository;

    public NotificationService(ThongBaoRepository thongBaoRepository) {
        this.thongBaoRepository = thongBaoRepository;
    }

    @Transactional
    public NotificationResponse create(NotificationCreateRequest request) {
        ThongBao entity = new ThongBao();
        entity.setTieuDe(request.tieuDe());
        entity.setNoiDung(request.noiDung());
        entity.setNguoiNhanId(request.nguoiNhanId());
        entity.setVanBanId(request.vanBanId());
        entity.setLoaiThongBao(request.loaiThongBao());
        entity.setKenhGui(request.kenhGui());
        entity.setDaDoc(false);
        entity.setNgayGui(LocalDateTime.now());
        ThongBao saved = thongBaoRepository.save(entity);
        return toResponse(saved);
    }

    public PageResponse<NotificationResponse> getNotifications(Long nguoiNhanId, Boolean daDoc, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<ThongBao> result;
        if (daDoc == null) {
            result = thongBaoRepository.findByNguoiNhanId(nguoiNhanId, pageable);
        } else {
            result = thongBaoRepository.findByNguoiNhanIdAndDaDoc(nguoiNhanId, daDoc, pageable);
        }
        return new PageResponse<>(
            result.getContent().stream().map(this::toResponse).toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Transactional
    public NotificationReadResponse markAsRead(Long id, NotificationReadRequest request) {
        ThongBao notification = thongBaoRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (request.nguoiNhanId() != null && notification.getNguoiNhanId() != null
            && !request.nguoiNhanId().equals(notification.getNguoiNhanId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        notification.setDaDoc(true);
        notification.setNgayDoc(LocalDateTime.now());
        thongBaoRepository.save(notification);
        return new NotificationReadResponse(notification.getId(), notification.getDaDoc(), notification.getNgayDoc());
    }

    @Transactional
    public void delete(Long id) {
        if (!thongBaoRepository.existsById(id)) {
            throw new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        thongBaoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public NotificationSendResponse send(Long id, NotificationSendRequest request) {
        ThongBao notification = thongBaoRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        // TODO: send email
        // TODO: send Teams
        return new NotificationSendResponse(notification.getId(), request.kenhGui());
    }

    private NotificationResponse toResponse(ThongBao entity) {
        return new NotificationResponse(
            entity.getId(),
            entity.getTieuDe(),
            entity.getNoiDung(),
            entity.getNguoiNhanId(),
            entity.getVanBanId(),
            entity.getLoaiThongBao(),
            entity.getKenhGui(),
            entity.getDaDoc(),
            entity.getNgayGui(),
            entity.getNgayDoc()
        );
    }
}

