package com.qlda.documentservice.service.impl;

import com.qlda.documentservice.common.DocumentConstants;
import com.qlda.documentservice.common.PageResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.entity.HoSoCongViec;
import com.qlda.documentservice.entity.VanBan;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.mapper.DocumentMapper;
import com.qlda.documentservice.repository.HoSoCongViecRepository;
import com.qlda.documentservice.repository.VanBanRepository;
import com.qlda.documentservice.service.CaseFileService;
import com.qlda.documentservice.specification.HoSoCongViecSpecification;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CaseFileServiceImpl implements CaseFileService {
    private final HoSoCongViecRepository hoSoCongViecRepository;
    private final VanBanRepository vanBanRepository;
    private final DocumentMapper documentMapper;
    private final Map<Long, String> classificationStore = new ConcurrentHashMap<>();

    public CaseFileServiceImpl(
        HoSoCongViecRepository hoSoCongViecRepository,
        VanBanRepository vanBanRepository,
        DocumentMapper documentMapper
    ) {
        this.hoSoCongViecRepository = hoSoCongViecRepository;
        this.vanBanRepository = vanBanRepository;
        this.documentMapper = documentMapper;
    }

    @Override
    @Transactional
    public DocumentResponses.CaseFileSimpleResponse create(DocumentRequests.CaseFileCreateRequest request) {
        if (hoSoCongViecRepository.existsByMaHoSo(request.maHoSo())) {
            throw BusinessException.conflict(ErrorCode.INVALID_REQUEST, "MaHoSo already exists");
        }
        HoSoCongViec hoSoCongViec = new HoSoCongViec();
        hoSoCongViec.setMaHoSo(request.maHoSo());
        hoSoCongViec.setTenHoSo(request.tenHoSo());
        hoSoCongViec.setVanBan(findDocument(request.vanBanId()));
        hoSoCongViec.setNguoiPhuTrachId(request.nguoiPhuTrachId());
        hoSoCongViec.setDonViId(request.donViId());
        hoSoCongViec.setTrangThai(request.trangThai());
        hoSoCongViec.setGhiChu(request.ghiChu());
        hoSoCongViec.setNgayMoHoSo(LocalDateTime.now());
        HoSoCongViec saved = hoSoCongViecRepository.save(hoSoCongViec);
        return new DocumentResponses.CaseFileSimpleResponse(saved.getId(), saved.getMaHoSo());
    }

    @Override
    @Transactional
    public DocumentResponses.IdResponse update(Long id, DocumentRequests.CaseFileUpdateRequest request) {
        HoSoCongViec hoSoCongViec = getCaseFileOrThrow(id);
        hoSoCongViec.setTenHoSo(request.tenHoSo());
        hoSoCongViec.setNguoiPhuTrachId(request.nguoiPhuTrachId());
        hoSoCongViec.setDonViId(request.donViId());
        hoSoCongViec.setTrangThai(request.trangThai());
        hoSoCongViec.setGhiChu(request.ghiChu());
        hoSoCongViecRepository.save(hoSoCongViec);
        return new DocumentResponses.IdResponse(id);
    }

    @Override
    @Transactional
    public DocumentResponses.CaseFileAttachResponse attachDocument(Long id, DocumentRequests.CaseFileAttachDocumentRequest request) {
        HoSoCongViec hoSoCongViec = getCaseFileOrThrow(id);
        VanBan vanBan = findDocument(request.vanBanId());
        hoSoCongViec.setVanBan(vanBan);
        hoSoCongViecRepository.save(hoSoCongViec);
        // TODO: Current schema supports only one VanBanID per case file. Add junction table if multi-document per case file is required.
        return new DocumentResponses.CaseFileAttachResponse(id, request.vanBanId());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DocumentResponses.CaseFileListItemResponse> list(
        String keyword,
        Integer donViId,
        Long nguoiPhuTrachId,
        Integer trangThai,
        Pageable pageable
    ) {
        Specification<HoSoCongViec> spec = Specification.where(HoSoCongViecSpecification.keyword(keyword))
            .and(HoSoCongViecSpecification.donViId(donViId))
            .and(HoSoCongViecSpecification.nguoiPhuTrachId(nguoiPhuTrachId))
            .and(HoSoCongViecSpecification.trangThai(trangThai));
        Page<HoSoCongViec> page = hoSoCongViecRepository.findAll(spec, pageable);
        return new PageResponse<>(
            page.getContent().stream().map(documentMapper::toCaseFileListItemResponse).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponses.CaseFileDetailResponse detail(Long id) {
        return documentMapper.toCaseFileDetailResponse(getCaseFileOrThrow(id));
    }

    @Override
    @Transactional
    public DocumentResponses.IdResponse delete(Long id) {
        HoSoCongViec hoSoCongViec = getCaseFileOrThrow(id);
        hoSoCongViec.setTrangThai(DocumentConstants.TRANG_THAI_HO_SO_DA_XOA);
        hoSoCongViec.setNgayDongHoSo(LocalDateTime.now());
        hoSoCongViecRepository.save(hoSoCongViec);
        return new DocumentResponses.IdResponse(id);
    }

    @Override
    @Transactional
    public DocumentResponses.CaseFileClassificationResponse classify(Long id, DocumentRequests.CaseFileClassificationRequest request) {
        HoSoCongViec hoSoCongViec = getCaseFileOrThrow(id);
        classificationStore.put(id, request.nhomHoSo());
        String note = request.ghiChu() == null ? "" : request.ghiChu();
        hoSoCongViec.setGhiChu("[NhomHoSo:" + request.nhomHoSo() + "] " + note);
        hoSoCongViecRepository.save(hoSoCongViec);
        // TODO: Schema has no dedicated classification field/table. Persist temporary classification in GhiChu + in-memory map.
        return new DocumentResponses.CaseFileClassificationResponse(id, request.nhomHoSo());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DocumentResponses.CaseFileClassificationItemResponse> searchClassification(String nhomHoSo, Pageable pageable) {
        List<HoSoCongViec> filtered = hoSoCongViecRepository.findAll().stream()
            .filter(item -> {
                String classified = resolveClassification(item);
                if (nhomHoSo == null || nhomHoSo.isBlank()) {
                    return true;
                }
                return nhomHoSo.equalsIgnoreCase(classified);
            })
            .sorted(Comparator.comparing(HoSoCongViec::getId))
            .toList();

        int start = Math.min((int) pageable.getOffset(), filtered.size());
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        Page<HoSoCongViec> page = new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
        List<DocumentResponses.CaseFileClassificationItemResponse> content = page.getContent().stream()
            .map(item -> new DocumentResponses.CaseFileClassificationItemResponse(
                item.getId(),
                item.getMaHoSo(),
                item.getTenHoSo(),
                resolveClassification(item)
            ))
            .toList();
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private String resolveClassification(HoSoCongViec hoSoCongViec) {
        String direct = classificationStore.get(hoSoCongViec.getId());
        if (direct != null) {
            return direct;
        }
        String note = hoSoCongViec.getGhiChu();
        if (note != null && note.startsWith("[NhomHoSo:")) {
            int endIndex = note.indexOf("]");
            if (endIndex > 10) {
                return note.substring(10, endIndex);
            }
        }
        return null;
    }

    private HoSoCongViec getCaseFileOrThrow(Long id) {
        return hoSoCongViecRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.CASE_FILE_NOT_FOUND, "Case file not found"));
    }

    private VanBan findDocument(Long vanBanId) {
        if (vanBanId == null) {
            return null;
        }
        return vanBanRepository.findByIdAndDaXoaFalse(vanBanId)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.DOCUMENT_NOT_FOUND, "Document not found"));
    }
}

