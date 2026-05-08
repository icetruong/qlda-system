package com.qlda.aiservice.service.chatbot;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ChatbotResultMapper {

    public Map<String, Object> toResponse(
        Long resultId,
        ChatbotIntent intent,
        ChatbotMetricCode metricCode,
        String question,
        String answer,
        Long value,
        List<Map<String, Object>> sources,
        String modelUsed,
        double confidence
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("resultId", resultId);
        response.put("intent", intent.name());
        if (metricCode != null) {
            response.put("metricCode", metricCode.name());
        }
        response.put("question", question);
        response.put("answer", answer);
        if (value != null) {
            response.put("value", value);
        }
        if (sources != null && !sources.isEmpty()) {
            response.put("sources", sources);
        }
        response.put("modelUsed", modelUsed);
        response.put("confidence", confidence);
        return response;
    }
}
