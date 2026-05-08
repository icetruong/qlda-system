package com.qlda.aiservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.aiservice.client.DocumentInternalApiService;
import com.qlda.aiservice.dto.request.ChatbotAskRequest;
import com.qlda.aiservice.dto.request.ClassifyRequest;
import com.qlda.aiservice.dto.request.IndexDocumentRequest;
import com.qlda.aiservice.dto.request.MetadataExtractRequest;
import com.qlda.aiservice.dto.request.SemanticSearchRequest;
import com.qlda.aiservice.dto.request.SuggestionHandlingRequest;
import com.qlda.aiservice.dto.request.SuggestionReplyRequest;
import com.qlda.aiservice.dto.request.SummarizeRequest;
import com.qlda.aiservice.entity.AiDocumentChunkEntity;
import com.qlda.aiservice.entity.AiProcessType;
import com.qlda.aiservice.entity.AiResultEntity;
import com.qlda.aiservice.exception.AppException;
import com.qlda.aiservice.exception.ErrorCode;
import com.qlda.aiservice.repository.AiDocumentChunkRepository;
import com.qlda.aiservice.repository.AiResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AiApplicationService {

    private static final int CHUNK_SIZE = 500;
    private static final int CHATBOT_TOP_K = 3;
    private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of("pdf", "doc", "docx", "txt", "png", "jpg", "jpeg");
    private static final Set<String> ALLOWED_SUMMARY_TYPES = Set.of("SHORT", "DETAILED", "BULLET");
    private static final List<String> DEFAULT_CLASSIFICATION_CATEGORIES = List.of(
        "CONG_VAN",
        "QUYET_DINH",
        "THONG_BAO",
        "KE_HOACH",
        "BAO_CAO"
    );
    private static final List<String> DEFAULT_METADATA_FIELDS = List.of(
        "soKyHieu",
        "ngayVanBan",
        "nguoiKy",
        "doKhan"
    );

    private final AiResultRepository aiResultRepository;
    private final AiDocumentChunkRepository aiDocumentChunkRepository;
    private final AiModelService aiModelService;
    private final EmbeddingService embeddingService;
    private final OcrService ocrService;
    private final ObjectMapper objectMapper;
    private final DocumentInternalApiService documentInternalApiService;

    public Map<String, Object> summarize(SummarizeRequest request) {
        validateSummarizeRequest(request);
        ensureDocumentExists(request.documentId());
        SummarizationOutput output = aiModelService.summarize(
            request.text(),
            request.summaryType().trim().toUpperCase(),
            normalizeLanguage(request.language())
        );
        AiResultEntity saved = saveResult(
            request.documentId(),
            request.userId(),
            AiProcessType.SUMMARY,
            request.text(),
            output.summary(),
            output.confidence(),
            output.modelUsed(),
            "summaryType=" + request.summaryType().trim().toUpperCase() + ",language=" + normalizeLanguage(request.language())
        );
        return Map.of(
            "resultId", saved.getId(),
            "documentId", request.documentId(),
            "summaryType", request.summaryType().trim().toUpperCase(),
            "summary", output.summary(),
            "modelUsed", output.modelUsed(),
            "confidence", output.confidence()
        );
    }

    public Map<String, Object> summarizeFile(Long documentId, Long userId, String summaryType, String language, MultipartFile file) {
        validateFile(file);
        String text = ocrService.extractText(file);
        Map<String, Object> data = summarize(new SummarizeRequest(documentId, userId, text, summaryType, language));
        return merge(data, Map.of("fileName", file.getOriginalFilename()));
    }

    public Map<String, Object> classify(ClassifyRequest request) {
        validateClassifyRequest(request);
        ensureDocumentExists(request.documentId());
        ClassificationOutput output = aiModelService.classify(request.text(), request.categories(), normalizeLanguage(request.language()));
        AiResultEntity saved = saveResult(
            request.documentId(),
            request.userId(),
            AiProcessType.CLASSIFICATION,
            request.text(),
            toJson(Map.of("category", output.category(), "reason", output.reason())),
            output.confidence(),
            output.modelUsed(),
            output.reason()
        );
        return Map.of(
            "resultId", saved.getId(),
            "documentId", request.documentId(),
            "category", output.category(),
            "categoryName", output.categoryName(),
            "confidence", output.confidence(),
            "reason", output.reason(),
            "modelUsed", output.modelUsed()
        );
    }

    public Map<String, Object> classifyFile(Long documentId, Long userId, String language, MultipartFile file) {
        validateFile(file);
        String text = ocrService.extractText(file);
        Map<String, Object> data = classify(new ClassifyRequest(documentId, userId, text, DEFAULT_CLASSIFICATION_CATEGORIES, language));
        return merge(data, Map.of("fileName", file.getOriginalFilename()));
    }

    public Map<String, Object> extractMetadata(MetadataExtractRequest request) {
        validateMetadataRequest(request);
        ensureDocumentExists(request.documentId());
        MetadataOutput output = aiModelService.extractMetadata(request.text(), request.fields(), normalizeLanguage(request.language()));
        AiResultEntity saved = saveResult(
            request.documentId(),
            request.userId(),
            AiProcessType.METADATA_EXTRACTION,
            request.text(),
            toJson(output.metadata()),
            output.confidence(),
            output.modelUsed(),
            "fields=" + String.join(",", request.fields())
        );
        return Map.of(
            "resultId", saved.getId(),
            "documentId", request.documentId(),
            "metadata", output.metadata(),
            "confidence", output.confidence(),
            "modelUsed", output.modelUsed()
        );
    }

    public Map<String, Object> extractMetadataFile(Long documentId, Long userId, String language, MultipartFile file) {
        validateFile(file);
        String text = ocrService.extractText(file);
        Map<String, Object> data = extractMetadata(
            new MetadataExtractRequest(documentId, userId, text, DEFAULT_METADATA_FIELDS, language)
        );
        return merge(data, Map.of("fileName", file.getOriginalFilename()));
    }

    public Map<String, Object> semanticSearch(SemanticSearchRequest request) {
        List<Double> keywordEmbedding = embeddingService.generateEmbedding(request.keyword());
        List<RankedChunk> ranked = rankChunks(keywordEmbedding, request.filters());
        int page = request.page() == null ? 0 : Math.max(0, request.page());
        int size = request.size() == null ? 10 : Math.max(1, request.size());

        int from = page * size;
        int to = Math.min(from + size, ranked.size());
        List<Map<String, Object>> content = from >= ranked.size()
            ? List.of()
            : ranked.subList(from, to).stream().map(this::toSearchItem).toList();

        return Map.of(
            "content", content,
            "page", page,
            "size", size,
            "totalElements", ranked.size()
        );
    }

    public Map<String, Object> indexDocument(IndexDocumentRequest request) {
        ensureDocumentExists(request.documentId());
        aiDocumentChunkRepository.deleteByVanBanId(request.documentId());
        List<String> chunks = splitToChunks(request.text(), CHUNK_SIZE);
        List<AiDocumentChunkEntity> entities = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            AiDocumentChunkEntity entity = AiDocumentChunkEntity.builder()
                .vanBanId(request.documentId())
                .tepDinhKemId(request.attachmentId())
                .chunkIndex(i)
                .noiDung(chunkText)
                .embedding(toJson(embeddingService.generateEmbedding(chunkText)))
                .metadata(toJson(request.metadata()))
                .ngayTao(LocalDateTime.now())
                .build();
            entities.add(entity);
        }
        if (!entities.isEmpty()) {
            aiDocumentChunkRepository.saveAll(entities);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("documentId", request.documentId());
        result.put("attachmentId", request.attachmentId());
        result.put("totalChunks", entities.size());
        result.put("indexed", true);
        return result;
    }

    public Map<String, Object> deleteDocumentIndex(Long documentId) {
        ensureDocumentExists(documentId);
        aiDocumentChunkRepository.deleteByVanBanId(documentId);
        return Map.of("documentId", documentId, "deleted", true);
    }

    public Map<String, Object> suggestHandling(SuggestionHandlingRequest request) {
        ensureDocumentExists(request.documentId());
        SuggestionHandlingOutput output = aiModelService.suggestHandling(request.text(), request.context());
        AiResultEntity saved = saveResult(
            request.documentId(),
            request.userId(),
            AiProcessType.SUGGESTION_HANDLING,
            request.text(),
            toJson(output.suggestions()),
            output.confidence(),
            output.modelUsed(),
            null
        );
        return Map.of(
            "resultId", saved.getId(),
            "documentId", request.documentId(),
            "suggestions", output.suggestions(),
            "confidence", output.confidence(),
            "modelUsed", output.modelUsed()
        );
    }

    public Map<String, Object> suggestReply(SuggestionReplyRequest request) {
        ensureDocumentExists(request.documentId());
        SuggestionReplyOutput output = aiModelService.suggestReply(request.text(), request.replyStyle(), request.language());
        AiResultEntity saved = saveResult(
            request.documentId(),
            request.userId(),
            AiProcessType.SUGGESTION_REPLY,
            request.text(),
            output.suggestedReply(),
            output.confidence(),
            output.modelUsed(),
            "replyStyle=" + request.replyStyle()
        );
        return Map.of(
            "resultId", saved.getId(),
            "documentId", request.documentId(),
            "suggestedReply", output.suggestedReply(),
            "replyStyle", output.replyStyle(),
            "modelUsed", output.modelUsed(),
            "confidence", output.confidence()
        );
    }

    public Map<String, Object> askChatbot(ChatbotAskRequest request) {
        Long contextDocumentId = extractDocumentId(request.context());
        if (contextDocumentId != null) {
            ensureDocumentExists(contextDocumentId);
        }
        List<Double> questionEmbedding = embeddingService.generateEmbedding(request.question());
        List<RankedChunk> ranked = rankChunks(questionEmbedding, request.context()).stream().limit(CHATBOT_TOP_K).toList();
        String context = ranked.stream().map(item -> item.chunk().getNoiDung()).reduce("", (a, b) -> a + "\n" + b).trim();

        ChatbotOutput output = aiModelService.answerWithContext(request.question(), context);
        Long documentId = ranked.isEmpty() ? null : ranked.getFirst().chunk().getVanBanId();
        AiResultEntity saved = saveResult(
            documentId,
            request.userId(),
            AiProcessType.CHATBOT,
            request.question(),
            output.answer(),
            output.confidence(),
            output.modelUsed(),
            request.context() == null ? null : toJson(request.context())
        );

        List<Map<String, Object>> sources = ranked.stream().map(item -> {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("chunkId", item.chunk().getId());
            source.put("documentId", item.chunk().getVanBanId());
            source.put("chunkIndex", item.chunk().getChunkIndex());
            source.put("reference", "ai_document_chunk");
            source.put("matchedText", item.chunk().getNoiDung());
            return source;
        }).toList();

        return Map.of(
            "resultId", saved.getId(),
            "question", request.question(),
            "answer", output.answer(),
            "modelUsed", output.modelUsed(),
            "confidence", output.confidence(),
            "sources", sources
        );
    }

    public Map<String, Object> getResultsByDocument(Long documentId, String loaiXuLyAI, int page, int size) {
        ensureDocumentExists(documentId);
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<AiResultEntity> resultPage;
        if (loaiXuLyAI == null || loaiXuLyAI.isBlank()) {
            resultPage = aiResultRepository.findByVanBanID(documentId, pageable);
        } else {
            AiProcessType type = parseAiProcessType(loaiXuLyAI);
            resultPage = aiResultRepository.findByVanBanIDAndLoaiXuLyAI(documentId, type, pageable);
        }
        List<Map<String, Object>> content = resultPage.getContent().stream().map(this::mapResultSummary).toList();
        return Map.of(
            "content", content,
            "page", resultPage.getNumber(),
            "size", resultPage.getSize(),
            "totalElements", resultPage.getTotalElements()
        );
    }

    public Map<String, Object> getResultDetail(Long id) {
        AiResultEntity entity = aiResultRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.AI_RESULT_NOT_FOUND, HttpStatus.NOT_FOUND, "AI result not found"));
        return Map.of(
            "id", entity.getId(),
            "documentId", entity.getVanBanID(),
            "userId", entity.getNguoiYeuCauID(),
            "loaiXuLyAI", entity.getLoaiXuLyAI(),
            "noiDungDauVao", entity.getNoiDungDauVao(),
            "ketQuaTraVe", entity.getKetQuaTraVe(),
            "confidence", entity.getDoTinCay(),
            "modelUsed", entity.getModelSuDung(),
            "processedAt", entity.getThoiGianXuLy(),
            "ghiChu", entity.getGhiChu()
        );
    }

    public Map<String, Object> deleteResult(Long id) {
        if (!aiResultRepository.existsById(id)) {
            throw new AppException(ErrorCode.AI_RESULT_NOT_FOUND, HttpStatus.NOT_FOUND, "AI result not found");
        }
        aiResultRepository.deleteById(id);
        return Map.of("id", id, "deleted", true);
    }

    private AiResultEntity saveResult(
        Long documentId,
        Long userId,
        AiProcessType type,
        String input,
        String output,
        Double confidence,
        String modelUsed,
        String note
    ) {
        AiResultEntity entity = AiResultEntity.builder()
            .vanBanID(documentId)
            .nguoiYeuCauID(userId)
            .loaiXuLyAI(type)
            .noiDungDauVao(input)
            .ketQuaTraVe(output)
            .doTinCay(confidence)
            .modelSuDung(modelUsed)
            .thoiGianXuLy(LocalDateTime.now())
            .ghiChu(note)
            .build();
        return aiResultRepository.save(entity);
    }

    private List<RankedChunk> rankChunks(List<Double> queryEmbedding, Map<String, Object> filters) {
        Long filterDocumentId = extractDocumentId(filters);
        return aiDocumentChunkRepository.findAll().stream()
            .filter(chunk -> filterDocumentId == null || Objects.equals(chunk.getVanBanId(), filterDocumentId))
            .filter(chunk -> matchesMetadataFilters(chunk, filters))
            .map(chunk -> new RankedChunk(chunk, cosineSimilarity(queryEmbedding, fromJsonVector(chunk.getEmbedding()))))
            .filter(item -> item.score() > 0)
            .sorted(Comparator.comparingDouble(RankedChunk::score).reversed())
            .toList();
    }

    private Map<String, Object> toSearchItem(RankedChunk rankedChunk) {
        AiDocumentChunkEntity chunk = rankedChunk.chunk();
        return Map.of(
            "documentId", chunk.getVanBanId(),
            "chunkId", chunk.getId(),
            "chunkIndex", chunk.getChunkIndex(),
            "score", rankedChunk.score(),
            "matchedText", chunk.getNoiDung(),
            "metadata", fromJsonMap(chunk.getMetadata())
        );
    }

    private List<Double> fromJsonVector(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<Double> values = objectMapper.readValue(
                json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Double.class)
            );
            return values == null ? List.of() : values;
        } catch (Exception ex) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Object> values = objectMapper.readValue(json, Map.class);
            return values == null ? Map.of() : values;
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        int size = Math.min(a.size(), b.size());
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < size; i++) {
            double av = a.get(i);
            double bv = b.get(i);
            dot += av * bv;
            normA += av * av;
            normB += bv * bv;
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private List<String> splitToChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        String safe = text == null ? "" : text.trim();
        if (safe.isEmpty()) {
            return chunks;
        }
        for (int start = 0; start < safe.length(); start += chunkSize) {
            int end = Math.min(start + chunkSize, safe.length());
            chunks.add(safe.substring(start, end));
        }
        return chunks;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT, HttpStatus.BAD_REQUEST, "File is empty");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT, HttpStatus.BAD_REQUEST, "Invalid file format");
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT, HttpStatus.BAD_REQUEST, "Invalid file format");
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.AI_PROCESSING_FAILED, HttpStatus.INTERNAL_SERVER_ERROR, "Serialize result failed");
        }
    }

    private Long extractDocumentId(Map<String, Object> filters) {
        if (filters == null) {
            return null;
        }
        Object value = filters.get("documentId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str && !str.isBlank()) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ex) {
                throw new AppException(ErrorCode.AI_PROCESSING_FAILED, HttpStatus.BAD_REQUEST, "Invalid documentId filter");
            }
        }
        return null;
    }

    private boolean matchesMetadataFilters(AiDocumentChunkEntity chunk, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }

        Map<String, Object> metadata = fromJsonMap(chunk.getMetadata());
        if (!matchesDateRange(filters, metadata)) {
            return false;
        }

        return filters.entrySet().stream()
            .filter(entry -> !"documentId".equals(entry.getKey()))
            .filter(entry -> !"fromDate".equals(entry.getKey()))
            .filter(entry -> !"toDate".equals(entry.getKey()))
            .allMatch(entry -> {
                Object expected = entry.getValue();
                if (expected == null) {
                    return true;
                }
                Object actual = metadata.get(entry.getKey());
                return areEquivalent(actual, expected);
            });
    }

    private boolean matchesDateRange(Map<String, Object> filters, Map<String, Object> metadata) {
        Object fromRaw = filters.get("fromDate");
        Object toRaw = filters.get("toDate");
        if (fromRaw == null && toRaw == null) {
            return true;
        }

        LocalDate valueDate = parseDate(metadata.get("ngayVanBan"));
        if (valueDate == null) {
            return false;
        }

        LocalDate from = parseDate(fromRaw);
        LocalDate to = parseDate(toRaw);

        if (from != null && valueDate.isBefore(from)) {
            return false;
        }
        return to == null || !valueDate.isAfter(to);
    }

    private LocalDate parseDate(Object raw) {
        if (raw == null) {
            return null;
        }
        String value = String.valueOf(raw).trim();
        if (value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private boolean areEquivalent(Object actual, Object expected) {
        if (actual == null) {
            return false;
        }

        if (actual instanceof Number actualNumber && expected instanceof Number expectedNumber) {
            return Double.compare(actualNumber.doubleValue(), expectedNumber.doubleValue()) == 0;
        }

        String actualText = String.valueOf(actual).trim();
        String expectedText = String.valueOf(expected).trim();
        return actualText.equalsIgnoreCase(expectedText);
    }

    private Map<String, Object> mapResultSummary(AiResultEntity entity) {
        return Map.of(
            "id", entity.getId(),
            "documentId", entity.getVanBanID(),
            "userId", entity.getNguoiYeuCauID(),
            "loaiXuLyAI", entity.getLoaiXuLyAI(),
            "ketQuaTraVe", entity.getKetQuaTraVe(),
            "confidence", entity.getDoTinCay(),
            "modelUsed", entity.getModelSuDung(),
            "processedAt", entity.getThoiGianXuLy()
        );
    }

    private Map<String, Object> merge(Map<String, Object> source, Map<String, Object> extra) {
        Map<String, Object> merged = new LinkedHashMap<>(source);
        merged.putAll(extra);
        return merged;
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "vi";
        }
        return language.trim();
    }

    private void validateSummarizeRequest(SummarizeRequest request) {
        if (request == null || request.documentId() == null || request.userId() == null || isBlank(request.text())) {
            throw new AppException(ErrorCode.AI_PROCESSING_FAILED, HttpStatus.BAD_REQUEST, "Invalid summarize request");
        }
        if (isBlank(request.summaryType()) || !ALLOWED_SUMMARY_TYPES.contains(request.summaryType().trim().toUpperCase())) {
            throw new AppException(ErrorCode.AI_PROCESSING_FAILED, HttpStatus.BAD_REQUEST, "Invalid summaryType");
        }
    }

    private void validateClassifyRequest(ClassifyRequest request) {
        if (request == null || request.documentId() == null || request.userId() == null || isBlank(request.text())) {
            throw new AppException(ErrorCode.AI_PROCESSING_FAILED, HttpStatus.BAD_REQUEST, "Invalid classify request");
        }
        if (request.categories() == null || request.categories().isEmpty()) {
            throw new AppException(ErrorCode.AI_PROCESSING_FAILED, HttpStatus.BAD_REQUEST, "Categories cannot be empty");
        }
    }

    private void validateMetadataRequest(MetadataExtractRequest request) {
        if (request == null || request.documentId() == null || request.userId() == null || isBlank(request.text())) {
            throw new AppException(ErrorCode.AI_PROCESSING_FAILED, HttpStatus.BAD_REQUEST, "Invalid metadata request");
        }
        if (request.fields() == null || request.fields().isEmpty()) {
            throw new AppException(ErrorCode.AI_PROCESSING_FAILED, HttpStatus.BAD_REQUEST, "Fields cannot be empty");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private AiProcessType parseAiProcessType(String loaiXuLyAI) {
        try {
            return AiProcessType.valueOf(loaiXuLyAI.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.AI_PROCESSING_FAILED, HttpStatus.BAD_REQUEST, "Invalid loaiXuLyAI");
        }
    }

    private void ensureDocumentExists(Long documentId) {
        if (documentId == null) {
            return;
        }
        documentInternalApiService.getDocument(documentId);
    }

    private record RankedChunk(AiDocumentChunkEntity chunk, double score) {
    }
}
