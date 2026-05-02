package com.qlda.documentservice.service;

import com.qlda.documentservice.common.PageResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentWorkflowService {
    DocumentResponses.DocumentSimpleResponse createIncoming(DocumentRequests.IncomingDocumentRequest request);

    DocumentResponses.DocumentSimpleResponse updateIncoming(Long id, DocumentRequests.IncomingDocumentRequest request);

    PageResponse<DocumentResponses.DocumentListItemResponse> listIncoming(
        String keyword,
        Integer loaiVanBanId,
        Integer donViChuTriId,
        Integer trangThai,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable
    );

    DocumentResponses.DocumentDetailResponse getIncomingDetail(Long id);

    DocumentResponses.TransferResponse transferIncoming(Long id, DocumentRequests.TransferDocumentRequest request);

    DocumentResponses.OcrUploadResponse uploadOcrFile(Long id, MultipartFile file);

    DocumentResponses.OcrProcessResponse processOcr(Long id, DocumentRequests.OcrProcessRequest request);

    DocumentResponses.OcrSaveResponse saveOcr(Long id, DocumentRequests.OcrSaveRequest request);

    DocumentResponses.DocumentSimpleResponse createDraft(DocumentRequests.DraftDocumentRequest request);

    DocumentResponses.DocumentSimpleResponse updateDraft(Long id, DocumentRequests.DraftDocumentRequest request);

    DocumentResponses.DraftCommentResponse requestDraftComment(Long id, DocumentRequests.DraftCommentRequest request);

    DocumentResponses.SubmitSigningResponse submitDraftSigning(Long id, DocumentRequests.SubmitSigningRequest request);

    DocumentResponses.DigitalSignResponse digitalSign(Long id, DocumentRequests.DigitalSignRequest request);

    DocumentResponses.PublishResponse publish(Long id, DocumentRequests.PublishRequest request);

    DocumentResponses.SendDocumentResponse send(Long id, DocumentRequests.SendDocumentRequest request);

    DocumentResponses.DocumentSimpleResponse createOutgoing(DocumentRequests.OutgoingDocumentRequest request);

    DocumentResponses.DocumentSimpleResponse updateOutgoing(Long id, DocumentRequests.OutgoingDocumentRequest request);

    DocumentResponses.SubmitApprovalResponse submitOutgoingApproval(Long id, DocumentRequests.SubmitApprovalRequest request);

    PageResponse<DocumentResponses.DocumentListItemResponse> listOutgoing(
        String keyword,
        Integer loaiVanBanId,
        Integer trangThai,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable
    );

    DocumentResponses.DocumentDetailResponse getOutgoingDetail(Long id);

    DocumentResponses.NumberGenerateResponse generateNumber(DocumentRequests.GenerateNumberRequest request);

    DocumentResponses.NumberCheckResponse checkNumber(String soKyHieu);

    DocumentResponses.NumberAssignResponse assignNumber(Long id, DocumentRequests.AssignNumberRequest request);

    DocumentResponses.DocumentVersionResponse createVersion(Long id, DocumentRequests.DocumentVersionCreateRequest request);

    List<DocumentResponses.DocumentVersionResponse> listVersions(Long id);

    DocumentResponses.DocumentVersionCompareResponse compareVersions(Long id, String fromVersion, String toVersion);

    DocumentResponses.DocumentVersionRestoreResponse restoreVersion(Long id, DocumentRequests.DocumentVersionRestoreRequest request);

    DocumentResponses.DocumentVersionDeleteResponse deleteVersion(Long id, String versionName);
}
