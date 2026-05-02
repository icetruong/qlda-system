package com.qlda.notificationservice.audit;

import com.qlda.notificationservice.audit.dto.AuditLogCreateRequest;
import com.qlda.notificationservice.audit.entity.LichSuHeThong;
import com.qlda.notificationservice.audit.repository.LichSuHeThongRepository;
import com.qlda.notificationservice.audit.service.AuditLogService;
import com.qlda.notificationservice.common.api.PageResponse;
import com.qlda.notificationservice.common.exception.AppException;
import com.qlda.notificationservice.common.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private LichSuHeThongRepository repository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void createLog() {
        AuditLogCreateRequest request = new AuditLogCreateRequest(2L, "CREATE", "VB", 1L, "Tao van ban", "127.0.0.1", 1);
        LichSuHeThong saved = new LichSuHeThong();
        saved.setId(1L);
        saved.setHanhDong("CREATE");
        saved.setNguoiDungId(2L);
        saved.setThoiGianThucHien(LocalDateTime.now());
        when(repository.save(any(LichSuHeThong.class))).thenReturn(saved);

        var response = auditLogService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.hanhDong()).isEqualTo("CREATE");
    }

    @Test
    void filterLog() {
        LichSuHeThong item = new LichSuHeThong();
        item.setId(1L);
        item.setNguoiDungId(2L);
        item.setDoiTuong("VB");
        item.setHanhDong("UPDATE");
        item.setThoiGianThucHien(LocalDateTime.now());

        when(repository.filter(
            eq(2L), eq("VB"), any(LocalDateTime.class), any(LocalDateTime.class), eq(PageRequest.of(0, 10))
        )).thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1));

        PageResponse<?> response = auditLogService.getLogs(2L, "VB", LocalDateTime.now().minusDays(7), LocalDateTime.now(), 0, 10);

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content()).hasSize(1);
    }

    @Test
    void getDetail() {
        LichSuHeThong item = new LichSuHeThong();
        item.setId(1L);
        item.setHanhDong("DELETE");
        when(repository.findById(1L)).thenReturn(Optional.of(item));

        var response = auditLogService.getDetail(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.hanhDong()).isEqualTo("DELETE");
    }

    @Test
    void getDetailNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditLogService.getDetail(999L))
            .isInstanceOf(AppException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.AUDIT_LOG_NOT_FOUND);
    }
}

