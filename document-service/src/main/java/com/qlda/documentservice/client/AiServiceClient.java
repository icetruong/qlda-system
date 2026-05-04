package com.qlda.documentservice.client;

import com.qlda.documentservice.client.dto.AiClientDtos;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-service-client", url = "${services.ai-service.base-url:http://localhost:8084}")
public interface AiServiceClient {

    @PostMapping("/internal/ai/ocr")
    AiClientDtos.OcrResponse ocr(@RequestBody AiClientDtos.OcrRequest request);

    @PostMapping("/internal/ai/summarize")
    AiClientDtos.SummarizeResponse summarize(@RequestBody AiClientDtos.SummarizeRequest request);

    @PostMapping("/internal/ai/classify")
    AiClientDtos.ClassifyResponse classify(@RequestBody AiClientDtos.ClassifyRequest request);

    @PostMapping("/internal/ai/metadata/extract")
    AiClientDtos.ExtractMetadataResponse extractMetadata(@RequestBody AiClientDtos.ExtractMetadataRequest request);

    @PostMapping("/internal/ai/suggestions")
    AiClientDtos.SuggestionResponse suggestions(@RequestBody AiClientDtos.SuggestionRequest request);
}
