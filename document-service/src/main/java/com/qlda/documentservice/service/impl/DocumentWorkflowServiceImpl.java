package com.qlda.documentservice.service.impl;

import com.qlda.documentservice.common.DocumentConstants;
import com.qlda.documentservice.common.PageResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.entity.LoaiVanBan;
import com.qlda.documentservice.entity.TepDinhKem;
import com.qlda.documentservice.entity.VanBan;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.mapper.DocumentMapper;
import com.qlda.documentservice.repository.LoaiVanBanRepository;
import com.qlda.documentservice.repository.TepDinhKemRepository;
import com.qlda.documentservice.repository.VanBanRepository;
import com.qlda.documentservice.security.SecurityUtils;
import com.qlda.documentservice.service.DocumentWorkflowService;
import com.qlda.documentservice.service.FileStorageService;
import com.qlda.documentservice.specification.VanBanSpecification;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentWorkflowServiceImpl implements DocumentWorkflowService {
    private final VanBanRepository vanBanRepository;
    private final LoaiVanBanRepository loaiVanBanRepository;
    private final TepDinhKemRepository tepDinhKemRepository;
    private final DocumentMapper documentMapper;
    private final FileStorageService fileStorageService;
    private final SecurityUtils securityUtils;

    private final Map<Long, String> ocrFileStore = new ConcurrentHashMap<>();
    private final Map<Long, List<DocumentResponses.DocumentVersionResponse>> versionsStore = new ConcurrentHashMap<>();

    public DocumentWorkflowServiceImpl(
        VanBanRepository vanBanRepository,
        LoaiVanBanRepository loaiVanBanRepository,
        TepDinhKemRepository tepDinhKemRepository,
        DocumentMapper documentMapper,
        FileStorageService fileStorageService,
        SecurityUtils securityUtils
    ) {
        this.vanBanRepository = vanBanRepository;
        this.loaiVanBanRepository = loaiVanBanRepository;
        this.tepDinhKemRepository = tepDinhKemRepository;
        this.documentMapper = documentMapper;
        this.fileStorageService = fileStorageService;
        this.securityUtils = securityUtils;
    }

    @Override
    @Transactional
    public DocumentResponses.DocumentSimpleResponse createIncoming(DocumentRequests.IncomingDocumentRequest request) {
        VanBan vanBan = new VanBan();
        applyIncomingOutgoingFields(vanBan, request.soKyHieu(), request.trichYeu(), request.loaiVanBanId(), request.donViBanHanh(),
            request.nguoiKy(), request.ngayVanBan(), request.ngayTiepNhan(), request.doMat(), request.doKhan(), request.donViChuTriId(),
            request.hanXuLy(), request.trangThai());
        vanBan.setPhanLoaiVanBan(DocumentConstants.PHAN_LOAI_VAN_BAN_DEN);
        vanBan.setTrangThai(vanBan.getTrangThai() == null ? DocumentConstants.TRANG_THAI_NHAP : vanBan.getTrangThai());
        vanBan.setDaXoa(false);
        vanBan.setDaOCR(false);
        vanBan.setDaKySo(false);
        vanBan.setNgayTao(LocalDateTime.now());
        securityUtils.getCurrentUserId().ifPresent(vanBan::setNguoiTaoId);
        return documentMapper.toDocumentSimpleResponse(vanBanRepository.save(vanBan));
    }

    @Override
    @Transactional
    public DocumentResponses.DocumentSimpleResponse updateIncoming(Long id, DocumentRequests.IncomingDocumentRequest request) {
        VanBan vanBan = getDocumentOrThrow(id);
        applyIncomingOutgoingFields(vanBan, request.soKyHieu(), request.trichYeu(), request.loaiVanBanId(), request.donViBanHanh(),
            request.nguoiKy(), request.ngayVanBan(), request.ngayTiepNhan(), request.doMat(), request.doKhan(), request.donViChuTriId(),
            request.hanXuLy(), request.trangThai());
        vanBan.setNgayCapNhat(LocalDateTime.now());
        return documentMapper.toDocumentSimpleResponse(vanBanRepository.save(vanBan));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DocumentResponses.DocumentListItemResponse> listIncoming(
        String keyword,
        Integer loaiVanBanId,
        Integer donViChuTriId,
        Integer trangThai,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable
    ) {
        Specification<VanBan> spec = Specification.where(VanBanSpecification.phanLoai(DocumentConstants.PHAN_LOAI_VAN_BAN_DEN))
            .and(VanBanSpecification.daXoaFalse())
            .and(VanBanSpecification.keyword(keyword))
            .and(VanBanSpecification.loaiVanBanId(loaiVanBanId))
            .and(VanBanSpecification.donViChuTriId(donViChuTriId))
            .and(VanBanSpecification.trangThai(trangThai))
            .and(VanBanSpecification.ngayTaoBetween(fromDate, toDate));
        Page<VanBan> page = vanBanRepository.findAll(spec, pageable);
        return new PageResponse<>(
            page.getContent().stream().map(documentMapper::toDocumentListItemResponse).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponses.DocumentDetailResponse getIncomingDetail(Long id) {
        VanBan vanBan = getDocumentOrThrow(id);
        List<TepDinhKem> attachments = tepDinhKemRepository.findByVanBan_Id(vanBan.getId());
        return documentMapper.toDocumentDetailResponse(vanBan, attachments);
    }

    @Override
    @Transactional
    public DocumentResponses.TransferResponse transferIncoming(Long id, DocumentRequests.TransferDocumentRequest request) {
        VanBan vanBan = getDocumentOrThrow(id);
        vanBan.setTrangThai(DocumentConstants.TRANG_THAI_DA_CHUYEN);
        if (request.hanXuLy() != null) {
            vanBan.setHanXuLy(request.hanXuLy());
        }
        vanBan.setNgayCapNhat(LocalDateTime.now());
        vanBanRepository.save(vanBan);
        // TODO: Need transfer history table for persisting transfer detail.
        return new DocumentResponses.TransferResponse(vanBan.getId(), request.nguoiNhanId(), request.donViXuLyId(), vanBan.getTrangThai());
    }

    @Override
    @Transactional
    public DocumentResponses.OcrUploadResponse uploadOcrFile(Long id, MultipartFile file) {
        VanBan vanBan = getDocumentOrThrow(id);
        String fileUrl = fileStorageService.store(file);
        ocrFileStore.put(vanBan.getId(), fileUrl);
        return new DocumentResponses.OcrUploadResponse(vanBan.getId(), file.getOriginalFilename(), fileUrl);
    }

    @Override
    public DocumentResponses.OcrProcessResponse processOcr(Long id, DocumentRequests.OcrProcessRequest request) {
        getDocumentOrThrow(id);
        String content = "OCR mock result for " + request.fileUrl();
        if (ocrFileStore.containsKey(id)) {
            content = "OCR mock result for uploaded file " + ocrFileStore.get(id);
        }
        return new DocumentResponses.OcrProcessResponse(id, content, 92.5);
    }

    @Override
    @Transactional
    public DocumentResponses.OcrSaveResponse saveOcr(Long id, DocumentRequests.OcrSaveRequest request) {
        VanBan vanBan = getDocumentOrThrow(id);
        vanBan.setDaOCR(true);
        vanBan.setNgayCapNhat(LocalDateTime.now());
        vanBanRepository.save(vanBan);
        // TODO: Current schema has no column/table to persist OCR text and confidence.
        return new DocumentResponses.OcrSaveResponse(id, true);
    }

    @Override
    @Transactional
    public DocumentResponses.DocumentSimpleResponse createDraft(DocumentRequests.DraftDocumentRequest request) {
        VanBan vanBan = new VanBan();
        vanBan.setTrichYeu(request.trichYeu());
        vanBan.setLoaiVanBan(findLoaiVanBan(request.loaiVanBanId()));
        vanBan.setDonViChuTriId(request.donViChuTriId());
        vanBan.setPhanLoaiVanBan(DocumentConstants.PHAN_LOAI_VAN_BAN_NHAP);
        vanBan.setTrangThai(DocumentConstants.TRANG_THAI_NHAP);
        vanBan.setDaXoa(false);
        vanBan.setDaOCR(false);
        vanBan.setDaKySo(false);
        vanBan.setNgayTao(LocalDateTime.now());
        securityUtils.getCurrentUserId().ifPresent(vanBan::setNguoiTaoId);
        return documentMapper.toDocumentSimpleResponse(vanBanRepository.save(vanBan));
    }

    @Override
    @Transactional
    public DocumentResponses.DocumentSimpleResponse updateDraft(Long id, DocumentRequests.DraftDocumentRequest request) {
        VanBan vanBan = getDocumentOrThrow(id);
        vanBan.setTrichYeu(request.trichYeu());
        vanBan.setLoaiVanBan(findLoaiVanBan(request.loaiVanBanId()));
        vanBan.setDonViChuTriId(request.donViChuTriId());
        vanBan.setNgayCapNhat(LocalDateTime.now());
        return documentMapper.toDocumentSimpleResponse(vanBanRepository.save(vanBan));
    }

    @Override
    public DocumentResponses.DraftCommentResponse requestDraftComment(Long id, DocumentRequests.DraftCommentRequest request) {
        getDocumentOrThrow(id);
        // TODO: Current schema has no table for draft comment request history.
        return new DocumentResponses.DraftCommentResponse(id, request.nguoiNhanIds());
    }

    @Override
    @Transactional
    public DocumentResponses.SubmitSigningResponse submitDraftSigning(Long id, DocumentRequests.SubmitSigningRequest request) {
        VanBan vanBan = getDocumentOrThrow(id);
        vanBan.setTrangThai(DocumentConstants.TRANG_THAI_TRINH_KY);
        vanBan.setNgayCapNhat(LocalDateTime.now());
        vanBanRepository.save(vanBan);
        // TODO: Current schema has no table for submit-signing history.
        return new DocumentResponses.SubmitSigningResponse(id, request.nguoiKyId(), vanBan.getTrangThai());
    }

    @Override
    @Transactional
    public DocumentResponses.DigitalSignResponse digitalSign(Long id, DocumentRequests.DigitalSignRequest request) {
        VanBan vanBan = getDocumentOrThrow(id);
        vanBan.setDaKySo(true);
        vanBan.setTrangThai(DocumentConstants.TRANG_THAI_DA_KY);
        vanBan.setNgayCapNhat(LocalDateTime.now());
        vanBanRepository.save(vanBan);
        return new DocumentResponses.DigitalSignResponse(id, request.nguoiKyId(), true, LocalDateTime.now());
    }

    @Override
    @Transactional
    public DocumentResponses.PublishResponse publish(Long id, DocumentRequests.PublishRequest request) {
        VanBan vanBan = getDocumentOrThrow(id);
        LocalDateTime publishTime = request.ngayPhatHanh() == null ? LocalDateTime.now() : request.ngayPhatHanh().atStartOfDay();
        vanBan.setNgayPhatHanh(publishTime);
        vanBan.setTrangThai(DocumentConstants.TRANG_THAI_DA_PHAT_HANH);
        vanBan.setNgayCapNhat(LocalDateTime.now());
        vanBanRepository.save(vanBan);
        return new DocumentResponses.PublishResponse(id, publishTime, vanBan.getTrangThai());
    }

    @Override
    public DocumentResponses.SendDocumentResponse send(Long id, DocumentRequests.SendDocumentRequest request) {
        getDocumentOrThrow(id);
        int users = request.nguoiNhanIds() == null ? 0 : request.nguoiNhanIds().size();
        int units = request.donViNhanIds() == null ? 0 : request.donViNhanIds().size();
        // TODO: Current schema has no receiver table for send history.
        return new DocumentResponses.SendDocumentResponse(id, request.kenhGui(), users + units);
    }

    @Override
    @Transactional
    public DocumentResponses.DocumentSimpleResponse createOutgoing(DocumentRequests.OutgoingDocumentRequest request) {
        VanBan vanBan = new VanBan();
        applyIncomingOutgoingFields(vanBan, request.soKyHieu(), request.trichYeu(), request.loaiVanBanId(), null, request.nguoiKy(),
            request.ngayVanBan(), null, request.doMat(), request.doKhan(), request.donViChuTriId(), null, request.trangThai());
        vanBan.setPhanLoaiVanBan(DocumentConstants.PHAN_LOAI_VAN_BAN_DI);
        vanBan.setTrangThai(vanBan.getTrangThai() == null ? DocumentConstants.TRANG_THAI_NHAP : vanBan.getTrangThai());
        vanBan.setDaXoa(false);
        vanBan.setDaOCR(false);
        vanBan.setDaKySo(false);
        vanBan.setNgayTao(LocalDateTime.now());
        securityUtils.getCurrentUserId().ifPresent(vanBan::setNguoiTaoId);
        return documentMapper.toDocumentSimpleResponse(vanBanRepository.save(vanBan));
    }

    @Override
    @Transactional
    public DocumentResponses.DocumentSimpleResponse updateOutgoing(Long id, DocumentRequests.OutgoingDocumentRequest request) {
        VanBan vanBan = getDocumentOrThrow(id);
        applyIncomingOutgoingFields(vanBan, request.soKyHieu(), request.trichYeu(), request.loaiVanBanId(), null, request.nguoiKy(),
            request.ngayVanBan(), null, request.doMat(), request.doKhan(), request.donViChuTriId(), null, request.trangThai());
        vanBan.setNgayCapNhat(LocalDateTime.now());
        return documentMapper.toDocumentSimpleResponse(vanBanRepository.save(vanBan));
    }

    @Override
    @Transactional
    public DocumentResponses.SubmitApprovalResponse submitOutgoingApproval(Long id, DocumentRequests.SubmitApprovalRequest request) {
        VanBan vanBan = getDocumentOrThrow(id);
        vanBan.setTrangThai(DocumentConstants.TRANG_THAI_TRINH_KY);
        vanBan.setNgayCapNhat(LocalDateTime.now());
        vanBanRepository.save(vanBan);
        // TODO: Current schema has no submit-approval history table.
        return new DocumentResponses.SubmitApprovalResponse(id, request.nguoiPheDuyetId(), vanBan.getTrangThai());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DocumentResponses.DocumentListItemResponse> listOutgoing(
        String keyword,
        Integer loaiVanBanId,
        Integer trangThai,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable
    ) {
        Specification<VanBan> spec = Specification.where(VanBanSpecification.phanLoai(DocumentConstants.PHAN_LOAI_VAN_BAN_DI))
            .and(VanBanSpecification.daXoaFalse())
            .and(VanBanSpecification.keyword(keyword))
            .and(VanBanSpecification.loaiVanBanId(loaiVanBanId))
            .and(VanBanSpecification.trangThai(trangThai))
            .and(VanBanSpecification.ngayTaoBetween(fromDate, toDate));
        Page<VanBan> page = vanBanRepository.findAll(spec, pageable);
        return new PageResponse<>(
            page.getContent().stream().map(documentMapper::toDocumentListItemResponse).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponses.DocumentDetailResponse getOutgoingDetail(Long id) {
        VanBan vanBan = getDocumentOrThrow(id);
        List<TepDinhKem> attachments = tepDinhKemRepository.findByVanBan_Id(vanBan.getId());
        return documentMapper.toDocumentDetailResponse(vanBan, attachments);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponses.NumberGenerateResponse generateNumber(DocumentRequests.GenerateNumberRequest request) {
        int year = request.nam() == null ? LocalDate.now().getYear() : request.nam();
        String code = "CV-QLVB";
        if (request.loaiVanBanId() != null) {
            LoaiVanBan loaiVanBan = findLoaiVanBan(request.loaiVanBanId());
            if (loaiVanBan != null && loaiVanBan.getMaLoaiVanBan() != null && !loaiVanBan.getMaLoaiVanBan().isBlank()) {
                code = loaiVanBan.getMaLoaiVanBan();
            }
        }
        long count = vanBanRepository.findAll().stream()
            .filter(v -> !Boolean.TRUE.equals(v.getDaXoa()))
            .map(VanBan::getSoKyHieu)
            .filter(Objects::nonNull)
            .filter(number -> number.endsWith("/" + year))
            .count();
        String soKyHieu = String.format("%02d/%s/%d", count + 1, code, year);
        return new DocumentResponses.NumberGenerateResponse(soKyHieu);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponses.NumberCheckResponse checkNumber(String soKyHieu) {
        boolean exists = vanBanRepository.existsBySoKyHieuAndDaXoaFalse(soKyHieu);
        return new DocumentResponses.NumberCheckResponse(soKyHieu, exists);
    }

    @Override
    @Transactional
    public DocumentResponses.NumberAssignResponse assignNumber(Long id, DocumentRequests.AssignNumberRequest request) {
        if (vanBanRepository.existsBySoKyHieuAndDaXoaFalse(request.soKyHieu())) {
            throw BusinessException.conflict(ErrorCode.DUPLICATE_DOCUMENT_NUMBER, "Document number already exists");
        }
        VanBan vanBan = getDocumentOrThrow(id);
        vanBan.setSoKyHieu(request.soKyHieu());
        vanBan.setNgayCapNhat(LocalDateTime.now());
        vanBanRepository.save(vanBan);
        return new DocumentResponses.NumberAssignResponse(id, request.soKyHieu());
    }

    @Override
    public DocumentResponses.DocumentVersionResponse createVersion(Long id, DocumentRequests.DocumentVersionCreateRequest request) {
        getDocumentOrThrow(id);
        DocumentResponses.DocumentVersionResponse version = new DocumentResponses.DocumentVersionResponse(
            id,
            request.versionName(),
            request.fileUrl(),
            request.noiDungThayDoi(),
            LocalDateTime.now()
        );
        versionsStore.computeIfAbsent(id, ignored -> new ArrayList<>()).removeIf(v -> v.versionName().equals(request.versionName()));
        versionsStore.computeIfAbsent(id, ignored -> new ArrayList<>()).add(version);
        // TODO: Need document version table for persistent storage.
        return version;
    }

    @Override
    public List<DocumentResponses.DocumentVersionResponse> listVersions(Long id) {
        getDocumentOrThrow(id);
        return versionsStore.getOrDefault(id, List.of()).stream()
            .sorted(Comparator.comparing(DocumentResponses.DocumentVersionResponse::createdAt))
            .toList();
    }

    @Override
    public DocumentResponses.DocumentVersionCompareResponse compareVersions(Long id, String fromVersion, String toVersion) {
        getDocumentOrThrow(id);
        List<DocumentResponses.DocumentVersionResponse> versions = versionsStore.getOrDefault(id, List.of());
        DocumentResponses.DocumentVersionResponse from = versions.stream()
            .filter(v -> v.versionName().equals(fromVersion))
            .findFirst()
            .orElseThrow(() -> BusinessException.badRequest(ErrorCode.INVALID_REQUEST, "fromVersion not found"));
        DocumentResponses.DocumentVersionResponse to = versions.stream()
            .filter(v -> v.versionName().equals(toVersion))
            .findFirst()
            .orElseThrow(() -> BusinessException.badRequest(ErrorCode.INVALID_REQUEST, "toVersion not found"));
        List<Map<String, String>> differences = List.of(Map.of(
            "field", "noiDung",
            "oldValue", from.noiDungThayDoi() == null ? "" : from.noiDungThayDoi(),
            "newValue", to.noiDungThayDoi() == null ? "" : to.noiDungThayDoi()
        ));
        // TODO: Need real diff algorithm and persistent version table.
        return new DocumentResponses.DocumentVersionCompareResponse(id, fromVersion, toVersion, differences);
    }

    @Override
    public DocumentResponses.DocumentVersionRestoreResponse restoreVersion(Long id, DocumentRequests.DocumentVersionRestoreRequest request) {
        getDocumentOrThrow(id);
        versionsStore.getOrDefault(id, List.of()).stream()
            .filter(v -> v.versionName().equals(request.versionName()))
            .findFirst()
            .orElseThrow(() -> BusinessException.badRequest(ErrorCode.INVALID_REQUEST, "versionName not found"));
        // TODO: Need persistent version snapshots to restore actual content.
        return new DocumentResponses.DocumentVersionRestoreResponse(id, request.versionName());
    }

    @Override
    public DocumentResponses.DocumentVersionDeleteResponse deleteVersion(Long id, String versionName) {
        getDocumentOrThrow(id);
        List<DocumentResponses.DocumentVersionResponse> versions = new ArrayList<>(versionsStore.getOrDefault(id, List.of()));
        boolean removed = versions.removeIf(v -> v.versionName().equals(versionName));
        if (!removed) {
            throw BusinessException.badRequest(ErrorCode.INVALID_REQUEST, "versionName not found");
        }
        versionsStore.put(id, versions);
        return new DocumentResponses.DocumentVersionDeleteResponse(id, versionName);
    }

    private void applyIncomingOutgoingFields(
        VanBan vanBan,
        String soKyHieu,
        String trichYeu,
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
        vanBan.setSoKyHieu(soKyHieu);
        vanBan.setTrichYeu(trichYeu);
        vanBan.setLoaiVanBan(findLoaiVanBan(loaiVanBanId));
        vanBan.setDonViBanHanh(donViBanHanh);
        vanBan.setNguoiKy(nguoiKy);
        vanBan.setNgayVanBan(ngayVanBan == null ? null : ngayVanBan.atStartOfDay());
        vanBan.setNgayTiepNhan(ngayTiepNhan == null ? null : ngayTiepNhan.atStartOfDay());
        vanBan.setDoMat(doMat);
        vanBan.setDoKhan(doKhan);
        vanBan.setDonViChuTriId(donViChuTriId);
        vanBan.setHanXuLy(hanXuLy);
        if (trangThai != null) {
            vanBan.setTrangThai(trangThai);
        }
    }

    private LoaiVanBan findLoaiVanBan(Integer loaiVanBanId) {
        if (loaiVanBanId == null) {
            return null;
        }
        return loaiVanBanRepository.findById(loaiVanBanId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.DOCUMENT_TYPE_NOT_FOUND, "Document type not found"));
    }

    private VanBan getDocumentOrThrow(Long id) {
        return vanBanRepository.findByIdAndDaXoaFalse(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.DOCUMENT_NOT_FOUND, "Document not found"));
    }
}

