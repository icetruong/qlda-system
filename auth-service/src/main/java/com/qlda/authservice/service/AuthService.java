package com.qlda.authservice.service;

import com.qlda.authservice.common.ErrorCode;
import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.dto.auth.AuthTokenResponse;
import com.qlda.authservice.dto.auth.AuthUserResponse;
import com.qlda.authservice.dto.auth.AzureLoginRequest;
import com.qlda.authservice.dto.auth.CurrentUserResponse;
import com.qlda.authservice.dto.auth.LogoutRequest;
import com.qlda.authservice.dto.auth.RefreshTokenRequest;
import com.qlda.authservice.dto.auth.RefreshTokenResponse;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.exception.ApiException;
import com.qlda.authservice.repository.NguoiDungRepository;
import com.qlda.authservice.security.CurrentUserPrincipal;
import com.qlda.authservice.security.JwtService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserService userService;
    private final NguoiDungRepository nguoiDungRepository;
    private final JwtService jwtService;
    private final RefreshTokenStoreService refreshTokenStoreService;
    private final AzureAuthService azureAuthService;
    private final AuthProperties authProperties;
    private final AuditLogService auditLogService;

    public AuthService(
            UserService userService,
            NguoiDungRepository nguoiDungRepository,
            JwtService jwtService,
            RefreshTokenStoreService refreshTokenStoreService,
            AzureAuthService azureAuthService,
            AuthProperties authProperties,
            AuditLogService auditLogService
    ) {
        this.userService = userService;
        this.nguoiDungRepository = nguoiDungRepository;
        this.jwtService = jwtService;
        this.refreshTokenStoreService = refreshTokenStoreService;
        this.azureAuthService = azureAuthService;
        this.authProperties = authProperties;
        this.auditLogService = auditLogService;
    }

    public AuthTokenResponse loginAzure(AzureLoginRequest request, String ipAddress) {
        AzureAuthService.AzureUserInfo azureUserInfo = azureAuthService.exchangeCodeForUser(request);
        if (azureUserInfo == null || !StringUtils.hasText(azureUserInfo.azureAdId())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.AZURE_AUTH_FAILED, "Azure login failed");
        }

        Optional<NguoiDung> byAzureId = nguoiDungRepository.findByAzureAdId(azureUserInfo.azureAdId());
        Optional<NguoiDung> byEmail = StringUtils.hasText(azureUserInfo.email())
                ? nguoiDungRepository.findByEmail(azureUserInfo.email())
                : Optional.empty();

        NguoiDung user = byAzureId.or(() -> byEmail)
                .filter(candidate -> candidate.getTrangThai() != null && candidate.getTrangThai() == 1)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        ErrorCode.AZURE_AUTH_FAILED,
                        "Azure account is not linked with an active user"
                ));

        user.setAzureAdId(azureUserInfo.azureAdId());
        user.setLanDangNhapCuoi(LocalDateTime.now());
        user.setNgayCapNhat(LocalDateTime.now());
        nguoiDungRepository.save(user);

        AuthTokenResponse response = issueAuthTokenResponse(user);
        auditLogService.log(user.getId(), user.getHoTen(), "AZURE_LOGIN", "NguoiDung",
                user.getId(), "Azure login", ipAddress, 1);
        return response;
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken().trim();
        if (!jwtService.isRefreshTokenValid(refreshToken) || !refreshTokenStoreService.isValid(refreshToken)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        NguoiDung user = userService.findActiveUserByUsername(username);
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        refreshTokenStoreService.replace(
                refreshToken,
                newRefreshToken,
                user.getId(),
                user.getUserName(),
                Instant.now().plusSeconds(authProperties.getJwt().getRefreshTokenSeconds())
        );
        return new RefreshTokenResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtService.getAccessTokenSeconds()
        );
    }

    public void logout(LogoutRequest request, String ipAddress) {
        String refreshToken = request.refreshToken().trim();
        refreshTokenStoreService.revoke(refreshToken);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUserPrincipal principal) {
            NguoiDung user = userService.findActiveUserById(principal.getUserId());
            auditLogService.log(user.getId(), user.getHoTen(), "LOGOUT", "NguoiDung",
                    user.getId(), "User logout", ipAddress, 1);
        }
    }

    public CurrentUserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUserPrincipal principal)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Unauthorized");
        }
        NguoiDung user = userService.findActiveUserById(principal.getUserId());
        return new CurrentUserResponse(
                user.getId(),
                user.getUserName(),
                user.getHoTen(),
                user.getEmail(),
                user.getDonVi() == null ? null : user.getDonVi().getId(),
                user.getNhomQuyen() == null ? null : user.getNhomQuyen().getId(),
                resolveRoles(user)
        );
    }

    private AuthTokenResponse issueAuthTokenResponse(NguoiDung user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenStoreService.save(
                refreshToken,
                user.getId(),
                user.getUserName(),
                Instant.now().plusSeconds(authProperties.getJwt().getRefreshTokenSeconds())
        );
        return new AuthTokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenSeconds(),
                new AuthUserResponse(
                        user.getId(),
                        user.getUserName(),
                        user.getHoTen(),
                        user.getEmail(),
                        resolveRoles(user)
                )
        );
    }

    private List<String> resolveRoles(NguoiDung user) {
        if (user.getNhomQuyen() == null) {
            return List.of("USER");
        }
        if (StringUtils.hasText(user.getNhomQuyen().getMaNhomQuyen())) {
            return List.of(user.getNhomQuyen().getMaNhomQuyen().toUpperCase(Locale.ROOT));
        }
        return List.of("USER");
    }
}
