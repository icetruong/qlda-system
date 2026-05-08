package com.qlda.aiservice.service.chatbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.aiservice.client.AuthInternalApiService;
import com.qlda.aiservice.client.DocumentInternalApiService;
import com.qlda.aiservice.client.WorkflowInternalApiService;
import com.qlda.aiservice.dto.request.ChatbotAskRequest;
import com.qlda.aiservice.entity.AiDocumentChunkEntity;
import com.qlda.aiservice.entity.AiProcessType;
import com.qlda.aiservice.entity.AiResultEntity;
import com.qlda.aiservice.repository.AiDocumentChunkRepository;
import com.qlda.aiservice.repository.AiResultRepository;
import com.qlda.aiservice.service.EmbeddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private AiResultRepository aiResultRepository;

    @Mock
    private AiDocumentChunkRepository aiDocumentChunkRepository;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private ChatbotLlmService chatbotLlmService;

    @Mock
    private DocumentInternalApiService documentInternalApiService;

    @Mock
    private WorkflowInternalApiService workflowInternalApiService;

    @Mock
    private AuthInternalApiService authInternalApiService;

    @Captor
    private ArgumentCaptor<AiResultEntity> aiResultCaptor;

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = new ChatbotService(
            aiResultRepository,
            aiDocumentChunkRepository,
            embeddingService,
            chatbotLlmService,
            documentInternalApiService,
            workflowInternalApiService,
            authInternalApiService,
            new ChatbotIntentDetector(),
            new ChatbotPromptBuilder(),
            new ChatbotResultMapper(),
            new ObjectMapper(),
            5
        );
        when(aiResultRepository.save(any())).thenAnswer(invocation -> {
            AiResultEntity entity = invocation.getArgument(0);
            entity.setId(99L);
            return entity;
        });
    }

    @Test
    void shouldUseEmbeddingAndChunkSearchForDocumentSearch() {
        when(embeddingService.generateEmbedding(anyString())).thenReturn(List.of(1.0, 0.0));
        when(aiDocumentChunkRepository.findAll()).thenReturn(List.of(
            chunk(10L, 1L, "Nội dung tài liệu đào tạo", "{\"type\":\"DOCUMENT\"}", "[1.0,0.0]")
        ));
        when(documentInternalApiService.checkDocumentAccess(2L, List.of(1L))).thenReturn(Set.of(1L));
        when(chatbotLlmService.generateAnswer(anyString(), anyString(), anyString()))
            .thenReturn(new ChatbotLlmResponse("Đã tìm thấy tài liệu phù hợp.", 91.0, "stub-model"));

        Map<String, Object> response = chatbotService.ask(new ChatbotAskRequest(
            2L,
            "Tìm tài liệu về đào tạo người dùng",
            Map.of("module", "DOCUMENT", "documentId", 1L)
        ));

        verify(embeddingService).generateEmbedding("Tìm tài liệu về đào tạo người dùng");
        verify(aiDocumentChunkRepository).findAll();
        verify(documentInternalApiService).checkDocumentAccess(2L, List.of(1L));
        verify(aiResultRepository).save(aiResultCaptor.capture());
        assertThat(aiResultCaptor.getValue().getLoaiXuLyAI()).isEqualTo(AiProcessType.CHATBOT);
        assertThat(response.get("resultId")).isEqualTo(99L);
        assertThat(response.get("intent")).isEqualTo("DOCUMENT_SEARCH");
    }

    @Test
    void shouldCallMyUploadedMetricForUploadedQuestion() {
        when(documentInternalApiService.getMyUploadedDocumentCount(2L)).thenReturn(12L);
        when(chatbotLlmService.generateAnswer(anyString(), anyString(), anyString()))
            .thenReturn(new ChatbotLlmResponse("Bạn đã upload 12 văn bản.", 96.0, "stub-model"));

        Map<String, Object> response = chatbotService.ask(new ChatbotAskRequest(
            2L,
            "Tôi đã upload bao nhiêu văn bản?",
            null
        ));

        verify(documentInternalApiService).getMyUploadedDocumentCount(2L);
        verify(workflowInternalApiService, never()).getMyDueSoonDocumentCount(anyLong(), anyInt());
        verify(authInternalApiService, never()).getTotalUserCount(anyLong());
        assertThat(response.get("intent")).isEqualTo("SYSTEM_STATISTIC");
        assertThat(response.get("metricCode")).isEqualTo("MY_UPLOADED_DOCUMENT_COUNT");
        assertThat(response.get("resultId")).isEqualTo(99L);
    }

    @Test
    void shouldCallDueSoonMetricForDueSoonQuestion() {
        when(workflowInternalApiService.getMyDueSoonDocumentCount(2L, 3)).thenReturn(5L);
        when(chatbotLlmService.generateAnswer(anyString(), anyString(), anyString()))
            .thenReturn(new ChatbotLlmResponse("Bạn có 5 văn bản sắp hết hạn.", 95.0, "stub-model"));

        Map<String, Object> response = chatbotService.ask(new ChatbotAskRequest(
            2L,
            "Tôi có bao nhiêu văn bản sắp hết hạn?",
            null
        ));

        verify(workflowInternalApiService).getMyDueSoonDocumentCount(2L, 3);
        assertThat(response.get("metricCode")).isEqualTo("MY_DUE_SOON_DOCUMENT_COUNT");
        assertThat(response.get("resultId")).isEqualTo(99L);
    }

    @Test
    void shouldCheckPermissionBeforeCallingTotalUserCountMetric() {
        when(authInternalApiService.hasSystemStatisticPermission(2L)).thenReturn(true);
        when(authInternalApiService.getTotalUserCount(2L)).thenReturn(120L);
        when(chatbotLlmService.generateAnswer(anyString(), anyString(), anyString()))
            .thenReturn(new ChatbotLlmResponse("Hệ thống có 120 người dùng.", 97.0, "stub-model"));

        chatbotService.ask(new ChatbotAskRequest(
            2L,
            "Hệ thống có bao nhiêu người dùng?",
            null
        ));

        var order = inOrder(authInternalApiService);
        order.verify(authInternalApiService).hasSystemStatisticPermission(2L);
        order.verify(authInternalApiService).getTotalUserCount(2L);
    }

    @Test
    void shouldFilterUserGuideChunksByMetadataType() {
        when(embeddingService.generateEmbedding(anyString())).thenReturn(List.of(1.0, 0.0));
        when(aiDocumentChunkRepository.findAll()).thenReturn(List.of(
            chunk(20L, 8L, "Hướng dẫn tạo văn bản đến", "{\"type\":\"huong_dan\"}", "[1.0,0.0]"),
            chunk(21L, 9L, "Nội dung văn bản nghiệp vụ", "{\"type\":\"DOCUMENT\"}", "[1.0,0.0]")
        ));
        when(chatbotLlmService.generateAnswer(anyString(), anyString(), anyString()))
            .thenReturn(new ChatbotLlmResponse("Vào menu Văn bản đến, chọn Tạo mới.", 90.0, "stub-model"));

        Map<String, Object> response = chatbotService.ask(new ChatbotAskRequest(
            2L,
            "lam sao tao van ban den",
            Map.of("module", "DOCUMENT")
        ));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sources = (List<Map<String, Object>>) response.get("sources");
        assertThat(sources).hasSize(1);
        assertThat(sources.getFirst().get("chunkId")).isEqualTo(20L);
        assertThat(sources.getFirst().get("reference")).isEqualTo("huong_dan");
        assertThat(response.get("intent")).isEqualTo("USER_GUIDE");
    }

    @Test
    void shouldReturnSafeAnswerForGeneralHelpWithoutFabrication() {
        Map<String, Object> response = chatbotService.ask(new ChatbotAskRequest(
            2L,
            "Xin chào",
            null
        ));

        verifyNoInteractions(chatbotLlmService);
        assertThat(response.get("intent")).isEqualTo("GENERAL_HELP");
        assertThat(response.get("answer")).isEqualTo("Không tìm thấy dữ liệu phù hợp.");
        assertThat(response.get("resultId")).isEqualTo(99L);
    }

    private AiDocumentChunkEntity chunk(Long chunkId, Long documentId, String content, String metadata, String embedding) {
        AiDocumentChunkEntity entity = new AiDocumentChunkEntity();
        entity.setId(chunkId);
        entity.setVanBanId(documentId);
        entity.setNoiDung(content);
        entity.setMetadata(metadata);
        entity.setEmbedding(embedding);
        entity.setChunkIndex(0);
        return entity;
    }
}
