package com.qlda.documentservice.service.impl;

import com.qlda.documentservice.client.AiServiceClient;
import com.qlda.documentservice.client.dto.AiClientDtos;
import com.qlda.documentservice.common.DocumentConstants;
import com.qlda.documentservice.common.PageResponse;
import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.entity.LoaiVanBan;
import com.qlda.documentservice.entity.TemplateVanBan;
import com.qlda.documentservice.entity.VanBan;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.mapper.DocumentMapper;
import com.qlda.documentservice.repository.LoaiVanBanRepository;
import com.qlda.documentservice.repository.TemplateVanBanRepository;
import com.qlda.documentservice.repository.VanBanRepository;
import com.qlda.documentservice.security.SecurityUtils;
import com.qlda.documentservice.service.TemplateService;
import com.qlda.documentservice.specification.TemplateVanBanSpecification;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TemplateServiceImpl implements TemplateService {
    private static final Logger log = LoggerFactory.getLogger(TemplateServiceImpl.class);
    private static final String SOURCE_SERVICE = "document-service";

    private final TemplateVanBanRepository templateVanBanRepository;
    private final LoaiVanBanRepository loaiVanBanRepository;
    private final VanBanRepository vanBanRepository;
    private final DocumentMapper documentMapper;
    private final SecurityUtils securityUtils;
    private final AiServiceClient aiServiceClient;

    public TemplateServiceImpl(
        TemplateVanBanRepository templateVanBanRepository,
        LoaiVanBanRepository loaiVanBanRepository,
        VanBanRepository vanBanRepository,
        DocumentMapper documentMapper,
        SecurityUtils securityUtils,
        AiServiceClient aiServiceClient
    ) {
        this.templateVanBanRepository = templateVanBanRepository;
        this.loaiVanBanRepository = loaiVanBanRepository;
        this.vanBanRepository = vanBanRepository;
        this.documentMapper = documentMapper;
        this.securityUtils = securityUtils;
        this.aiServiceClient = aiServiceClient;
    }

    @Override
    @Transactional
    public DocumentResponses.TemplateSimpleResponse create(DocumentRequests.TemplateCreateRequest request) {
        if (templateVanBanRepository.existsByMaTemplate(request.maTemplate())) {
            throw BusinessException.conflict(ErrorCode.INVALID_REQUEST, "MaTemplate already exists");
        }
        TemplateVanBan templateVanBan = new TemplateVanBan();
        templateVanBan.setMaTemplate(request.maTemplate());
        templateVanBan.setTenTemplate(request.tenTemplate());
        templateVanBan.setLoaiVanBan(findLoaiVanBan(request.loaiVanBanId()));
        templateVanBan.setNoiDungMau(request.noiDungMau());
        templateVanBan.setTepMau(request.tepMau());
        templateVanBan.setSuDung(request.suDung() == null ? true : request.suDung());
        securityUtils.getCurrentUserId().ifPresent(templateVanBan::setNguoiTaoId);
        templateVanBan = templateVanBanRepository.save(templateVanBan);
        return new DocumentResponses.TemplateSimpleResponse(templateVanBan.getId(), templateVanBan.getMaTemplate());
    }

    @Override
    @Transactional
    public DocumentResponses.IdResponse update(Integer id, DocumentRequests.TemplateUpdateRequest request) {
        TemplateVanBan templateVanBan = getTemplateOrThrow(id);
        templateVanBan.setTenTemplate(request.tenTemplate());
        templateVanBan.setLoaiVanBan(findLoaiVanBan(request.loaiVanBanId()));
        templateVanBan.setNoiDungMau(request.noiDungMau());
        templateVanBan.setTepMau(request.tepMau());
        if (request.suDung() != null) {
            templateVanBan.setSuDung(request.suDung());
        }
        templateVanBanRepository.save(templateVanBan);
        return new DocumentResponses.IdResponse(id);
    }

    @Override
    @Transactional
    public DocumentResponses.IdResponse delete(Integer id) {
        TemplateVanBan templateVanBan = getTemplateOrThrow(id);
        templateVanBan.setSuDung(false);
        templateVanBanRepository.save(templateVanBan);
        return new DocumentResponses.IdResponse(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DocumentResponses.TemplateListItemResponse> list(String keyword, Integer loaiVanBanId, Boolean suDung, Pageable pageable) {
        Specification<TemplateVanBan> spec = Specification.where(TemplateVanBanSpecification.keyword(keyword))
            .and(TemplateVanBanSpecification.loaiVanBanId(loaiVanBanId))
            .and(TemplateVanBanSpecification.suDung(suDung));
        Page<TemplateVanBan> page = templateVanBanRepository.findAll(spec, pageable);
        return new PageResponse<>(
            page.getContent().stream().map(documentMapper::toTemplateListItemResponse).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponses.TemplateDetailResponse detail(Integer id) {
        return documentMapper.toTemplateDetailResponse(getTemplateOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponses.ApplyTemplateResponse apply(Integer templateId, DocumentRequests.ApplyTemplateRequest request) {
        TemplateVanBan templateVanBan = getTemplateOrThrow(templateId);
        String replacedContent = replacePlaceholder(templateVanBan.getNoiDungMau(), request.replaceData());
        return new DocumentResponses.ApplyTemplateResponse(request.documentId(), templateId, replacedContent);
    }

    @Override
    @Transactional
    public DocumentResponses.CreateFromTemplateResponse createFromTemplate(DocumentRequests.CreateFromTemplateRequest request) {
        TemplateVanBan templateVanBan = getTemplateOrThrow(request.templateId());
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
        vanBan = vanBanRepository.save(vanBan);
        requestIndexDocument(vanBan.getId());

        // TODO: Current implementation stores applied content in-memory only. Add docx/file generation when template engine is available.
        replacePlaceholder(templateVanBan.getNoiDungMau(), request.replaceData());
        return new DocumentResponses.CreateFromTemplateResponse(vanBan.getId(), templateVanBan.getId(), vanBan.getTrangThai());
    }

    private String replacePlaceholder(String rawContent, Map<String, String> replaceData) {
        if (rawContent == null) {
            return "";
        }
        if (replaceData == null || replaceData.isEmpty()) {
            return rawContent;
        }
        String output = rawContent;
        for (Map.Entry<String, String> entry : replaceData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() == null ? "" : entry.getValue();
            output = output.replace("{{" + key + "}}", value);
            output = output.replace("${" + key + "}", value);
        }
        return output;
    }

    private TemplateVanBan getTemplateOrThrow(Integer id) {
        return templateVanBanRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.TEMPLATE_NOT_FOUND, "Template not found"));
    }

    private LoaiVanBan findLoaiVanBan(Integer id) {
        if (id == null) {
            return null;
        }
        return loaiVanBanRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.DOCUMENT_TYPE_NOT_FOUND, "Document type not found"));
    }

    private void requestIndexDocument(Long documentId) {
        try {
            aiServiceClient.indexDocument(documentId, new AiClientDtos.IndexDocumentRequest(SOURCE_SERVICE));
        } catch (Exception ex) {
            log.warn("Index document failed after create from template: documentId={}", documentId, ex);
        }
    }
}
