package com.qlda.documentservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.qlda.documentservice.dto.internal.InternalDocumentRequests;
import com.qlda.documentservice.dto.internal.InternalDocumentResponses;
import com.qlda.documentservice.service.InternalDocumentService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "internal.auth.service-token=internal-token",
    "internal.auth.allowed-services[0]=workflow-service",
    "internal.auth.allowed-services[1]=ai-service"
})
class InternalDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InternalDocumentService internalDocumentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getInternalDocument_shouldReturnSuccess() throws Exception {
        when(internalDocumentService.getInternalDocument(1L))
            .thenReturn(new InternalDocumentResponses.InternalDocumentResponse(
                1L, "123/CV-ABC", "Trich yeu", 1, "Cong van", "INCOMING", 1, 2L,
                LocalDateTime.of(2026, 5, 10, 17, 0), 1, false, false
            ));

        mockMvc.perform(internalGet("/internal/documents/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    void getInternalContent_shouldReturnSuccess() throws Exception {
        when(internalDocumentService.getDocumentContent(2L))
            .thenReturn(new InternalDocumentResponses.InternalDocumentContentResponse(2L, "TY", "Noi dung", "OCR", "vi"));

        mockMvc.perform(internalGet("/internal/documents/2/content"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.documentId").value(2L));
    }

    @Test
    void getInternalAttachments_shouldReturnSuccess() throws Exception {
        when(internalDocumentService.getDocumentAttachments(3L))
            .thenReturn(List.of(new InternalDocumentResponses.InternalAttachmentResponse(10L, "a.pdf", "/uploads/a.pdf", "pdf", 100L)));

        mockMvc.perform(internalGet("/internal/documents/3/attachments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value(10L));
    }

    @Test
    void updateStatus_shouldReturnSuccess() throws Exception {
        when(internalDocumentService.updateDocumentStatus(eq(4L), any(InternalDocumentRequests.UpdateStatusRequest.class)))
            .thenReturn(new InternalDocumentResponses.UpdateStatusResponse(4L, 3));

        mockMvc.perform(patch("/internal/documents/4/status")
                .header("Authorization", "Bearer internal-token")
                .header("X-Service-Name", "workflow-service")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"trangThai":3,"reason":"done","updatedByService":"workflow-service"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.trangThai").value(3));
    }

    @Test
    void updateAssignee_shouldReturnSuccess() throws Exception {
        when(internalDocumentService.updateDocumentAssignee(eq(5L), any(InternalDocumentRequests.UpdateAssigneeRequest.class)))
            .thenReturn(new InternalDocumentResponses.UpdateAssigneeResponse(5L, 2L, 1));

        mockMvc.perform(patch("/internal/documents/5/assignee")
                .header("Authorization", "Bearer internal-token")
                .header("X-Service-Name", "workflow-service")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nguoiXuLyId":2,"donViXuLyId":1,"hanXuLy":"2026-05-10T17:00:00"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.nguoiXuLyId").value(2L));
    }

    @Test
    void updateWorkflowStatus_shouldReturnSuccess() throws Exception {
        when(internalDocumentService.updateWorkflowStatus(eq(6L), any(InternalDocumentRequests.UpdateWorkflowStatusRequest.class)))
            .thenReturn(new InternalDocumentResponses.UpdateWorkflowStatusResponse(6L, "PROCESSING", 20L));

        mockMvc.perform(patch("/internal/documents/6/workflow-status")
                .header("Authorization", "Bearer internal-token")
                .header("X-Service-Name", "workflow-service")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"workflowStatus":"PROCESSING","currentStep":"step","processingId":20}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.processingId").value(20L));
    }

    @Test
    void updateOcrStatus_shouldReturnSuccess() throws Exception {
        when(internalDocumentService.updateOcrStatus(eq(8L), any(InternalDocumentRequests.UpdateOcrStatusRequest.class)))
            .thenReturn(new InternalDocumentResponses.UpdateOcrStatusResponse(8L, true));

        mockMvc.perform(patch("/internal/documents/8/ocr-status")
                .header("Authorization", "Bearer internal-token")
                .header("X-Service-Name", "ai-service")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"daOCR":true}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.daOCR").value(true));
    }

    @Test
    void getStatistics_shouldReturnSuccess() throws Exception {
        InternalDocumentResponses.InternalDocumentStatisticsResponse statistics = new InternalDocumentResponses.InternalDocumentStatisticsResponse(
            10L,
            7L,
            3L,
            List.of(new InternalDocumentResponses.StatisticItemResponse("Dang xu ly", 4L))
        );
        when(internalDocumentService.getStatistics(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), 1, "status"))
            .thenReturn(statistics);

        mockMvc.perform(internalGet("/internal/documents/statistics?fromDate=2026-04-01&toDate=2026-04-30&donViId=1&groupBy=status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalDocuments").value(10L));
    }

    @Test
    void getOverdue_shouldReturnSuccess() throws Exception {
        InternalDocumentResponses.InternalOverdueDocumentsResponse overdue = new InternalDocumentResponses.InternalOverdueDocumentsResponse(
            List.of(new InternalDocumentResponses.OverdueDocumentItemResponse(
                1L,
                "123/CV-ABC",
                "Trich yeu",
                LocalDateTime.of(2026, 4, 25, 17, 0),
                5L,
                1
            )),
            0,
            10,
            1L
        );
        when(internalDocumentService.getOverdueDocuments(1, 2L, 0, 10)).thenReturn(overdue);

        mockMvc.perform(internalGet("/internal/documents/overdue?donViId=1&nguoiXuLyId=2&page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[0].documentId").value(1L));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder internalGet(String path) {
        return get(path)
            .header("Authorization", "Bearer internal-token")
            .header("X-Service-Name", "workflow-service");
    }
}
