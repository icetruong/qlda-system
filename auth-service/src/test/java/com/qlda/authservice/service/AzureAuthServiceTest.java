package com.qlda.authservice.service;

import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.dto.auth.AzureLoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AzureAuthServiceTest {

    @Test
    void exchangeCodeForUserShouldReturnGraphProfileWhenIdTokenMissing() {
        AuthProperties properties = buildAzureProperties();
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AzureAuthService service = new AzureAuthService(properties, builder);

        server.expect(requestTo("https://login.microsoftonline.com/tenant-id/oauth2/v2.0/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("grant_type=authorization_code")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("code=sample-code")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("redirect_uri=http%3A%2F%2Flocalhost%2Fcallback")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("code_verifier=pkce-code-verifier")))
                .andRespond(withSuccess("{\"access_token\":\"graph-access-token\"}", MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://graph.microsoft.com/v1.0/me?$select=id,displayName,mail,userPrincipalName"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "azure-oid-1",
                          "displayName": "Azure Admin",
                          "mail": "admin@company.com",
                          "userPrincipalName": "admin@company.com"
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        AzureAuthService.AzureUserInfo info = service.exchangeCodeForUser(
                new AzureLoginRequest("sample-code", "http://localhost/callback", "pkce-code-verifier")
        );

        assertNotNull(info);
        assertEquals("azure-oid-1", info.azureAdId());
        assertEquals("admin@company.com", info.email());
        assertEquals("admin@company.com", info.username());
        assertEquals("Azure Admin", info.displayName());
        server.verify();
    }

    @Test
    void exchangeCodeForUserShouldReturnNullWhenTokenExchangeFails() {
        AuthProperties properties = buildAzureProperties();
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AzureAuthService service = new AzureAuthService(properties, builder);

        server.expect(requestTo("https://login.microsoftonline.com/tenant-id/oauth2/v2.0/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());

        AzureAuthService.AzureUserInfo info = service.exchangeCodeForUser(
                new AzureLoginRequest("sample-code", "http://localhost/callback", null)
        );

        assertNull(info);
        server.verify();
    }

    private AuthProperties buildAzureProperties() {
        AuthProperties properties = new AuthProperties();
        properties.getAzure().setTenantId("tenant-id");
        properties.getAzure().setClientId("client-id");
        properties.getAzure().setClientSecret("client-secret");
        return properties;
    }
}
