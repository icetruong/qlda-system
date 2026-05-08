package com.qlda.aiservice.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StubAiModelService implements AiModelService {

    @Override
    public SummarizationOutput summarize(String text, String summaryType, String language) {
        String trimmed = text == null ? "" : text.trim();
        String summary = trimmed.length() <= 160 ? trimmed : trimmed.substring(0, 160);
        return new SummarizationOutput(summary, 90.0, "stub-ai-model");
    }

    @Override
    public ClassificationOutput classify(String text, List<String> categories, String language) {
        String category = categories == null || categories.isEmpty() ? "UNKNOWN" : categories.getFirst();
        return new ClassificationOutput(category, category, 88.0, "Stub classification", "stub-ai-model");
    }

    @Override
    public MetadataOutput extractMetadata(String text, List<String> fields, String language) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (fields != null) {
            for (String field : fields) {
                metadata.put(field, "stub-" + field);
            }
        }
        return new MetadataOutput(metadata, 86.0, "stub-ai-model");
    }

    @Override
    public SuggestionHandlingOutput suggestHandling(String text, Map<String, Object> context) {
        List<SuggestionItem> items = new ArrayList<>();
        items.add(new SuggestionItem("CHUYEN_XU_LY", "Xu ly theo quy trinh noi bo", "HIGH"));
        return new SuggestionHandlingOutput(items, 84.0, "stub-ai-model");
    }

    @Override
    public SuggestionReplyOutput suggestReply(String text, String replyStyle, String language) {
        return new SuggestionReplyOutput(
            "Kinh gui, don vi da tiep nhan va se xu ly theo quy dinh.",
            replyStyle,
            85.0,
            "stub-ai-model"
        );
    }

    @Override
    public ChatbotOutput answerWithContext(String question, String context) {
        String answer = context == null || context.isBlank()
            ? "Khong tim thay tai lieu phu hop."
            : "Tra loi dua tren ngu canh da tim thay.";
        return new ChatbotOutput(answer, 87.0, "stub-ai-model");
    }
}

