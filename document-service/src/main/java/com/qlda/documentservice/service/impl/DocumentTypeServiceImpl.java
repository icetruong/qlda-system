package com.qlda.documentservice.service.impl;

import com.qlda.documentservice.dto.request.DocumentRequests;
import com.qlda.documentservice.dto.response.DocumentResponses;
import com.qlda.documentservice.entity.LoaiVanBan;
import com.qlda.documentservice.exception.BusinessException;
import com.qlda.documentservice.exception.ErrorCode;
import com.qlda.documentservice.mapper.DocumentMapper;
import com.qlda.documentservice.repository.LoaiVanBanRepository;
import com.qlda.documentservice.service.DocumentTypeService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentTypeServiceImpl implements DocumentTypeService {
    private final LoaiVanBanRepository loaiVanBanRepository;
    private final DocumentMapper documentMapper;

    public DocumentTypeServiceImpl(LoaiVanBanRepository loaiVanBanRepository, DocumentMapper documentMapper) {
        this.loaiVanBanRepository = loaiVanBanRepository;
        this.documentMapper = documentMapper;
    }

    @Override
    @Transactional
    public DocumentResponses.DocumentTypeResponse create(DocumentRequests.DocumentTypeCreateRequest request) {
        if (loaiVanBanRepository.existsByMaLoaiVanBan(request.maLoaiVanBan())) {
            throw BusinessException.conflict(ErrorCode.INVALID_REQUEST, "MaLoaiVanBan already exists");
        }
        LoaiVanBan loaiVanBan = new LoaiVanBan();
        loaiVanBan.setMaLoaiVanBan(request.maLoaiVanBan());
        loaiVanBan.setTenLoaiVanBan(request.tenLoaiVanBan());
        loaiVanBan.setMoTa(request.moTa());
        loaiVanBan.setSuDung(request.suDung() == null ? true : request.suDung());
        return documentMapper.toDocumentTypeResponse(loaiVanBanRepository.save(loaiVanBan));
    }

    @Override
    @Transactional
    public DocumentResponses.IdResponse update(Integer id, DocumentRequests.DocumentTypeUpdateRequest request) {
        LoaiVanBan loaiVanBan = getTypeOrThrow(id);
        loaiVanBan.setTenLoaiVanBan(request.tenLoaiVanBan());
        loaiVanBan.setMoTa(request.moTa());
        if (request.suDung() != null) {
            loaiVanBan.setSuDung(request.suDung());
        }
        loaiVanBanRepository.save(loaiVanBan);
        return new DocumentResponses.IdResponse(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponses.DocumentTypeResponse> list(String keyword, Boolean suDung) {
        return loaiVanBanRepository.findAll().stream()
            .filter(item -> {
                if (keyword == null || keyword.isBlank()) {
                    return true;
                }
                String key = keyword.toLowerCase();
                return (item.getMaLoaiVanBan() != null && item.getMaLoaiVanBan().toLowerCase().contains(key))
                    || (item.getTenLoaiVanBan() != null && item.getTenLoaiVanBan().toLowerCase().contains(key));
            })
            .filter(item -> suDung == null || suDung.equals(item.getSuDung()))
            .map(documentMapper::toDocumentTypeResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponses.DocumentTypeResponse detail(Integer id) {
        return documentMapper.toDocumentTypeResponse(getTypeOrThrow(id));
    }

    @Override
    @Transactional
    public DocumentResponses.IdResponse delete(Integer id) {
        LoaiVanBan loaiVanBan = getTypeOrThrow(id);
        loaiVanBan.setSuDung(false);
        loaiVanBanRepository.save(loaiVanBan);
        return new DocumentResponses.IdResponse(id);
    }

    private LoaiVanBan getTypeOrThrow(Integer id) {
        return loaiVanBanRepository.findById(id)
            .orElseThrow(() -> BusinessException.notFound(ErrorCode.DOCUMENT_TYPE_NOT_FOUND, "Document type not found"));
    }
}

