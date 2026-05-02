package com.qlda.documentservice.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class DocumentResponses {
    private DocumentResponses() {
    }

    public record IdResponse(Object id) {
    }

    public record DocumentSimpleResponse(
        Long id,
        String soKyHieu,
        String trichYeu,
        Integer loaiVanBan,
        Integer trangThai,
        LocalDateTime ngayTao,
        LocalDateTime ngayCapNhat
    ) {
    }

    public record DocumentListItemResponse(
        Long id,
        String soKyHieu,
        String trichYeu,
        String tenLoaiVanBan,
        String donViBanHanh,
        LocalDateTime ngayVanBan,
        LocalDateTime ngayTiepNhan,
        LocalDateTime hanXuLy,
        Integer trangThai
    ) {
    }

    public record AttachmentResponse(
        Long id,
        Long documentId,
        String tenTep,
        String duongDanTep,
        String loaiTep,
        Long kichThuoc,
        LocalDateTime ngayTaiLen
    ) {
    }

    public record DocumentDetailResponse(
        Long id,
        String soKyHieu,
        String trichYeu,
        Integer loaiVanBanId,
        String tenLoaiVanBan,
        String donViBanHanh,
        String nguoiKy,
        LocalDateTime ngayVanBan,
        LocalDateTime ngayTiepNhan,
        String doMat,
        String doKhan,
        Integer donViChuTriId,
        LocalDateTime hanXuLy,
        Integer trangThai,
        Boolean daOCR,
        Boolean daKySo,
        List<AttachmentResponse> attachments
    ) {
    }

    public record TransferResponse(
        Long documentId,
        Long nguoiNhanId,
        Integer donViXuLyId,
        Integer trangThai
    ) {
    }

    public record DraftCommentResponse(
        Long documentId,
        List<Long> nguoiNhanIds
    ) {
    }

    public record SubmitSigningResponse(
        Long documentId,
        Long nguoiKyId,
        Integer trangThai
    ) {
    }

    public record SubmitApprovalResponse(
        Long documentId,
        Long nguoiPheDuyetId,
        Integer trangThai
    ) {
    }

    public record OcrUploadResponse(
        Long documentId,
        String fileName,
        String fileUrl
    ) {
    }

    public record OcrProcessResponse(
        Long documentId,
        String ocrText,
        Double confidence
    ) {
    }

    public record OcrSaveResponse(
        Long documentId,
        Boolean daOCR
    ) {
    }

    public record TemplateSimpleResponse(
        Integer id,
        String maTemplate
    ) {
    }

    public record TemplateListItemResponse(
        Integer id,
        String maTemplate,
        String tenTemplate,
        Integer loaiVanBanId,
        String tenLoaiVanBan,
        Boolean suDung
    ) {
    }

    public record TemplateDetailResponse(
        Integer id,
        String maTemplate,
        String tenTemplate,
        Integer loaiVanBanId,
        String noiDungMau,
        String tepMau,
        Boolean suDung
    ) {
    }

    public record ApplyTemplateResponse(
        Long documentId,
        Integer templateId,
        String content
    ) {
    }

    public record CreateFromTemplateResponse(
        Long documentId,
        Integer templateId,
        Integer trangThai
    ) {
    }

    public record DigitalSignResponse(
        Long documentId,
        Long nguoiKyId,
        Boolean daKySo,
        LocalDateTime signedAt
    ) {
    }

    public record PublishResponse(
        Long documentId,
        LocalDateTime ngayPhatHanh,
        Integer trangThai
    ) {
    }

    public record SendDocumentResponse(
        Long documentId,
        String kenhGui,
        Integer totalReceivers
    ) {
    }

    public record NumberGenerateResponse(String soKyHieu) {
    }

    public record NumberCheckResponse(String soKyHieu, Boolean exists) {
    }

    public record NumberAssignResponse(Long documentId, String soKyHieu) {
    }

    public record CaseFileSimpleResponse(Long id, String maHoSo) {
    }

    public record CaseFileListItemResponse(
        Long id,
        String maHoSo,
        String tenHoSo,
        Long nguoiPhuTrachId,
        Integer donViId,
        Integer trangThai
    ) {
    }

    public record CaseFileDocumentResponse(
        Long id,
        String soKyHieu,
        String trichYeu
    ) {
    }

    public record CaseFileDetailResponse(
        Long id,
        String maHoSo,
        String tenHoSo,
        Long nguoiPhuTrachId,
        Integer donViId,
        Integer trangThai,
        LocalDateTime ngayMoHoSo,
        LocalDateTime ngayDongHoSo,
        String ghiChu,
        List<CaseFileDocumentResponse> documents
    ) {
    }

    public record CaseFileAttachResponse(Long caseFileId, Long vanBanId) {
    }

    public record CaseFileClassificationResponse(Long caseFileId, String nhomHoSo) {
    }

    public record CaseFileClassificationItemResponse(Long id, String maHoSo, String tenHoSo, String nhomHoSo) {
    }

    public record DocumentVersionResponse(
        Long documentId,
        String versionName,
        String fileUrl,
        String noiDungThayDoi,
        LocalDateTime createdAt
    ) {
    }

    public record DocumentVersionCompareResponse(
        Long documentId,
        String fromVersion,
        String toVersion,
        List<Map<String, String>> differences
    ) {
    }

    public record DocumentVersionRestoreResponse(
        Long documentId,
        String restoredVersion
    ) {
    }

    public record DocumentVersionDeleteResponse(
        Long documentId,
        String versionName
    ) {
    }

    public record AttachmentDownloadResponse(
        Long attachmentId,
        String downloadUrl
    ) {
    }

    public record DocumentTypeResponse(
        Integer id,
        String maLoaiVanBan,
        String tenLoaiVanBan,
        String moTa,
        Boolean suDung
    ) {
    }
}
