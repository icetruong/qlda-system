package com.qlda.authservice.security;

import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.entity.NguoiDung;
import com.qlda.authservice.entity.NhomQuyen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void generateAccessTokenShouldBeValidAndContainClaims() {
        JwtService jwtService = buildJwtService("test-secret-key-test-secret-key-test-secret-key-123", 3600, 7200);
        NguoiDung user = buildUser();

        String accessToken = jwtService.generateAccessToken(user);

        assertTrue(jwtService.isAccessTokenValid(accessToken));
        assertFalse(jwtService.isRefreshTokenValid(accessToken));
        assertEquals(1L, jwtService.extractUserId(accessToken));
        assertEquals("admin", jwtService.extractUsername(accessToken));
    }

    @Test
    void generateRefreshTokenShouldBeValidForRefreshType() {
        JwtService jwtService = buildJwtService("test-secret-key-test-secret-key-test-secret-key-123", 3600, 7200);
        NguoiDung user = buildUser();

        String refreshToken = jwtService.generateRefreshToken(user);

        assertTrue(jwtService.isRefreshTokenValid(refreshToken));
        assertFalse(jwtService.isAccessTokenValid(refreshToken));
    }

    @Test
    void tokenShouldBeInvalidWhenExpired() {
        JwtService jwtService = buildJwtService("test-secret-key-test-secret-key-test-secret-key-123", -1, 7200);
        NguoiDung user = buildUser();

        String expiredAccessToken = jwtService.generateAccessToken(user);

        assertFalse(jwtService.isAccessTokenValid(expiredAccessToken));
    }

    @Test
    void tokenShouldBeInvalidWhenMalformed() {
        JwtService jwtService = buildJwtService("test-secret-key-test-secret-key-test-secret-key-123", 3600, 7200);
        assertFalse(jwtService.isAccessTokenValid("not-a-jwt-token"));
        assertFalse(jwtService.isRefreshTokenValid("not-a-jwt-token"));
    }

    private JwtService buildJwtService(String secret, long accessSeconds, long refreshSeconds) {
        AuthProperties authProperties = new AuthProperties();
        authProperties.getJwt().setIssuer("auth-service-test");
        authProperties.getJwt().setSecret(secret);
        authProperties.getJwt().setAccessTokenSeconds(accessSeconds);
        authProperties.getJwt().setRefreshTokenSeconds(refreshSeconds);
        return new JwtService(authProperties);
    }

    private NguoiDung buildUser() {
        NhomQuyen role = new NhomQuyen();
        role.setId(1);
        role.setMaNhomQuyen("ADMIN");

        NguoiDung user = new NguoiDung();
        user.setId(1L);
        user.setUserName("admin");
        user.setNhomQuyen(role);
        return user;
    }
}
