package com.qlda.aiservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlda.aiservice.exception.AppException;
import com.qlda.aiservice.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WorkflowInternalApiClientTest {

    private static final String BASE_URL = "http://workflow-service";
    private static final String TOKEN = "internal-token";
    private static final String SERVICE_NAME = "ai-service";

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private WorkflowInternalApiClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new WorkflowInternalApiClient(restTemplate, new ObjectMapper(), BASE_URL, TOKEN, SERVICE_NAME);
    }

    @Test
    void shouldCallDueSoonUrlWithHeadersAndMapCount() {
        server.expect(requestTo(BASE_URL + "/internal/workflows/statistics/my-due-soon-count?userId=2&days=3"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(header("X-Service-Name", SERVICE_NAME))
            .andRespond(withSuccess("""
                {"success": true, "message": "ok", "data": {"count": 5}}
                """, MediaType.APPLICATION_JSON));

        long count = client.getMyDueSoonDocumentCount(2L, 3);

        assertThat(count).isEqualTo(5L);
        server.verify();
    }

    @Test
    void shouldCallOverdueUrlWithHeadersAndMapCount() {
        server.expect(requestTo(BASE_URL + "/internal/workflows/statistics/my-overdue-count?userId=2"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(header("X-Service-Name", SERVICE_NAME))
            .andRespond(withSuccess("""
                {"success": true, "message": "ok", "data": {"count": 2}}
                """, MediaType.APPLICATION_JSON));

        long count = client.getMyOverdueDocumentCount(2L);

        assertThat(count).isEqualTo(2L);
        server.verify();
    }

    @Test
    void shouldMapInternalApiError() {
        server.expect(requestTo(BASE_URL + "/internal/workflows/statistics/my-overdue-count?userId=2"))
            .andRespond(withServerError());

        assertThatThrownBy(() -> client.getMyOverdueDocumentCount(2L))
            .isInstanceOf(AppException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
