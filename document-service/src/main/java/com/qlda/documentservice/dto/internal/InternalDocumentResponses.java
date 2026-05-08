package com.qlda.documentservice.dto.internal;

import java.time.LocalDateTime;
import java.util.List;

public final class InternalDocumentResponses {
    private InternalDocumentResponses() {
    }

    public record InternalDocumentResponse(
        Long id,
        String soKyHieu,
        String trichYeu,
        Integer loaiVanBanId,
        String tenLoaiVanBan,
        String documentType,
        Integer donViChuTriId,
        Long nguoiTaoId,
        LocalDateTime hanXuLy,
        Integer trangThai,
        Boolean daOCR,
        Boolean daKySo
    ) {
    }

    public record InternalDocumentContentResponse(
        Long documentId,
        String trichYeu,
        String noiDung,
        String ocrText,
        String language
    ) {
    }

    public record InternalAttachmentResponse(
        Long id,
        String tenTep,
        String duongDanTep,
        String loaiTep,
        Long kichThuoc
    ) {
    }

    public record UpdateStatusResponse(Long documentId, Integer trangThai) {
    }

    public record UpdateAssigneeResponse(Long documentId, Long nguoiXuLyId, Integer donViXuLyId) {
    }

    public record UpdateWorkflowStatusResponse(Long documentId, String workflowStatus, Long processingId) {
    }

    public record UpdateOcrStatusResponse(Long documentId, Boolean daOCR) {
    }

    public record AccessCheckResponse(List<Long> allowedDocumentIds) {
    }

    public record MyUploadedDocumentCountResponse(Long userId, Long count) {
    }

    public record TotalDocumentCountResponse(Long count) {
    }

    public record StatisticItemResponse(String label, Long value) {
    }

    public record InternalDocumentStatisticsResponse(
        Long totalDocuments,
        Long incomingDocuments,
        Long outgoingDocuments,
        List<StatisticItemResponse> items
    ) {
    }

    public record OverdueDocumentItemResponse(
        Long documentId,
        String soKyHieu,
        String trichYeu,
        LocalDateTime hanXuLy,
        Long soNgayTre,
        Integer trangThai
    ) {
    }

    public record InternalOverdueDocumentsResponse(
        List<OverdueDocumentItemResponse> content,
        int page,
        int size,
        long totalElements
    ) {
    }
}
