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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AuthInternalApiClientTest {

    private static final String BASE_URL = "http://auth-service";
    private static final String TOKEN = "internal-token";
    private static final String SERVICE_NAME = "ai-service";

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private AuthInternalApiClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new AuthInternalApiClient(restTemplate, new ObjectMapper(), BASE_URL, TOKEN, SERVICE_NAME);
    }

    @Test
    void shouldCheckPermissionWithHeadersAndBody() {
        server.expect(requestTo(BASE_URL + "/internal/auth/permissions/check"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(header("X-Service-Name", SERVICE_NAME))
            .andExpect(content().json("""
                {"userId":2,"maChucNang":"SYSTEM_STATISTIC","permission":"IsView"}
                """))
            .andRespond(withSuccess("""
                {"success":true,"message":"ok","data":{"allowed":true}}
                """, MediaType.APPLICATION_JSON));

        boolean allowed = client.hasSystemStatisticPermission(2L);

        assertThat(allowed).isTrue();
        server.verify();
    }

    @Test
    void shouldCallTotalUserCountWithHeadersAndMapData() {
        server.expect(requestTo(BASE_URL + "/internal/auth/statistics/users/count"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer " + TOKEN))
            .andExpect(header("X-Service-Name", SERVICE_NAME))
            .andRespond(withSuccess("""
                {"success":true,"message":"ok","data":{"count":120}}
                """, MediaType.APPLICATION_JSON));

        long count = client.getTotalUserCount(2L);

        assertThat(count).isEqualTo(120L);
        server.verify();
    }

    @Test
    void shouldMapInternalApiError() {
        server.expect(requestTo(BASE_URL + "/internal/auth/statistics/users/count"))
            .andRespond(withServerError());

        assertThatThrownBy(() -> client.getTotalUserCount(2L))
            .isInstanceOf(AppException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
