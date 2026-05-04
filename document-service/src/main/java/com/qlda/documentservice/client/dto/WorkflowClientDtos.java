package com.qlda.documentservice.client.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class WorkflowClientDtos {
    private WorkflowClientDtos() {
    }

    public record StartWorkflowRequest(Long nguoiTaoId, String loaiQuyTrinh) {
    }

    public record StartWorkflowResponse(
        Long documentId,
        Long workflowId,
        Long processingId,
        String currentStep,
        Integer trangThaiXuLy
    ) {
    }

    public record TransferWorkflowRequest(Long nguoiNhanId, Integer donViXuLyId, String noiDungChuyen) {
    }

    public record TransferWorkflowResponse(Long processingId, Long documentId, Long nguoiNhanId, Integer trangThaiXuLy) {
    }

    public record SubmitApprovalRequest(Long nguoiPheDuyetId, String noiDungTrinh) {
    }

    public record SubmitApprovalResponse(Long documentId, Long processingId, Long nguoiPheDuyetId, Integer trangThaiXuLy) {
    }

    public record WorkflowStatusResponse(
        Long documentId,
        String currentStep,
        Integer trangThaiXuLy,
        Integer tyLeHoanThanh,
        LocalDateTime hanXuLy,
        Boolean isOverdue
    ) {
    }

    public record WorkflowTimelineItem(
        Long processingId,
        String tenBuoc,
        Long nguoiXuLyId,
        String hanhDongXuLy,
        LocalDateTime ngayNhan,
        LocalDateTime ngayHoanThanh,
        Integer trangThaiXuLy
    ) {
    }

    public record WorkflowTimelineResponse(List<WorkflowTimelineItem> items) {
    }
}
