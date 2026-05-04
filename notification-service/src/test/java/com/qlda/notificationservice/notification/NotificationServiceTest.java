package com.qlda.notificationservice.notification;

import com.qlda.notificationservice.common.exception.AppException;
import com.qlda.notificationservice.common.exception.ErrorCode;
import com.qlda.notificationservice.notification.dto.NotificationCreateRequest;
import com.qlda.notificationservice.notification.dto.NotificationReadRequest;
import com.qlda.notificationservice.notification.dto.NotificationSendRequest;
import com.qlda.notificationservice.notification.entity.ThongBao;
import com.qlda.notificationservice.notification.repository.ThongBaoRepository;
import com.qlda.notificationservice.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private ThongBaoRepository thongBaoRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createSuccess() {
        NotificationCreateRequest request = new NotificationCreateRequest(
            "Thong bao",
            "Noi dung",
            2L,
            10L,
            "NHAC_VIEC",
            "SYSTEM"
        );
        ThongBao saved = new ThongBao();
        saved.setId(1L);
        saved.setTieuDe("Thong bao");
        saved.setNoiDung("Noi dung");
        saved.setNguoiNhanId(2L);
        saved.setDaDoc(false);
        saved.setNgayGui(LocalDateTime.now());

        when(thongBaoRepository.save(org.mockito.ArgumentMatchers.any(ThongBao.class))).thenReturn(saved);

        var response = notificationService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.daDoc()).isFalse();
        ArgumentCaptor<ThongBao> captor = ArgumentCaptor.forClass(ThongBao.class);
        verify(thongBaoRepository).save(captor.capture());
        assertThat(captor.getValue().getDaDoc()).isFalse();
    }

    @Test
    void readSuccess() {
        ThongBao existing = new ThongBao();
        existing.setId(1L);
        existing.setNguoiNhanId(2L);
        existing.setDaDoc(false);

        when(thongBaoRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(thongBaoRepository.save(existing)).thenReturn(existing);

        var response = notificationService.markAsRead(1L, new NotificationReadRequest(2L));

        assertThat(response.notificationId()).isEqualTo(1L);
        assertThat(response.daDoc()).isTrue();
        assertThat(response.ngayDoc()).isNotNull();
    }

    @Test
    void deleteSuccess() {
        when(thongBaoRepository.existsById(1L)).thenReturn(true);

        notificationService.delete(1L);

        verify(thongBaoRepository).deleteById(1L);
    }

    @Test
    void notFound() {
        when(thongBaoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(999L, new NotificationReadRequest(2L)))
            .isInstanceOf(AppException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    void listNotificationByNguoiNhanId() {
        ThongBao item = new ThongBao();
        item.setId(1L);
        item.setNguoiNhanId(2L);
        item.setDaDoc(false);
        when(thongBaoRepository.findByNguoiNhanId(2L, PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of(item)));

        var response = notificationService.getNotifications(2L, null, 0, 10);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().id()).isEqualTo(1L);
    }

    @Test
    void sendSuccess() {
        ThongBao existing = new ThongBao();
        existing.setId(1L);
        when(thongBaoRepository.findById(1L)).thenReturn(Optional.of(existing));

        var response = notificationService.send(1L, new NotificationSendRequest(List.of("SYSTEM", "EMAIL")));

        assertThat(response.notificationId()).isEqualTo(1L);
        assertThat(response.sentChannels()).containsExactly("SYSTEM", "EMAIL");
    }
}
