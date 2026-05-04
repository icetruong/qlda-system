package com.qlda.notificationservice.client;

import com.qlda.notificationservice.client.dto.WorkflowProgressClientResponse;
import com.qlda.notificationservice.client.dto.WorkflowStatisticsClientResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WorkflowServiceClient {

    private final WebClient webClient;

    public WorkflowServiceClient(
        WebClient.Builder webClientBuilder,
        @Value("${services.workflow-service.base-url:http://localhost:8083}") String baseUrl,
        @Value("${internal.auth.service-name:notification-service}") String serviceName,
        @Value("${internal.auth.service-token:change-me-in-dev}") String serviceToken
    ) {
        this.webClient = webClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken)
            .defaultHeader("X-Service-Name", serviceName)
            .build();
    }

    public WorkflowStatisticsClientResponse getWorkflowStatistics(String fromDate, String toDate, Long donViId) {
        return webClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder.path("/internal/workflows/statistics");
                if (StringUtils.hasText(fromDate)) {
                    builder.queryParam("fromDate", fromDate);
                }
                if (StringUtils.hasText(toDate)) {
                    builder.queryParam("toDate", toDate);
                }
                if (donViId != null) {
                    builder.queryParam("donViId", donViId);
                }
                return builder.build();
            })
            .retrieve()
            .bodyToMono(WorkflowStatisticsClientResponse.class)
            .block();
    }

    public WorkflowProgressClientResponse getWorkflowProgress(String fromDate, String toDate, Long donViId, Long nguoiXuLyId) {
        return webClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder.path("/internal/workflows/progress");
                if (StringUtils.hasText(fromDate)) {
                    builder.queryParam("fromDate", fromDate);
                }
                if (StringUtils.hasText(toDate)) {
                    builder.queryParam("toDate", toDate);
                }
                if (donViId != null) {
                    builder.queryParam("donViId", donViId);
                }
                if (nguoiXuLyId != null) {
                    builder.queryParam("nguoiXuLyId", nguoiXuLyId);
                }
                return builder.build();
            })
            .retrieve()
            .bodyToMono(WorkflowProgressClientResponse.class)
            .block();
    }

    public List<Object> getSlaViolations(String fromDate, String toDate, Long donViId) {
        return webClient.get()
            .uri(uriBuilder -> {
                var builder = uriBuilder.path("/internal/workflows/sla/violations");
                if (StringUtils.hasText(fromDate)) {
                    builder.queryParam("fromDate", fromDate);
                }
                if (StringUtils.hasText(toDate)) {
                    builder.queryParam("toDate", toDate);
                }
                if (donViId != null) {
                    builder.queryParam("donViId", donViId);
                }
                return builder.build();
            })
            .retrieve()
            .bodyToFlux(Object.class)
            .collectList()
            .block();
    }
}
