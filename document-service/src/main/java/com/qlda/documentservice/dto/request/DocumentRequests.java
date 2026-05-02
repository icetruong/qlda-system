package com.qlda.documentservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class DocumentRequests {
    private DocumentRequests() {
    }

    public record IncomingDocumentRequest(
        String soKyHieu,
        @NotBlank(message = "TrichYeu is required") String trichYeu,
        Integer loaiVanBanId,
        String donViBanHanh,
        String nguoiKy,
        LocalDate ngayVanBan,
        LocalDate ngayTiepNhan,
        String doMat,
        String doKhan,
        Integer donViChuTriId,
        LocalDateTime hanXuLy,
        Integer trangThai
    ) {
    }

    public record TransferDocumentRequest(
        @NotNull(message = "nguoiNhanId is required") Long nguoiNhanId,
        Integer donViXuLyId,
        String noiDungChuyen,
        LocalDateTime hanXuLy
    ) {
    }

    public record OcrProcessRequest(
        @NotBlank(message = "fileUrl is required") String fileUrl,
        String language
    ) {
    }

    public record OcrSaveRequest(
        @NotBlank(message = "ocrText is required") String ocrText,
        Double confidence
    ) {
    }

    public record DraftDocumentRequest(
        @NotBlank(message = "trichYeu is required") String trichYeu,
        Integer loaiVanBanId,
        Integer donViChuTriId,
        String noiDung
    ) {
    }

    public record DraftCommentRequest(
        @NotEmpty(message = "nguoiNhanIds is required") List<Long> nguoiNhanIds,
        String noiDung
    ) {
    }

    public record SubmitSigningRequest(
        @NotNull(message = "nguoiKyId is required") Long nguoiKyId,
        String noiDungTrinhKy
    ) {
    }

    public record TemplateCreateRequest(
        @NotBlank(message = "maTemplate is required") String maTemplate,
        @NotBlank(message = "tenTemplate is required") String tenTemplate,
        Integer loaiVanBanId,
        String noiDungMau,
        String tepMau,
        Boolean suDung
    ) {
    }

    public record TemplateUpdateRequest(
        @NotBlank(message = "tenTemplate is required") String tenTemplate,
        Integer loaiVanBanId,
        String noiDungMau,
        String tepMau,
        Boolean suDung
    ) {
    }

    public record ApplyTemplateRequest(
        Long documentId,
        Map<String, String> replaceData
    ) {
    }

    public record CreateFromTemplateRequest(
        @NotNull(message = "templateId is required") Integer templateId,
        @NotBlank(message = "trichYeu is required") String trichYeu,
        Integer loaiVanBanId,
        Integer donViChuTriId,
        Map<String, String> replaceData
    ) {
    }

    public record DigitalSignRequest(
        Long nguoiKyId,
        String signatureType,
        String ghiChu
    ) {
    }

    public record PublishRequest(
        LocalDate ngayPhatHanh,
        String noiDungPhatHanh
    ) {
    }

    public record SendDocumentRequest(
        List<Long> nguoiNhanIds,
        List<Integer> donViNhanIds,
        String kenhGui,
        String noiDung
    ) {
    }

    public record OutgoingDocumentRequest(
        String soKyHieu,
        @NotBlank(message = "trichYeu is required") String trichYeu,
        Integer loaiVanBanId,
        String nguoiKy,
        LocalDate ngayVanBan,
        String doMat,
        String doKhan,
        Integer donViChuTriId,
        Integer trangThai
    ) {
    }

    public record SubmitApprovalRequest(
        Long nguoiPheDuyetId,
        String noiDungTrinh
    ) {
    }

    public record GenerateNumberRequest(
        Integer loaiVanBanId,
        Integer donViId,
        Integer nam
    ) {
    }

    public record AssignNumberRequest(
        @NotBlank(message = "soKyHieu is required") String soKyHieu
    ) {
    }

    public record CaseFileCreateRequest(
        @NotBlank(message = "maHoSo is required") String maHoSo,
        @NotBlank(message = "tenHoSo is required") String tenHoSo,
        Long vanBanId,
        Long nguoiPhuTrachId,
        Integer donViId,
        Integer trangThai,
        String ghiChu
    ) {
    }

    public record CaseFileUpdateRequest(
        @NotBlank(message = "tenHoSo is required") String tenHoSo,
        Long nguoiPhuTrachId,
        Integer donViId,
        Integer trangThai,
        String ghiChu
    ) {
    }

    public record CaseFileAttachDocumentRequest(
        @NotNull(message = "vanBanId is required") Long vanBanId
    ) {
    }

    public record CaseFileClassificationRequest(
        @NotBlank(message = "nhomHoSo is required") String nhomHoSo,
        String ghiChu
    ) {
    }

    public record DocumentVersionCreateRequest(
        @NotBlank(message = "versionName is required") String versionName,
        String noiDungThayDoi,
        String fileUrl
    ) {
    }

    public record DocumentVersionRestoreRequest(
        @NotBlank(message = "versionName is required") String versionName
    ) {
    }

    public record DocumentTypeCreateRequest(
        @NotBlank(message = "maLoaiVanBan is required") String maLoaiVanBan,
        @NotBlank(message = "tenLoaiVanBan is required") String tenLoaiVanBan,
        String moTa,
        Boolean suDung
    ) {
    }

    public record DocumentTypeUpdateRequest(
        @NotBlank(message = "tenLoaiVanBan is required") String tenLoaiVanBan,
        String moTa,
        Boolean suDung
    ) {
    }
}

