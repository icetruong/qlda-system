package com.qlda.aiservice.service;

import java.util.List;
import java.util.Map;

public interface AiModelService {
    SummarizationOutput summarize(String text, String summaryType, String language);

    ClassificationOutput classify(String text, List<String> categories, String language);

    MetadataOutput extractMetadata(String text, List<String> fields, String language);

    SuggestionHandlingOutput suggestHandling(String text, Map<String, Object> context);

    SuggestionReplyOutput suggestReply(String text, String replyStyle, String language);

    ChatbotOutput answerWithContext(String question, String context);
}

