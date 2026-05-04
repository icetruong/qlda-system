package com.qlda.notificationservice.client;

import com.qlda.notificationservice.client.dto.DocumentOverduePageResponse;
import com.qlda.notificationservice.client.dto.DocumentStatisticsClientResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class DocumentServiceClient {

    private final WebClient webClient;

    public DocumentServiceClient(
        WebClient.Builder webClientBuilder,
        @Value("${services.document-service.base-url:http://localhost:8082}") String baseUrl,
        @Value("${internal.auth.service-name:notification-service}") String serviceName,
        @Value("${internal.auth.service-token:change-me-in-dev}") String serviceToken
    ) {
        this.webClient = webClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken)
            .defaultHeader("X-Service-Name", serviceName)
            .build();
    }

    public DocumentStatisticsClientResponse getDocumentStatistics(String fromDate, String toDate, Long donViId, String groupBy) {
        return webClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder.path("/internal/documents/statistics");
                if (StringUtils.hasText(fromDate)) {
                    builder.queryParam("fromDate", fromDate);
                }
                if (StringUtils.hasText(toDate)) {
                    builder.queryParam("toDate", toDate);
                }
                if (donViId != null) {
                    builder.queryParam("donViId", donViId);
                }
                if (StringUtils.hasText(groupBy)) {
                    builder.queryParam("groupBy", groupBy);
                }
                return builder.build();
            })
            .retrieve()
            .bodyToMono(DocumentStatisticsClientResponse.class)
            .block();
    }

    public DocumentOverduePageResponse getOverdueDocuments(Long donViId, Long nguoiXuLyId, int page, int size) {
        return webClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder.path("/internal/documents/overdue")
                    .queryParam("page", page)
                    .queryParam("size", size);
                if (donViId != null) {
                    builder.queryParam("donViId", donViId);
                }
                if (nguoiXuLyId != null) {
                    builder.queryParam("nguoiXuLyId", nguoiXuLyId);
                }
                return builder.build();
            })
            .retrieve()
            .bodyToMono(DocumentOverduePageResponse.class)
            .block();
    }
}
