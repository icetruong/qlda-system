package com.qlda.aiservice.service.chatbot;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class ChatbotIntentDetector {

    public IntentDetectionResult detect(String question) {
        String normalized = normalize(question);

        if (normalized.contains("upload") && normalized.contains("bao nhieu") && normalized.contains("van ban")) {
            return new IntentDetectionResult(ChatbotIntent.SYSTEM_STATISTIC, ChatbotMetricCode.MY_UPLOADED_DOCUMENT_COUNT);
        }
        if (normalized.contains("bao nhieu") && normalized.contains("van ban") && normalized.contains("sap het han")) {
            return new IntentDetectionResult(ChatbotIntent.SYSTEM_STATISTIC, ChatbotMetricCode.MY_DUE_SOON_DOCUMENT_COUNT);
        }
        if (normalized.contains("bao nhieu") && normalized.contains("van ban") && normalized.contains("qua han")) {
            return new IntentDetectionResult(ChatbotIntent.SYSTEM_STATISTIC, ChatbotMetricCode.MY_OVERDUE_DOCUMENT_COUNT);
        }
        if (normalized.contains("he thong") && normalized.contains("bao nhieu") && normalized.contains("nguoi dung")) {
            return new IntentDetectionResult(ChatbotIntent.SYSTEM_STATISTIC, ChatbotMetricCode.TOTAL_USER_COUNT);
        }
        if (normalized.contains("he thong") && normalized.contains("bao nhieu") && normalized.contains("van ban")) {
            return new IntentDetectionResult(ChatbotIntent.SYSTEM_STATISTIC, ChatbotMetricCode.TOTAL_DOCUMENT_COUNT);
        }
        if ((normalized.contains("lam sao") && normalized.contains("van ban den"))
            || (normalized.contains("cach") && normalized.contains("trinh ky van ban"))
            || (normalized.contains("tao") && normalized.contains("van ban") && normalized.contains("den"))) {
            return new IntentDetectionResult(ChatbotIntent.USER_GUIDE, null);
        }
        if ((normalized.contains("tim") && normalized.contains("van ban"))
            || (normalized.contains("tim") && normalized.contains("tai lieu"))
            || normalized.contains("tai lieu lien quan")) {
            return new IntentDetectionResult(ChatbotIntent.DOCUMENT_SEARCH, null);
        }
        return new IntentDetectionResult(ChatbotIntent.GENERAL_HELP, null);
    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        String lowered = input.toLowerCase(Locale.ROOT).trim();
        String normalized = Normalizer.normalize(lowered, Normalizer.Form.NFD);
        return normalized
            .replaceAll("\\p{M}+", "")
            .replaceAll("[^a-z0-9\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }
}
