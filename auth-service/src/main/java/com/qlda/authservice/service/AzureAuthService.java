package com.qlda.authservice.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.dto.auth.AzureLoginRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class AzureAuthService {

    private static final Logger log = LoggerFactory.getLogger(AzureAuthService.class);

    private static final String DEFAULT_SCOPE = "openid profile email offline_access User.Read";
    private static final String GRAPH_ME_ENDPOINT = "https://graph.microsoft.com/v1.0/me?$select=id,displayName,mail,userPrincipalName";

    private final AuthProperties authProperties;
    private final RestClient restClient;
    private final Map<String, JwtDecoder> jwtDecoders = new ConcurrentHashMap<>();

    @Autowired
    public AzureAuthService(AuthProperties authProperties) {
        this(authProperties, RestClient.builder());
    }

    AzureAuthService(AuthProperties authProperties, RestClient.Builder restClientBuilder) {
        this.authProperties = authProperties;
        this.restClient = restClientBuilder.build();
    }

    public AzureUserInfo exchangeCodeForUser(AzureLoginRequest request) {
        AuthProperties.Azure azure = authProperties.getAzure();
        if (!isAzureConfigured(azure)
                || !StringUtils.hasText(request.authorizationCode())
                || !StringUtils.hasText(request.redirectUri())) {
            return null;
        }

        OAuthTokenResponse tokenResponse = exchangeAuthorizationCode(azure, request);
        if (tokenResponse == null) {
            return null;
        }

        AzureUserInfo fromIdToken = resolveUserFromIdToken(azure, tokenResponse.idToken());
        if (fromIdToken != null) {
            return fromIdToken;
        }
        return resolveUserFromGraph(tokenResponse.accessToken(), isAzureConfigured(azure));
    }

    private OAuthTokenResponse exchangeAuthorizationCode(AuthProperties.Azure azure, AzureLoginRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", azure.getClientId().trim());
        if (StringUtils.hasText(azure.getClientSecret())) {
            form.add("client_secret", azure.getClientSecret().trim());
        }
        form.add("grant_type", "authorization_code");
        form.add("code", request.authorizationCode().trim());
        form.add("redirect_uri", request.redirectUri().trim());
        form.add("scope", DEFAULT_SCOPE);
        if (StringUtils.hasText(request.codeVerifier())) {
            form.add("code_verifier", request.codeVerifier().trim());
        }

        try {
            return restClient.post()
                    .uri(tokenEndpoint(azure.getTenantId().trim()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(OAuthTokenResponse.class);
        } catch (RestClientException exception) {
            log.error("Azure token exchange failed: {}", exception.getMessage(), exception);
            return null;
        }
    }

    private AzureUserInfo resolveUserFromIdToken(AuthProperties.Azure azure, String idToken) {
        if (!StringUtils.hasText(idToken)) {
            return null;
        }
        try {
            Jwt jwt = jwtDecoderFor(azure).decode(idToken.trim());
            String azureAdId = firstNonBlank(jwt.getClaimAsString("oid"), jwt.getSubject());
            if (!StringUtils.hasText(azureAdId)) {
                return null;
            }
            String email = firstNonBlank(
                    jwt.getClaimAsString("email"),
                    jwt.getClaimAsString("preferred_username"),
                    jwt.getClaimAsString("upn")
            );
            String username = firstNonBlank(
                    jwt.getClaimAsString("preferred_username"),
                    jwt.getClaimAsString("upn"),
                    email,
                    azureAdId
            );
            String displayName = firstNonBlank(jwt.getClaimAsString("name"), username);

            return new AzureUserInfo(azureAdId, email, username, displayName, true);
        } catch (JwtException exception) {
            log.warn("id_token validation failed (will fallback to Graph): {}", exception.getMessage());
            return null;
        }
    }

    private AzureUserInfo resolveUserFromGraph(String accessToken, boolean configured) {
        if (!StringUtils.hasText(accessToken)) {
            return null;
        }
        try {
            GraphMeResponse profile = restClient.get()
                    .uri(GRAPH_ME_ENDPOINT)
                    .headers(headers -> headers.setBearerAuth(accessToken.trim()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(GraphMeResponse.class);
            if (profile == null || !StringUtils.hasText(profile.id())) {
                return null;
            }

            String email = firstNonBlank(profile.mail(), profile.userPrincipalName());
            String username = firstNonBlank(profile.userPrincipalName(), email, profile.id());
            String displayName = firstNonBlank(profile.displayName(), username);
            return new AzureUserInfo(profile.id().trim(), email, username, displayName, configured);
        } catch (RestClientException exception) {
            log.error("Graph API call failed: {}", exception.getMessage(), exception);
            return null;
        }
    }

    private JwtDecoder jwtDecoderFor(AuthProperties.Azure azure) {
        String tenantId = azure.getTenantId().trim();
        String cacheKey = tenantId + "|" + azure.getClientId().trim();
        return jwtDecoders.computeIfAbsent(cacheKey, ignored -> {
            String issuer = issuer(tenantId);
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri(tenantId)).build();
            OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
            OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
                if (jwt.getAudience() != null && jwt.getAudience().contains(azure.getClientId().trim())) {
                    return OAuth2TokenValidatorResult.success();
                }
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Invalid id_token audience", null)
                );
            };
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));
            return decoder;
        });
    }

    private boolean isAzureConfigured(AuthProperties.Azure azure) {
        return StringUtils.hasText(azure.getTenantId()) && StringUtils.hasText(azure.getClientId());
    }

    private String tokenEndpoint(String tenantId) {
        return "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
    }

    private String jwkSetUri(String tenantId) {
        return "https://login.microsoftonline.com/" + tenantId + "/discovery/v2.0/keys";
    }

    private String issuer(String tenantId) {
        return "https://login.microsoftonline.com/" + tenantId + "/v2.0";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    public record AzureUserInfo(
            String azureAdId,
            String email,
            String username,
            String displayName,
            boolean graphConfigured
    ) {
    }

    private record OAuthTokenResponse(
            @JsonProperty("access_token")
            String accessToken,
            @JsonProperty("id_token")
            String idToken
    ) {
    }

    private record GraphMeResponse(
            String id,
            String displayName,
            String mail,
            String userPrincipalName
    ) {
    }
}
