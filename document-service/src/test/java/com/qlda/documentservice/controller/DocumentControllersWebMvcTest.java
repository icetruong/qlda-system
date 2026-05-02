package com.qlda.documentservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.qlda.documentservice.common.PageResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.service.AttachmentService;
import com.qlda.documentservice.service.CaseFileService;
import com.qlda.documentservice.service.DocumentTypeService;
import com.qlda.documentservice.service.DocumentWorkflowService;
import com.qlda.documentservice.service.TemplateService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentControllersWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentWorkflowService documentWorkflowService;
    @MockitoBean
    private AttachmentService attachmentService;
    @MockitoBean
    private CaseFileService caseFileService;
    @MockitoBean
    private DocumentTypeService documentTypeService;
    @MockitoBean
    private TemplateService templateService;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/documents/incoming"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403_whenTokenHasNoRequiredRole() throws Exception {
        mockMvc.perform(get("/api/documents/incoming")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void incomingController_shouldAllowAuthenticatedAdmin() throws Exception {
        when(documentWorkflowService.listIncoming(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0));

        mockMvc.perform(get("/api/documents/incoming")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void outgoingController_shouldCreateDocument() throws Exception {
        when(documentWorkflowService.createOutgoing(any(DocumentRequests.OutgoingDocumentRequest.class)))
            .thenReturn(new DocumentResponses.DocumentSimpleResponse(1L, "01/OUT/2026", "out", 2, 0, null, null));

        mockMvc.perform(post("/api/documents/outgoing")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"trichYeu":"van ban di","loaiVanBanId":1,"donViChuTriId":2}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    void draftController_shouldValidateRequest() throws Exception {
        mockMvc.perform(post("/api/documents/drafts")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"trichYeu":""}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_REQUEST.name()));
    }

    @Test
    void publicationController_shouldPublish() throws Exception {
        when(documentWorkflowService.publish(eq(1L), any(DocumentRequests.PublishRequest.class)))
            .thenReturn(new DocumentResponses.PublishResponse(1L, null, 5));

        mockMvc.perform(post("/api/documents/1/publish")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"ngayPhatHanh":"2026-05-01"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.documentId").value(1L));
    }

    @Test
    void attachmentController_shouldUploadFile() throws Exception {
        when(attachmentService.upload(eq(2L), any()))
            .thenReturn(new DocumentResponses.AttachmentResponse(10L, 2L, "a.pdf", "/uploads/a.pdf", "pdf", 3L, null));

        mockMvc.perform(multipart("/api/documents/2/attachments")
                .file("file", "abc".getBytes())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    void caseFileController_shouldCreate() throws Exception {
        when(caseFileService.create(any(DocumentRequests.CaseFileCreateRequest.class)))
            .thenReturn(new DocumentResponses.CaseFileSimpleResponse(1L, "HS-01"));

        mockMvc.perform(post("/api/documents/case-files")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"maHoSo":"HS-01","tenHoSo":"Ho so"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.maHoSo").value("HS-01"));
    }

    @Test
    void documentTypeController_shouldHandleBusinessException() throws Exception {
        when(documentTypeService.create(any(DocumentRequests.DocumentTypeCreateRequest.class)))
            .thenThrow(new BusinessException(ErrorCode.INVALID_REQUEST, "duplicate", HttpStatus.CONFLICT));

        mockMvc.perform(post("/api/documents/types")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"maLoaiVanBan":"CV","tenLoaiVanBan":"Cong van"}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_REQUEST.name()));
    }

    @Test
    void templateController_shouldCreateFromTemplate() throws Exception {
        when(templateService.createFromTemplate(any(DocumentRequests.CreateFromTemplateRequest.class)))
            .thenReturn(new DocumentResponses.CreateFromTemplateResponse(3L, 1, 0));

        mockMvc.perform(post("/api/documents/from-template")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"templateId":1,"trichYeu":"abc"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.documentId").value(3L));
    }

    @Test
    void numberingController_shouldAssignNumber() throws Exception {
        when(documentWorkflowService.assignNumber(eq(7L), any(DocumentRequests.AssignNumberRequest.class)))
            .thenReturn(new DocumentResponses.NumberAssignResponse(7L, "07/CV/2026"));

        mockMvc.perform(patch("/api/documents/7/number")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"soKyHieu":"07/CV/2026"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.soKyHieu").value("07/CV/2026"));
    }

    @Test
    void ocrController_shouldProcess() throws Exception {
        when(documentWorkflowService.processOcr(eq(5L), any(DocumentRequests.OcrProcessRequest.class)))
            .thenReturn(new DocumentResponses.OcrProcessResponse(5L, "ocr", 90.0));

        mockMvc.perform(post("/api/documents/5/ocr/process")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fileUrl":"http://x"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.documentId").value(5L));
    }

    @Test
    void versionController_shouldCreateAndDeleteVersion() throws Exception {
        when(documentWorkflowService.createVersion(eq(9L), any(DocumentRequests.DocumentVersionCreateRequest.class)))
            .thenReturn(new DocumentResponses.DocumentVersionResponse(9L, "v1", "/f", "change", null));
        when(documentWorkflowService.deleteVersion(9L, "v1"))
            .thenReturn(new DocumentResponses.DocumentVersionDeleteResponse(9L, "v1"));

        mockMvc.perform(post("/api/documents/9/versions")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"versionName":"v1","fileUrl":"/f"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.versionName").value("v1"));

        mockMvc.perform(delete("/api/documents/9/versions/v1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.versionName").value("v1"));
    }

    @Test
    void incomingController_shouldTransfer() throws Exception {
        when(documentWorkflowService.transferIncoming(eq(4L), any(DocumentRequests.TransferDocumentRequest.class)))
            .thenReturn(new DocumentResponses.TransferResponse(4L, 11L, 22, 2));

        mockMvc.perform(post("/api/documents/incoming/4/transfer")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nguoiNhanId":11,"donViXuLyId":22}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.documentId").value(4L));
    }
}
