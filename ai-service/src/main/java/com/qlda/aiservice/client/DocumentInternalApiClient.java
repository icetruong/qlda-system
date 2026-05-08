package com.qlda.aiservice.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.aiservice.dto.common.InternalApiEnvelope;
import com.qlda.aiservice.dto.internal.DocumentAttachmentDto;
import com.qlda.aiservice.dto.internal.DocumentContentDto;
import com.qlda.aiservice.dto.internal.DocumentMetadataDto;
import com.qlda.aiservice.exception.AppException;
import com.qlda.aiservice.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DocumentInternalApiClient implements DocumentInternalApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String token;
    private final String serviceName;

    public DocumentInternalApiClient(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        @Value("${DOCUMENT_SERVICE_BASE_URL:http://localhost:8081}") String baseUrl,
        @Value("${INTERNAL_SERVICE_TOKEN:}") String token,
        @Value("${SERVICE_NAME:ai-service}") String serviceName
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.token = token;
        this.serviceName = serviceName;
    }

    @Override
    public DocumentMetadataDto getDocument(Long id) {
        InternalApiEnvelope<?> envelope = exchange(baseUrl + "/internal/documents/" + id, HttpMethod.GET, null);
        return objectMapper.convertValue(envelope.data(), DocumentMetadataDto.class);
    }

    @Override
    public DocumentContentDto getDocumentContent(Long id) {
        InternalApiEnvelope<?> envelope = exchange(baseUrl + "/internal/documents/" + id + "/content", HttpMethod.GET, null);
        return objectMapper.convertValue(envelope.data(), DocumentContentDto.class);
    }

    @Override
    public List<DocumentAttachmentDto> getDocumentAttachments(Long id) {
        InternalApiEnvelope<?> envelope = exchange(baseUrl + "/internal/documents/" + id + "/attachments", HttpMethod.GET, null);
        List<Map<String, Object>> raw = objectMapper.convertValue(envelope.data(), new TypeReference<>() {
        });
        if (raw == null) {
            return Collections.emptyList();
        }
        return raw.stream()
            .map(item -> objectMapper.convertValue(item, DocumentAttachmentDto.class))
            .toList();
    }

    @Override
    public void updateOcrStatus(Long id, boolean daOcr) {
        exchange(baseUrl + "/internal/documents/" + id + "/ocr-status", HttpMethod.PATCH, Map.of("daOCR", daOcr));
    }

    @Override
    public Set<Long> checkDocumentAccess(Long userId, List<Long> documentIds) {
        InternalApiEnvelope<?> envelope = exchange(
            baseUrl + "/internal/documents/access-check",
            HttpMethod.POST,
            Map.of("userId", userId, "documentIds", documentIds)
        );
        Map<String, Object> raw = objectMapper.convertValue(envelope.data(), new TypeReference<>() {
        });
        if (raw == null) {
            return Set.of();
        }
        List<?> allowedIds = (List<?>) raw.get("allowedDocumentIds");
        if (allowedIds == null) {
            return Set.of();
        }
        return allowedIds.stream()
            .filter(Number.class::isInstance)
            .map(Number.class::cast)
            .map(Number::longValue)
            .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public long getMyUploadedDocumentCount(Long userId) {
        InternalApiEnvelope<?> envelope = exchange(
            baseUrl + "/internal/documents/statistics/my-uploaded-count?userId=" + userId,
            HttpMethod.GET,
            null
        );
        Map<String, Object> raw = objectMapper.convertValue(envelope.data(), new TypeReference<>() {
        });
        Object value = raw == null ? null : raw.get("count");
        return value instanceof Number number ? number.longValue() : 0L;
    }

    @Override
    public long getTotalDocumentCount() {
        InternalApiEnvelope<?> envelope = exchange(
            baseUrl + "/internal/documents/statistics/total-count",
            HttpMethod.GET,
            null
        );
        Map<String, Object> raw = objectMapper.convertValue(envelope.data(), new TypeReference<>() {
        });
        Object value = raw == null ? null : raw.get("count");
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private InternalApiEnvelope<?> exchange(String url, HttpMethod method, Object body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            headers.set("X-Service-Name", serviceName);

            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            ResponseEntity<InternalApiEnvelope> response = restTemplate.exchange(url, method, entity, InternalApiEnvelope.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new AppException(ErrorCode.DOCUMENT_NOT_FOUND, HttpStatus.NOT_FOUND, "Document not found");
        } catch (RestClientException ex) {
            throw new AppException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal document-service request failed"
            );
        }
    }
}
