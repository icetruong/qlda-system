package com.qlda.documentservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qlda.documentservice.client.AiServiceClient;
import com.qlda.documentservice.client.AuthServiceClient;
import com.qlda.documentservice.client.WorkflowServiceClient;
import com.qlda.documentservice.client.dto.AiClientDtos;
import com.qlda.documentservice.client.dto.AuthClientDtos;
import com.qlda.documentservice.client.dto.WorkflowClientDtos;
import com.qlda.documentservice.common.DocumentConstants;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.entity.LoaiVanBan;
import com.qlda.documentservice.entity.VanBan;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.mapper.DocumentMapper;
import com.qlda.documentservice.notification.NotificationEventPublisher;
import com.qlda.documentservice.repository.LoaiVanBanRepository;
import com.qlda.documentservice.repository.TepDinhKemRepository;
import com.qlda.documentservice.repository.VanBanRepository;
import com.qlda.documentservice.security.SecurityUtils;
import com.qlda.documentservice.service.FileStorageService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentWorkflowServiceImplTest {

    @Mock
    private VanBanRepository vanBanRepository;
    @Mock
    private LoaiVanBanRepository loaiVanBanRepository;
    @Mock
    private TepDinhKemRepository tepDinhKemRepository;
    @Mock
    private DocumentMapper documentMapper;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private WorkflowServiceClient workflowServiceClient;
    @Mock
    private AiServiceClient aiServiceClient;
    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    @InjectMocks
    private DocumentWorkflowServiceImpl service;

    @Test
    void createIncomingDocument_shouldValidateUnitAndStartWorkflow() {
        LoaiVanBan type = createLoaiVanBan(1);
        when(loaiVanBanRepository.findById(1)).thenReturn(Optional.of(type));
        when(securityUtils.getCurrentUserId()).thenReturn(Optional.of(12L));
        when(authServiceClient.getUnitById(5)).thenReturn(new AuthClientDtos.UnitInfoResponse(5, "HC", "Hanh chinh", null, true));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> {
            VanBan entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(100L);
            }
            return entity;
        });
        when(workflowServiceClient.startWorkflow(eq(100L), any(WorkflowClientDtos.StartWorkflowRequest.class)))
            .thenReturn(new WorkflowClientDtos.StartWorkflowResponse(100L, 11L, 22L, "step", DocumentConstants.TRANG_THAI_DANG_XU_LY));
        when(documentMapper.toDocumentSimpleResponse(any(VanBan.class)))
            .thenReturn(new DocumentResponses.DocumentSimpleResponse(100L, "01/CV/2026", "Trich yeu", 1, 1, null, null));

        DocumentResponses.DocumentSimpleResponse response = service.createIncoming(incomingRequest());

        assertThat(response.id()).isEqualTo(100L);
        verify(authServiceClient).getUnitById(5);
        verify(workflowServiceClient).startWorkflow(eq(100L), any(WorkflowClientDtos.StartWorkflowRequest.class));
        verify(aiServiceClient).indexDocument(100L, new AiClientDtos.IndexDocumentRequest("document-service"));
        verify(vanBanRepository, atLeastOnce()).save(any(VanBan.class));
    }

    @Test
    void createIncomingDocument_shouldFailWhenAuthValidationFails() {
        when(authServiceClient.getUnitById(5)).thenThrow(new RuntimeException("auth down"));

        assertThatThrownBy(() -> service.createIncoming(incomingRequest()))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
    }

    @Test
    void createIncomingDocument_shouldSaveDocumentEvenWhenNotificationPublisherFails() {
        LoaiVanBan type = createLoaiVanBan(1);
        when(loaiVanBanRepository.findById(1)).thenReturn(Optional.of(type));
        when(securityUtils.getCurrentUserId()).thenReturn(Optional.of(12L));
        when(authServiceClient.getUnitById(5)).thenReturn(new AuthClientDtos.UnitInfoResponse(5, "HC", "Hanh chinh", null, true));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> {
            VanBan entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(101L);
            }
            return entity;
        });
        when(workflowServiceClient.startWorkflow(eq(101L), any(WorkflowClientDtos.StartWorkflowRequest.class)))
            .thenReturn(new WorkflowClientDtos.StartWorkflowResponse(101L, 11L, 22L, "step", DocumentConstants.TRANG_THAI_DANG_XU_LY));
        doThrow(new RuntimeException("kafka down")).when(notificationEventPublisher).publish(any());
        when(documentMapper.toDocumentSimpleResponse(any(VanBan.class)))
            .thenReturn(new DocumentResponses.DocumentSimpleResponse(101L, "01/CV/2026", "Trich yeu", 1, 1, null, null));

        DocumentResponses.DocumentSimpleResponse response = service.createIncoming(incomingRequest());

        assertThat(response.id()).isEqualTo(101L);
        verify(vanBanRepository, atLeastOnce()).save(any(VanBan.class));
    }

    @Test
    void createOutgoingDocument_shouldValidateUnitAndStartWorkflow() {
        LoaiVanBan type = createLoaiVanBan(3);
        when(loaiVanBanRepository.findById(3)).thenReturn(Optional.of(type));
        when(securityUtils.getCurrentUserId()).thenReturn(Optional.of(15L));
        when(authServiceClient.getUnitById(8)).thenReturn(new AuthClientDtos.UnitInfoResponse(8, "DV8", "Don vi 8", null, true));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> {
            VanBan entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(102L);
            }
            return entity;
        });
        when(workflowServiceClient.startWorkflow(eq(102L), any(WorkflowClientDtos.StartWorkflowRequest.class)))
            .thenReturn(new WorkflowClientDtos.StartWorkflowResponse(102L, 12L, 23L, "step", DocumentConstants.TRANG_THAI_DANG_XU_LY));
        when(documentMapper.toDocumentSimpleResponse(any(VanBan.class)))
            .thenReturn(new DocumentResponses.DocumentSimpleResponse(102L, "02/OUT/2026", "Out", 3, 1, null, null));

        DocumentResponses.DocumentSimpleResponse response = service.createOutgoing(outgoingRequest());

        assertThat(response.id()).isEqualTo(102L);
        verify(authServiceClient).getUnitById(8);
        verify(workflowServiceClient).startWorkflow(eq(102L), any(WorkflowClientDtos.StartWorkflowRequest.class));
        verify(aiServiceClient).indexDocument(102L, new AiClientDtos.IndexDocumentRequest("document-service"));
    }

    @Test
    void transferIncomingDocument_shouldValidateReceiverAndUnitAndCallWorkflowAndPublishEvent() {
        VanBan vanBan = existingDocument(200L);
        when(vanBanRepository.findByIdAndDaXoaFalse(200L)).thenReturn(Optional.of(vanBan));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authServiceClient.getUserById(88L)).thenReturn(new AuthClientDtos.UserInfoResponse(88L, "u88", "User 88", null, null, null, null, null, 1));
        when(authServiceClient.getUnitById(9)).thenReturn(new AuthClientDtos.UnitInfoResponse(9, "DV9", "Don vi 9", null, true));
        when(workflowServiceClient.transferWorkflow(eq(200L), any(WorkflowClientDtos.TransferWorkflowRequest.class)))
            .thenReturn(new WorkflowClientDtos.TransferWorkflowResponse(300L, 200L, 88L, 1));

        DocumentResponses.TransferResponse response = service.transferIncoming(
            200L,
            new DocumentRequests.TransferDocumentRequest(88L, 9, "chuyen xu ly", null)
        );

        assertThat(response.trangThai()).isEqualTo(DocumentConstants.TRANG_THAI_DA_CHUYEN);
        verify(authServiceClient).getUserById(88L);
        verify(authServiceClient).getUnitById(9);
        verify(workflowServiceClient).transferWorkflow(eq(200L), any(WorkflowClientDtos.TransferWorkflowRequest.class));
        verify(notificationEventPublisher).publish(any());
    }

    @Test
    void transferIncomingDocument_shouldPersistStateWhenNotificationFails() {
        VanBan vanBan = existingDocument(201L);
        when(vanBanRepository.findByIdAndDaXoaFalse(201L)).thenReturn(Optional.of(vanBan));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authServiceClient.getUserById(90L)).thenReturn(new AuthClientDtos.UserInfoResponse(90L, "u90", "User 90", null, null, null, null, null, 1));
        when(authServiceClient.getUnitById(10)).thenReturn(new AuthClientDtos.UnitInfoResponse(10, "DV10", "Don vi 10", null, true));
        when(workflowServiceClient.transferWorkflow(eq(201L), any(WorkflowClientDtos.TransferWorkflowRequest.class)))
            .thenReturn(new WorkflowClientDtos.TransferWorkflowResponse(301L, 201L, 90L, 1));
        doThrow(new RuntimeException("kafka down")).when(notificationEventPublisher).publish(any());

        DocumentResponses.TransferResponse response = service.transferIncoming(
            201L,
            new DocumentRequests.TransferDocumentRequest(90L, 10, "chuyen", null)
        );

        assertThat(response.trangThai()).isEqualTo(DocumentConstants.TRANG_THAI_DA_CHUYEN);
        assertThat(vanBan.getTrangThai()).isEqualTo(DocumentConstants.TRANG_THAI_DA_CHUYEN);
    }

    @Test
    void submitDraftSigning_shouldValidateSignerAndCallWorkflowAndPublishEvent() {
        VanBan vanBan = existingDocument(300L);
        when(vanBanRepository.findByIdAndDaXoaFalse(300L)).thenReturn(Optional.of(vanBan));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authServiceClient.getUserById(66L)).thenReturn(new AuthClientDtos.UserInfoResponse(66L, "u66", "User 66", null, null, null, null, null, 1));
        when(workflowServiceClient.submitApproval(eq(300L), any(WorkflowClientDtos.SubmitApprovalRequest.class)))
            .thenReturn(new WorkflowClientDtos.SubmitApprovalResponse(300L, 400L, 66L, 1));

        DocumentResponses.SubmitSigningResponse response = service.submitDraftSigning(
            300L,
            new DocumentRequests.SubmitSigningRequest(66L, "Trinh ky")
        );

        assertThat(response.trangThai()).isEqualTo(DocumentConstants.TRANG_THAI_TRINH_KY);
        verify(authServiceClient).getUserById(66L);
        verify(workflowServiceClient).submitApproval(eq(300L), any(WorkflowClientDtos.SubmitApprovalRequest.class));
        verify(notificationEventPublisher).publish(any());
    }

    @Test
    void submitOutgoingApproval_shouldValidateApproverAndCallWorkflowAndPublishEvent() {
        VanBan vanBan = existingDocument(301L);
        when(vanBanRepository.findByIdAndDaXoaFalse(301L)).thenReturn(Optional.of(vanBan));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authServiceClient.getUserById(77L)).thenReturn(new AuthClientDtos.UserInfoResponse(77L, "u77", "User 77", null, null, null, null, null, 1));
        when(workflowServiceClient.submitApproval(eq(301L), any(WorkflowClientDtos.SubmitApprovalRequest.class)))
            .thenReturn(new WorkflowClientDtos.SubmitApprovalResponse(301L, 401L, 77L, 1));

        DocumentResponses.SubmitApprovalResponse response = service.submitOutgoingApproval(
            301L,
            new DocumentRequests.SubmitApprovalRequest(77L, "Trinh duyet")
        );

        assertThat(response.trangThai()).isEqualTo(DocumentConstants.TRANG_THAI_TRINH_KY);
        verify(authServiceClient).getUserById(77L);
        verify(workflowServiceClient).submitApproval(eq(301L), any(WorkflowClientDtos.SubmitApprovalRequest.class));
        verify(notificationEventPublisher).publish(any());
    }

    @Test
    void processOcr_shouldCallAiServiceAndUpdateOcrStatus() {
        VanBan vanBan = existingDocument(400L);
        when(vanBanRepository.findByIdAndDaXoaFalse(400L)).thenReturn(Optional.of(vanBan));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(aiServiceClient.ocr(any(AiClientDtos.OcrRequest.class)))
            .thenReturn(new AiClientDtos.OcrResponse(400L, "ket qua ocr", 97.5, "model-x"));

        DocumentResponses.OcrProcessResponse response = service.processOcr(
            400L,
            new DocumentRequests.OcrProcessRequest("https://files/doc.pdf", "vi")
        );

        assertThat(response.ocrText()).isEqualTo("ket qua ocr");
        assertThat(vanBan.getDaOCR()).isTrue();
        verify(aiServiceClient).ocr(any(AiClientDtos.OcrRequest.class));
        verify(aiServiceClient).indexDocument(400L, new AiClientDtos.IndexDocumentRequest("document-service"));
        verify(vanBanRepository).save(vanBan);
    }

    @Test
    void updateIncoming_shouldRequestIndexAfterUpdate() {
        VanBan vanBan = existingDocument(710L);
        LoaiVanBan type = createLoaiVanBan(1);
        when(vanBanRepository.findByIdAndDaXoaFalse(710L)).thenReturn(Optional.of(vanBan));
        when(loaiVanBanRepository.findById(1)).thenReturn(Optional.of(type));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentMapper.toDocumentSimpleResponse(any(VanBan.class)))
            .thenReturn(new DocumentResponses.DocumentSimpleResponse(710L, "01/CV/2026", "Trich yeu", 1, 1, null, null));

        DocumentResponses.DocumentSimpleResponse response = service.updateIncoming(710L, incomingRequest());

        assertThat(response.id()).isEqualTo(710L);
        verify(aiServiceClient).indexDocument(710L, new AiClientDtos.IndexDocumentRequest("document-service"));
    }

    @Test
    void createDraft_shouldRequestIndexAfterCreate() {
        LoaiVanBan type = createLoaiVanBan(1);
        when(loaiVanBanRepository.findById(1)).thenReturn(Optional.of(type));
        when(securityUtils.getCurrentUserId()).thenReturn(Optional.of(12L));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> {
            VanBan entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(720L);
            }
            return entity;
        });
        when(documentMapper.toDocumentSimpleResponse(any(VanBan.class)))
            .thenReturn(new DocumentResponses.DocumentSimpleResponse(720L, null, "draft", 1, DocumentConstants.PHAN_LOAI_VAN_BAN_NHAP, null, null));

        DocumentResponses.DocumentSimpleResponse response = service.createDraft(
            new DocumentRequests.DraftDocumentRequest("draft", 1, 5, "noi dung")
        );

        assertThat(response.id()).isEqualTo(720L);
        verify(aiServiceClient).indexDocument(720L, new AiClientDtos.IndexDocumentRequest("document-service"));
    }

    @Test
    void updateDraft_shouldRequestIndexAfterUpdate() {
        VanBan vanBan = existingDocument(730L);
        LoaiVanBan type = createLoaiVanBan(1);
        when(vanBanRepository.findByIdAndDaXoaFalse(730L)).thenReturn(Optional.of(vanBan));
        when(loaiVanBanRepository.findById(1)).thenReturn(Optional.of(type));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentMapper.toDocumentSimpleResponse(any(VanBan.class)))
            .thenReturn(new DocumentResponses.DocumentSimpleResponse(730L, null, "draft-updated", 1, DocumentConstants.PHAN_LOAI_VAN_BAN_NHAP, null, null));

        DocumentResponses.DocumentSimpleResponse response = service.updateDraft(
            730L,
            new DocumentRequests.DraftDocumentRequest("draft-updated", 1, 5, "noi dung moi")
        );

        assertThat(response.id()).isEqualTo(730L);
        verify(aiServiceClient).indexDocument(730L, new AiClientDtos.IndexDocumentRequest("document-service"));
    }

    @Test
    void updateOutgoing_shouldRequestIndexAfterUpdate() {
        VanBan vanBan = existingDocument(740L);
        LoaiVanBan type = createLoaiVanBan(3);
        when(vanBanRepository.findByIdAndDaXoaFalse(740L)).thenReturn(Optional.of(vanBan));
        when(loaiVanBanRepository.findById(3)).thenReturn(Optional.of(type));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentMapper.toDocumentSimpleResponse(any(VanBan.class)))
            .thenReturn(new DocumentResponses.DocumentSimpleResponse(740L, "02/OUT/2026", "Out", 3, 2, null, null));

        DocumentResponses.DocumentSimpleResponse response = service.updateOutgoing(740L, outgoingRequest());

        assertThat(response.id()).isEqualTo(740L);
        verify(aiServiceClient).indexDocument(740L, new AiClientDtos.IndexDocumentRequest("document-service"));
    }

    @Test
    void processOcr_shouldMapAiErrorToOcrFailed() {
        VanBan vanBan = existingDocument(401L);
        when(vanBanRepository.findByIdAndDaXoaFalse(401L)).thenReturn(Optional.of(vanBan));
        when(aiServiceClient.ocr(any(AiClientDtos.OcrRequest.class))).thenThrow(new RuntimeException("ai down"));

        assertThatThrownBy(() -> service.processOcr(401L, new DocumentRequests.OcrProcessRequest("https://files/ocr.pdf", "vi")))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.OCR_FAILED));
    }

    @Test
    void publishDocument_shouldPublishNotificationEvent() {
        VanBan vanBan = existingDocument(500L);
        vanBan.setNguoiTaoId(99L);
        when(vanBanRepository.findByIdAndDaXoaFalse(500L)).thenReturn(Optional.of(vanBan));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponses.PublishResponse response = service.publish(
            500L,
            new DocumentRequests.PublishRequest(LocalDate.of(2026, 5, 1), "Phat hanh")
        );

        assertThat(response.trangThai()).isEqualTo(DocumentConstants.TRANG_THAI_DA_PHAT_HANH);
        verify(notificationEventPublisher).publish(any());
    }

    @Test
    void sendDocument_shouldValidateReceiversAndPublishNotificationEvent() {
        VanBan vanBan = existingDocument(600L);
        when(vanBanRepository.findByIdAndDaXoaFalse(600L)).thenReturn(Optional.of(vanBan));
        when(authServiceClient.validateUsers(new AuthClientDtos.ValidateUsersRequest(List.of(1L, 2L))))
            .thenReturn(new AuthClientDtos.ValidateUsersResponse(true, List.of()));
        when(authServiceClient.validateUnits(new AuthClientDtos.ValidateUnitsRequest(List.of(3, 4))))
            .thenReturn(new AuthClientDtos.ValidateUnitsResponse(true, List.of()));

        DocumentResponses.SendDocumentResponse response = service.send(
            600L,
            new DocumentRequests.SendDocumentRequest(List.of(1L, 2L), List.of(3, 4), "EMAIL", "Noi dung")
        );

        assertThat(response.totalReceivers()).isEqualTo(4);
        verify(authServiceClient).validateUsers(any(AuthClientDtos.ValidateUsersRequest.class));
        verify(authServiceClient).validateUnits(any(AuthClientDtos.ValidateUnitsRequest.class));
        verify(notificationEventPublisher).publish(any());
    }

    @Test
    void createIncomingDocument_shouldNotPublishWhenWorkflowFails() {
        LoaiVanBan type = createLoaiVanBan(1);
        when(loaiVanBanRepository.findById(1)).thenReturn(Optional.of(type));
        when(authServiceClient.getUnitById(5)).thenReturn(new AuthClientDtos.UnitInfoResponse(5, "HC", "Hanh chinh", null, true));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> {
            VanBan entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(700L);
            }
            return entity;
        });
        when(workflowServiceClient.startWorkflow(eq(700L), any(WorkflowClientDtos.StartWorkflowRequest.class)))
            .thenThrow(new RuntimeException("workflow down"));

        assertThatThrownBy(() -> service.createIncoming(incomingRequest()))
            .isInstanceOf(BusinessException.class);
        verify(notificationEventPublisher, never()).publish(any());
    }

    @Test
    void assignNumber_shouldThrowConflict_whenDuplicate() {
        when(vanBanRepository.existsBySoKyHieuAndDaXoaFalse("01/CV/2026")).thenReturn(true);

        assertThatThrownBy(() -> service.assignNumber(1L, new DocumentRequests.AssignNumberRequest("01/CV/2026")))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_DOCUMENT_NUMBER));
    }

    @Test
    void createIncoming_shouldSetIncomingTypeAndCreator() {
        LoaiVanBan type = createLoaiVanBan(1);
        when(loaiVanBanRepository.findById(1)).thenReturn(Optional.of(type));
        when(securityUtils.getCurrentUserId()).thenReturn(Optional.of(12L));
        when(authServiceClient.getUnitById(5)).thenReturn(new AuthClientDtos.UnitInfoResponse(5, "HC", "Hanh chinh", null, true));
        when(vanBanRepository.save(any(VanBan.class))).thenAnswer(invocation -> {
            VanBan entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(100L);
            }
            return entity;
        });
        when(workflowServiceClient.startWorkflow(eq(100L), any(WorkflowClientDtos.StartWorkflowRequest.class)))
            .thenReturn(new WorkflowClientDtos.StartWorkflowResponse(100L, 11L, 22L, "step", DocumentConstants.TRANG_THAI_DANG_XU_LY));
        when(documentMapper.toDocumentSimpleResponse(any(VanBan.class)))
            .thenReturn(new DocumentResponses.DocumentSimpleResponse(100L, "01/CV/2026", "Trich yeu", 1, 1, null, null));

        service.createIncoming(incomingRequest());

        ArgumentCaptor<VanBan> captor = ArgumentCaptor.forClass(VanBan.class);
        verify(vanBanRepository, atLeastOnce()).save(captor.capture());
        VanBan firstSave = captor.getAllValues().get(0);
        assertThat(firstSave.getPhanLoaiVanBan()).isEqualTo(DocumentConstants.PHAN_LOAI_VAN_BAN_DEN);
        assertThat(firstSave.getNguoiTaoId()).isEqualTo(12L);
    }

    private static LoaiVanBan createLoaiVanBan(int id) {
        LoaiVanBan type = new LoaiVanBan();
        type.setId(id);
        return type;
    }

    private static VanBan existingDocument(Long id) {
        VanBan vanBan = new VanBan();
        vanBan.setId(id);
        vanBan.setSoKyHieu("SKH-" + id);
        vanBan.setTrichYeu("Trich yeu " + id);
        vanBan.setTrangThai(DocumentConstants.TRANG_THAI_NHAP);
        vanBan.setDaOCR(false);
        return vanBan;
    }

    private static DocumentRequests.IncomingDocumentRequest incomingRequest() {
        return new DocumentRequests.IncomingDocumentRequest(
            "01/CV/2026",
            "Trich yeu",
            1,
            "DV",
            "Nguoi ky",
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 2),
            "THUONG",
            "KHAN",
            5,
            null,
            null
        );
    }

    private static DocumentRequests.OutgoingDocumentRequest outgoingRequest() {
        return new DocumentRequests.OutgoingDocumentRequest(
            "02/OUT/2026",
            "Out",
            3,
            "Nguoi ky",
            LocalDate.of(2026, 2, 1),
            "MAT",
            "KHAN",
            8,
            null
        );
    }
}
