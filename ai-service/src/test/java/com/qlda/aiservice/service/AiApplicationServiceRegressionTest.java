package com.qlda.aiservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.aiservice.client.DocumentInternalApiService;
import com.qlda.aiservice.dto.internal.DocumentMetadataDto;
import com.qlda.aiservice.dto.request.ClassifyRequest;
import com.qlda.aiservice.dto.request.IndexDocumentRequest;
import com.qlda.aiservice.dto.request.MetadataExtractRequest;
import com.qlda.aiservice.dto.request.SemanticSearchRequest;
import com.qlda.aiservice.dto.request.SummarizeRequest;
import com.qlda.aiservice.entity.AiDocumentChunkEntity;
import com.qlda.aiservice.entity.AiResultEntity;
import com.qlda.aiservice.exception.AppException;
import com.qlda.aiservice.repository.AiDocumentChunkRepository;
import com.qlda.aiservice.repository.AiResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiApplicationServiceRegressionTest {

    @Mock
    private AiResultRepository aiResultRepository;

    @Mock
    private AiDocumentChunkRepository aiDocumentChunkRepository;

    @Mock
    private AiModelService aiModelService;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private OcrService ocrService;

    @Mock
    private DocumentInternalApiService documentInternalApiService;

    private AiApplicationService aiApplicationService;

    @BeforeEach
    void setUp() {
        aiApplicationService = new AiApplicationService(
            aiResultRepository,
            aiDocumentChunkRepository,
            aiModelService,
            embeddingService,
            ocrService,
            new ObjectMapper(),
            documentInternalApiService
        );

        when(aiResultRepository.save(any())).thenAnswer(invocation -> {
            AiResultEntity entity = invocation.getArgument(0);
            entity.setId(99L);
            return entity;
        });
        when(documentInternalApiService.getDocument(any())).thenReturn(
            new DocumentMetadataDto(1L, "stub", 1L, 1L, 1L, "2026-05-08T10:00:00")
        );
    }

    @Test
    void shouldRejectEmptyUploadFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> aiApplicationService.summarizeFile(1L, 2L, "SHORT", "vi", emptyFile))
            .isInstanceOf(AppException.class);
    }

    @Test
    void shouldRejectUnsupportedSummaryType() {
        SummarizeRequest request = new SummarizeRequest(1L, 2L, "Noi dung", "INVALID", "vi");

        assertThatThrownBy(() -> aiApplicationService.summarize(request))
            .isInstanceOf(AppException.class);
    }

    @Test
    void classifyFileShouldUseDefaultAdministrativeCategories() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "abc".getBytes());
        when(ocrService.extractText(any())).thenReturn("Noi dung can phan loai");
        when(aiModelService.classify(any(), any(), any()))
            .thenReturn(new ClassificationOutput("CONG_VAN", "Cong van", 91.0, "Ly do", "model"));

        aiApplicationService.classifyFile(1L, 2L, "vi", file);

        ArgumentCaptor<List<String>> categoriesCaptor = ArgumentCaptor.forClass(List.class);
        verify(aiModelService).classify(any(), categoriesCaptor.capture(), any());
        assertThat(categoriesCaptor.getValue()).containsExactly(
            "CONG_VAN",
            "QUYET_DINH",
            "THONG_BAO",
            "KE_HOACH",
            "BAO_CAO"
        );
    }

    @Test
    void extractMetadataFileShouldUseExtendedDefaultFields() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "abc".getBytes());
        when(ocrService.extractText(any())).thenReturn("Noi dung can trich xuat");
        when(aiModelService.extractMetadata(any(), any(), any()))
            .thenReturn(new MetadataOutput(Map.of("soKyHieu", "123/CV"), 90.0, "model"));

        aiApplicationService.extractMetadataFile(1L, 2L, "vi", file);

        ArgumentCaptor<List<String>> fieldsCaptor = ArgumentCaptor.forClass(List.class);
        verify(aiModelService).extractMetadata(any(), fieldsCaptor.capture(), any());
        assertThat(fieldsCaptor.getValue()).containsExactly(
            "soKyHieu",
            "ngayVanBan",
            "nguoiKy",
            "doKhan"
        );
    }

    @Test
    void semanticSearchShouldApplyMetadataFilters() {
        when(embeddingService.generateEmbedding("office")).thenReturn(List.of(1.0, 0.0));
        when(aiDocumentChunkRepository.findAll()).thenReturn(List.of(
            AiDocumentChunkEntity.builder()
                .id(1L)
                .vanBanId(10L)
                .chunkIndex(0)
                .noiDung("chunk-1")
                .embedding("[1.0,0.0]")
                .metadata("{\"loaiVanBanId\":1}")
                .build(),
            AiDocumentChunkEntity.builder()
                .id(2L)
                .vanBanId(11L)
                .chunkIndex(1)
                .noiDung("chunk-2")
                .embedding("[1.0,0.0]")
                .metadata("{\"loaiVanBanId\":2}")
                .build()
        ));

        Map<String, Object> result = aiApplicationService.semanticSearch(
            new SemanticSearchRequest("office", 2L, Map.of("loaiVanBanId", 1), 0, 10)
        );

        List<?> content = (List<?>) result.get("content");
        assertThat(content).hasSize(1);
    }

    @Test
    void semanticSearchShouldRejectInvalidDocumentIdFilter() {
        when(embeddingService.generateEmbedding("office")).thenReturn(List.of(1.0, 0.0));
        when(aiDocumentChunkRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> aiApplicationService.semanticSearch(
            new SemanticSearchRequest("office", 2L, Map.of("documentId", "abc"), 0, 10)
        )).isInstanceOf(AppException.class);
    }

    @Test
    void indexDocumentShouldReplaceOldIndexBeforeSavingNewChunks() {
        when(embeddingService.generateEmbedding(any())).thenReturn(List.of(1.0, 0.0));

        aiApplicationService.indexDocument(new IndexDocumentRequest(
            100L,
            null,
            "A".repeat(800),
            Map.of("loaiVanBanId", 1)
        ));

        var order = inOrder(aiDocumentChunkRepository);
        order.verify(aiDocumentChunkRepository).deleteByVanBanId(100L);
        order.verify(aiDocumentChunkRepository).saveAll(any());
    }

    @Test
    void metadataExtractShouldRejectEmptyFields() {
        MetadataExtractRequest request = new MetadataExtractRequest(1L, 2L, "Noi dung", List.of(), "vi");

        assertThatThrownBy(() -> aiApplicationService.extractMetadata(request))
            .isInstanceOf(AppException.class);
    }

    @Test
    void classifyShouldRejectEmptyCategories() {
        ClassifyRequest request = new ClassifyRequest(1L, 2L, "Noi dung", List.of(), "vi");

        assertThatThrownBy(() -> aiApplicationService.classify(request))
            .isInstanceOf(AppException.class);
    }
}
