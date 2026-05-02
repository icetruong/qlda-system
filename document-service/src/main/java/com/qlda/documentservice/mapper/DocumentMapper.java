package com.qlda.documentservice.mapper;

import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.entity.HoSoCongViec;
import com.qlda.documentservice.entity.LoaiVanBan;
import com.qlda.documentservice.entity.TepDinhKem;
import com.qlda.documentservice.entity.TemplateVanBan;
import com.qlda.documentservice.entity.VanBan;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentResponses.DocumentSimpleResponse toDocumentSimpleResponse(VanBan vanBan) {
        return new DocumentResponses.DocumentSimpleResponse(
            vanBan.getId(),
            vanBan.getSoKyHieu(),
            vanBan.getTrichYeu(),
            vanBan.getPhanLoaiVanBan(),
            vanBan.getTrangThai(),
            vanBan.getNgayTao(),
            vanBan.getNgayCapNhat()
        );
    }

    public DocumentResponses.DocumentListItemResponse toDocumentListItemResponse(VanBan vanBan) {
        return new DocumentResponses.DocumentListItemResponse(
            vanBan.getId(),
            vanBan.getSoKyHieu(),
            vanBan.getTrichYeu(),
            vanBan.getLoaiVanBan() == null ? null : vanBan.getLoaiVanBan().getTenLoaiVanBan(),
            vanBan.getDonViBanHanh(),
            vanBan.getNgayVanBan(),
            vanBan.getNgayTiepNhan(),
            vanBan.getHanXuLy(),
            vanBan.getTrangThai()
        );
    }

    public DocumentResponses.AttachmentResponse toAttachmentResponse(TepDinhKem tepDinhKem) {
        return new DocumentResponses.AttachmentResponse(
            tepDinhKem.getId(),
            tepDinhKem.getVanBan().getId(),
            tepDinhKem.getTenTep(),
            tepDinhKem.getDuongDanTep(),
            tepDinhKem.getLoaiTep(),
            tepDinhKem.getKichThuoc(),
            tepDinhKem.getNgayTaiLen()
        );
    }

    public DocumentResponses.DocumentDetailResponse toDocumentDetailResponse(VanBan vanBan, List<TepDinhKem> attachments) {
        List<DocumentResponses.AttachmentResponse> mappedAttachments = attachments == null
            ? Collections.emptyList()
            : attachments.stream().map(this::toAttachmentResponse).toList();

        return new DocumentResponses.DocumentDetailResponse(
            vanBan.getId(),
            vanBan.getSoKyHieu(),
            vanBan.getTrichYeu(),
            vanBan.getLoaiVanBan() == null ? null : vanBan.getLoaiVanBan().getId(),
            vanBan.getLoaiVanBan() == null ? null : vanBan.getLoaiVanBan().getTenLoaiVanBan(),
            vanBan.getDonViBanHanh(),
            vanBan.getNguoiKy(),
            vanBan.getNgayVanBan(),
            vanBan.getNgayTiepNhan(),
            vanBan.getDoMat(),
            vanBan.getDoKhan(),
            vanBan.getDonViChuTriId(),
            vanBan.getHanXuLy(),
            vanBan.getTrangThai(),
            vanBan.getDaOCR(),
            vanBan.getDaKySo(),
            mappedAttachments
        );
    }

    public DocumentResponses.TemplateListItemResponse toTemplateListItemResponse(TemplateVanBan templateVanBan) {
        return new DocumentResponses.TemplateListItemResponse(
            templateVanBan.getId(),
            templateVanBan.getMaTemplate(),
            templateVanBan.getTenTemplate(),
            templateVanBan.getLoaiVanBan() == null ? null : templateVanBan.getLoaiVanBan().getId(),
            templateVanBan.getLoaiVanBan() == null ? null : templateVanBan.getLoaiVanBan().getTenLoaiVanBan(),
            templateVanBan.getSuDung()
        );
    }

    public DocumentResponses.TemplateDetailResponse toTemplateDetailResponse(TemplateVanBan templateVanBan) {
        return new DocumentResponses.TemplateDetailResponse(
            templateVanBan.getId(),
            templateVanBan.getMaTemplate(),
            templateVanBan.getTenTemplate(),
            templateVanBan.getLoaiVanBan() == null ? null : templateVanBan.getLoaiVanBan().getId(),
            templateVanBan.getNoiDungMau(),
            templateVanBan.getTepMau(),
            templateVanBan.getSuDung()
        );
    }

    public DocumentResponses.CaseFileListItemResponse toCaseFileListItemResponse(HoSoCongViec hoSoCongViec) {
        return new DocumentResponses.CaseFileListItemResponse(
            hoSoCongViec.getId(),
            hoSoCongViec.getMaHoSo(),
            hoSoCongViec.getTenHoSo(),
            hoSoCongViec.getNguoiPhuTrachId(),
            hoSoCongViec.getDonViId(),
            hoSoCongViec.getTrangThai()
        );
    }

    public DocumentResponses.CaseFileDetailResponse toCaseFileDetailResponse(HoSoCongViec hoSoCongViec) {
        List<DocumentResponses.CaseFileDocumentResponse> documents = hoSoCongViec.getVanBan() == null
            ? Collections.emptyList()
            : List.of(new DocumentResponses.CaseFileDocumentResponse(
                hoSoCongViec.getVanBan().getId(),
                hoSoCongViec.getVanBan().getSoKyHieu(),
                hoSoCongViec.getVanBan().getTrichYeu()
            ));
        return new DocumentResponses.CaseFileDetailResponse(
            hoSoCongViec.getId(),
            hoSoCongViec.getMaHoSo(),
            hoSoCongViec.getTenHoSo(),
            hoSoCongViec.getNguoiPhuTrachId(),
            hoSoCongViec.getDonViId(),
            hoSoCongViec.getTrangThai(),
            hoSoCongViec.getNgayMoHoSo(),
            hoSoCongViec.getNgayDongHoSo(),
            hoSoCongViec.getGhiChu(),
            documents
        );
    }

    public DocumentResponses.DocumentTypeResponse toDocumentTypeResponse(LoaiVanBan loaiVanBan) {
        return new DocumentResponses.DocumentTypeResponse(
            loaiVanBan.getId(),
            loaiVanBan.getMaLoaiVanBan(),
            loaiVanBan.getTenLoaiVanBan(),
            loaiVanBan.getMoTa(),
            loaiVanBan.getSuDung()
        );
    }
}

