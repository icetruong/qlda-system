package com.qlda.notificationservice.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.notificationservice.audit.controller.AuditLogController;
import com.qlda.notificationservice.audit.dto.AuditLogCreateRequest;
import com.qlda.notificationservice.audit.dto.AuditLogResponse;
import com.qlda.notificationservice.audit.service.AuditLogService;
import com.qlda.notificationservice.common.api.PageResponse;
import com.qlda.notificationservice.common.exception.GlobalExceptionHandler;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    @Mock
    private AuditLogService auditLogService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        AuditLogController controller = new AuditLogController(auditLogService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new AuthorizationHeaderFilter())
            .build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void getList() throws Exception {
        AuditLogResponse item = new AuditLogResponse(1L, 2L, "UPDATE", "VB", 1L, "Chi tiet", "127.0.0.1", LocalDateTime.now(), 1);
        PageResponse<AuditLogResponse> page = new PageResponse<>(List.of(item), 0, 10, 1, 1);
        when(auditLogService.getLogs(any(), any(), any(), any(), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/audit-logs")
                .header("Authorization", "Bearer token")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    void getDetail() throws Exception {
        AuditLogResponse response = new AuditLogResponse(1L, 2L, "UPDATE", "VB", 1L, "Chi tiet", "127.0.0.1", LocalDateTime.now(), 1);
        when(auditLogService.getDetail(1L)).thenReturn(response);

        mockMvc.perform(get("/api/audit-logs/1").header("Authorization", "Bearer token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void postCreate() throws Exception {
        AuditLogCreateRequest request = new AuditLogCreateRequest(2L, "CREATE", "VB", 1L, "Tao", "127.0.0.1", 1);
        AuditLogResponse response = new AuditLogResponse(1L, 2L, "CREATE", "VB", 1L, "Tao", "127.0.0.1", LocalDateTime.now(), 1);
        when(auditLogService.create(any(AuditLogCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/audit-logs")
                .header("Authorization", "Bearer token")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.hanhDong").value("CREATE"));
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

