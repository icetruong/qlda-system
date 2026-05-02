package com.qlda.notificationservice.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.notificationservice.common.api.PageResponse;
import com.qlda.notificationservice.common.exception.GlobalExceptionHandler;
import com.qlda.notificationservice.notification.controller.NotificationController;
import com.qlda.notificationservice.notification.dto.NotificationCreateRequest;
import com.qlda.notificationservice.notification.dto.NotificationReadRequest;
import com.qlda.notificationservice.notification.dto.NotificationReadResponse;
import com.qlda.notificationservice.notification.dto.NotificationResponse;
import com.qlda.notificationservice.notification.dto.NotificationSendRequest;
import com.qlda.notificationservice.notification.dto.NotificationSendResponse;
import com.qlda.notificationservice.notification.service.NotificationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        NotificationController controller = new NotificationController(notificationService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new AuthorizationHeaderFilter())
            .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void postSuccess() throws Exception {
        NotificationCreateRequest request = new NotificationCreateRequest("A", "B", 2L, 1L, "NHAC_VIEC", "SYSTEM");
        NotificationResponse response = new NotificationResponse(1L, "A", "B", 2L, 1L, "NHAC_VIEC", "SYSTEM", false, LocalDateTime.now(), null);
        when(notificationService.create(any(NotificationCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/notifications")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getSuccess() throws Exception {
        NotificationResponse item = new NotificationResponse(1L, "A", "B", 2L, 1L, "NHAC_VIEC", "SYSTEM", false, LocalDateTime.now(), null);
        PageResponse<NotificationResponse> page = new PageResponse<>(List.of(item), 0, 10, 1, 1);
        when(notificationService.getNotifications(eq(2L), eq(Boolean.FALSE), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer token")
                .param("nguoiNhanId", "2")
                .param("daDoc", "false")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void patchReadSuccess() throws Exception {
        NotificationReadResponse response = new NotificationReadResponse(1L, true, LocalDateTime.now());
        when(notificationService.markAsRead(eq(1L), any(NotificationReadRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/notifications/1/read")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new NotificationReadRequest(2L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.daDoc").value(true));
    }

    @Test
    void deleteSuccess() throws Exception {
        doNothing().when(notificationService).delete(1L);

        mockMvc.perform(delete("/api/notifications/1").header("Authorization", "Bearer token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void noToken401() throws Exception {
        mockMvc.perform(get("/api/notifications").param("nguoiNhanId", "2"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void sendSuccess() throws Exception {
        NotificationSendResponse response = new NotificationSendResponse(1L, List.of("SYSTEM", "EMAIL", "TEAMS"));
        when(notificationService.send(eq(1L), any(NotificationSendRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/notifications/1/send")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new NotificationSendRequest(List.of("SYSTEM", "EMAIL", "TEAMS")))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.notificationId").value(1));
    }

    private static class AuthorizationHeaderFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            if (request.getHeader("Authorization") == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            filterChain.doFilter(request, response);
        }
    }
}

