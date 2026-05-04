package com.qlda.documentservice.client.dto;

import java.util.List;
import java.util.Map;

public final class AiClientDtos {
    private AiClientDtos() {
    }

    public record OcrRequest(Long documentId, String fileUrl, String language) {
    }

    public record OcrResponse(Long documentId, String ocrText, Double confidence, String modelUsed) {
    }

    public record SummarizeRequest(Long documentId, String content, String summaryType) {
    }

    public record SummarizeResponse(Long documentId, String summaryType, String summary, Double confidence, String modelUsed) {
    }

    public record ClassifyRequest(Long documentId, String content) {
    }

    public record ClassifyResponse(Long documentId, String category, String categoryName, Double confidence, String reason) {
    }

    public record ExtractMetadataRequest(Long documentId, String content) {
    }

    public record ExtractMetadataResponse(Long documentId, Map<String, String> metadata, Double confidence, String modelUsed) {
    }

    public record SuggestionItem(String action, String description, String priority) {
    }

    public record SuggestionRequest(Long documentId, String content) {
    }

    public record SuggestionResponse(Long documentId, List<SuggestionItem> suggestions, Double confidence) {
    }
}
