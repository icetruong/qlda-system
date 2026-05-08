package com.qlda.aiservice.service;

import com.qlda.aiservice.client.DocumentInternalApiService;
import com.qlda.aiservice.dto.internal.DocumentMetadataDto;
import com.qlda.aiservice.dto.request.ChatbotAskRequest;
import com.qlda.aiservice.dto.request.ClassifyRequest;
import com.qlda.aiservice.dto.request.IndexDocumentRequest;
import com.qlda.aiservice.dto.request.SummarizeRequest;
import com.qlda.aiservice.entity.AiDocumentChunkEntity;
import com.qlda.aiservice.entity.AiResultEntity;
import com.qlda.aiservice.entity.AiProcessType;
import com.qlda.aiservice.exception.AppException;
import com.qlda.aiservice.exception.ErrorCode;
import com.qlda.aiservice.repository.AiDocumentChunkRepository;
import com.qlda.aiservice.repository.AiResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
class AiApplicationServiceTest {

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

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AiApplicationService aiApplicationService;

    @Captor
    private ArgumentCaptor<AiResultEntity> resultCaptor;

    @Captor
    private ArgumentCaptor<List<AiDocumentChunkEntity>> chunkListCaptor;

    @BeforeEach
    void setup() {
        Mockito.lenient().when(aiResultRepository.save(any())).thenAnswer(invocation -> {
            AiResultEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(1L);
            }
            return entity;
        });
        Mockito.lenient().when(documentInternalApiService.getDocument(any())).thenReturn(
            new DocumentMetadataDto(1L, "stub", 1L, 1L, 1L, "2026-05-10T10:00:00")
        );
    }

    @Test
    void shouldSaveSummaryResultWithSummaryType() {
        SummarizeRequest request = new SummarizeRequest(1L, 2L, "Noi dung can tom tat", "SHORT", "vi");
        when(aiModelService.summarize(any(), any(), any()))
            .thenReturn(new SummarizationOutput("Tom tat", 91.5, "stub-model"));

        aiApplicationService.summarize(request);

        verify(aiResultRepository).save(resultCaptor.capture());
        assertThat(resultCaptor.getValue().getLoaiXuLyAI()).isEqualTo(AiProcessType.SUMMARY);
        assertThat(resultCaptor.getValue().getVanBanID()).isEqualTo(1L);
        assertThat(resultCaptor.getValue().getNguoiYeuCauID()).isEqualTo(2L);
    }

    @Test
    void shouldSaveClassificationResultWithCorrectType() {
        ClassifyRequest request = new ClassifyRequest(1L, 2L, "Noi dung", List.of("CONG_VAN"), "vi");
        when(aiModelService.classify(any(), any(), any()))
            .thenReturn(new ClassificationOutput("CONG_VAN", "Cong van", 93.2, "Phu hop", "stub-model"));

        aiApplicationService.classify(request);

        verify(aiResultRepository).save(resultCaptor.capture());
        assertThat(resultCaptor.getValue().getLoaiXuLyAI()).isEqualTo(AiProcessType.CLASSIFICATION);
    }

    @Test
    void shouldCreateChunksWhenIndexDocument() {
        when(embeddingService.generateEmbedding(any())).thenReturn(List.of(0.1, 0.2, 0.3));
        IndexDocumentRequest request = new IndexDocumentRequest(
            9L,
            null,
            "A".repeat(700),
            Map.of("loaiVanBanId", 1)
        );

        aiApplicationService.indexDocument(request);

        verify(aiDocumentChunkRepository).saveAll(chunkListCaptor.capture());
        List<AiDocumentChunkEntity> chunks = chunkListCaptor.getValue();
        assertThat(chunks.size()).isGreaterThan(1);
        assertThat(chunks.getFirst().getChunkIndex()).isEqualTo(0);
        assertThat(chunks.get(1).getChunkIndex()).isEqualTo(1);
    }

    @Test
    void shouldDeleteIndexByDocumentId() {
        aiApplicationService.deleteDocumentIndex(100L);
        verify(aiDocumentChunkRepository).deleteByVanBanId(100L);
    }

    @Test
    void shouldFetchRelevantChunksBeforeGeneratingChatbotAnswer() {
        when(embeddingService.generateEmbedding("Hoi gi do")).thenReturn(List.of(1.0, 0.0));
        AiDocumentChunkEntity chunk = AiDocumentChunkEntity.builder()
            .id(1L)
            .vanBanId(10L)
            .chunkIndex(0)
            .noiDung("Noi dung lien quan")
            .embedding("[1.0,0.0]")
            .metadata("{\"module\":\"DOCUMENT\"}")
            .build();
        when(aiDocumentChunkRepository.findAll()).thenReturn(List.of(chunk));
        when(aiModelService.answerWithContext(any(), any()))
            .thenReturn(new ChatbotOutput("Tra loi", 88.0, "stub-model"));

        ChatbotAskRequest request = new ChatbotAskRequest(2L, "Hoi gi do", Map.of("module", "DOCUMENT"));
        aiApplicationService.askChatbot(request);

        var order = inOrder(embeddingService, aiDocumentChunkRepository, aiModelService);
        order.verify(embeddingService).generateEmbedding("Hoi gi do");
        order.verify(aiDocumentChunkRepository).findAll();
        order.verify(aiModelService).answerWithContext(any(), any());
        verify(aiResultRepository).save(resultCaptor.capture());
        assertThat(resultCaptor.getValue().getLoaiXuLyAI()).isEqualTo(AiProcessType.CHATBOT);
    }

    @Test
    void shouldRejectInvalidFileFormat() {
        MockMultipartFile invalid = new MockMultipartFile("file", "bad.exe", "application/octet-stream", "abc".getBytes());

        assertThatThrownBy(() -> aiApplicationService.summarizeFile(1L, 2L, "SHORT", "vi", invalid))
            .isInstanceOf(AppException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_FILE_FORMAT);
    }
}
