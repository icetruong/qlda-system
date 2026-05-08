package com.qlda.authservice.service;

import com.qlda.authservice.common.ErrorCode;
import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.dto.auth.AuthTokenResponse;
import com.qlda.authservice.dto.auth.AzureLoginRequest;
import com.qlda.authservice.dto.auth.CurrentUserResponse;
import com.qlda.authservice.dto.auth.LogoutRequest;
import com.qlda.authservice.dto.auth.RefreshTokenRequest;
import com.qlda.authservice.dto.auth.RefreshTokenResponse;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.NhomQuyen;
import com.qlda.authservice.exception.ApiException;
import com.qlda.authservice.repository.NguoiDungRepository;
import com.qlda.authservice.security.CurrentUserPrincipal;
import com.qlda.authservice.security.JwtService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private NguoiDungRepository nguoiDungRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenStoreService refreshTokenStoreService;
    @Mock
    private AzureAuthService azureAuthService;
    @Mock
    private AuditLogService auditLogService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.getDevPassword().setEnabled(true);
        authProperties.getDevPassword().setValue("123456");
        authProperties.getJwt().setRefreshTokenSeconds(7200);
        authProperties.getJwt().setAccessTokenSeconds(3600);

        authService = new AuthService(
                userService,
                nguoiDungRepository,
                jwtService,
                refreshTokenStoreService,
                azureAuthService,
                authProperties,
                auditLogService
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginAzureShouldThrowWhenAzureInfoInvalid() {
        when(azureAuthService.exchangeCodeForUser(any())).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class, () ->
                authService.loginAzure(new AzureLoginRequest("code", "http://localhost/callback", null), "127.0.0.1"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals(ErrorCode.AZURE_AUTH_FAILED, exception.getErrorCode());
    }

    @Test
    void loginAzureShouldIssueTokenWhenMappedByEmail() {
        NguoiDung user = buildActiveUser();
        AzureAuthService.AzureUserInfo userInfo = new AzureAuthService.AzureUserInfo(
                "azure-id-1",
                "admin@company.com",
                "admin",
                "Azure Admin",
                true
        );
        when(azureAuthService.exchangeCodeForUser(any())).thenReturn(userInfo);
        when(nguoiDungRepository.findByAzureAdId("azure-id-1")).thenReturn(Optional.empty());
        when(nguoiDungRepository.findByEmail("admin@company.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.getAccessTokenSeconds()).thenReturn(3600L);

        AuthTokenResponse response = authService.loginAzure(
                new AzureLoginRequest("code", "http://localhost/callback", null),
                "127.0.0.1"
        );

        assertEquals("access-token", response.accessToken());
        ArgumentCaptor<NguoiDung> userCaptor = ArgumentCaptor.forClass(NguoiDung.class);
        verify(nguoiDungRepository).save(userCaptor.capture());
        assertEquals("azure-id-1", userCaptor.getValue().getAzureAdId());
    }

    @Test
    void refreshTokenShouldReturnNewTokensWhenValid() {
        NguoiDung user = buildActiveUser();
        when(jwtService.isRefreshTokenValid("old-refresh")).thenReturn(true);
        when(refreshTokenStoreService.isValid("old-refresh")).thenReturn(true);
        when(jwtService.extractUsername("old-refresh")).thenReturn("admin");
        when(userService.findActiveUserByUsername("admin")).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh");
        when(jwtService.getAccessTokenSeconds()).thenReturn(3600L);

        RefreshTokenResponse response = authService.refreshToken(new RefreshTokenRequest("old-refresh"));

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        verify(refreshTokenStoreService).replace(eq("old-refresh"), eq("new-refresh"), eq(1L), eq("admin"), any(Instant.class));
    }

    @Test
    void refreshTokenShouldThrowWhenInvalid() {
        when(jwtService.isRefreshTokenValid("bad-token")).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class,
                () -> authService.refreshToken(new RefreshTokenRequest("bad-token")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void logoutShouldRevokeTokenAndWriteAuditLogWhenContextHasPrincipal() {
        NguoiDung user = buildActiveUser();
        when(userService.findActiveUserById(1L)).thenReturn(user);

        CurrentUserPrincipal principal = new CurrentUserPrincipal(1L, "admin", List.of());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        authService.logout(new LogoutRequest("refresh-token"), "127.0.0.1");

        verify(refreshTokenStoreService).revoke("refresh-token");
        verify(auditLogService).log(eq(1L), eq("Admin User"), eq("LOGOUT"), eq("NguoiDung"),
                eq(1L), eq("User logout"), eq("127.0.0.1"), eq(1));
    }

    @Test
    void getCurrentUserShouldReturnUserDataWhenAuthenticated() {
        NguoiDung user = buildActiveUser();
        when(userService.findActiveUserById(1L)).thenReturn(user);
        CurrentUserPrincipal principal = new CurrentUserPrincipal(1L, "admin", List.of());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CurrentUserResponse response = authService.getCurrentUser();

        assertEquals(1L, response.id());
        assertEquals("admin", response.username());
        assertEquals("Admin User", response.hoTen());
        assertEquals("admin@company.com", response.email());
        assertEquals(List.of("ADMIN"), response.roles());
    }

    @Test
    void getCurrentUserShouldThrowUnauthorizedWhenNoAuthentication() {
        ApiException exception = assertThrows(ApiException.class, () -> authService.getCurrentUser());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    private NguoiDung buildActiveUser() {
        NhomQuyen nhomQuyen = new NhomQuyen();
        nhomQuyen.setId(1);
        nhomQuyen.setMaNhomQuyen("ADMIN");
        nhomQuyen.setTenNhomQuyen("Admin");

        NguoiDung user = new NguoiDung();
        user.setId(1L);
        user.setUserName("admin");
        user.setHoTen("Admin User");
        user.setEmail("admin@company.com");
        user.setTrangThai(1);
        user.setNhomQuyen(nhomQuyen);
        return user;
    }
}
