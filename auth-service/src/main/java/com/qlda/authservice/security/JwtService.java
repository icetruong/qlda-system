package com.qlda.authservice.security;

import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.entity.NguoiDung;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JwtService {

    private final AuthProperties authProperties;
    private final SecretKey secretKey;

    public JwtService(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.secretKey = buildSecretKey(authProperties.getJwt().getSecret());
    }

    public String generateAccessToken(NguoiDung user) {
        Map<String, Object> claims = defaultClaims(user);
        claims.put("type", "access");
        return generateToken(user.getUserName(), claims, authProperties.getJwt().getAccessTokenSeconds());
    }

    public String generateRefreshToken(NguoiDung user) {
        Map<String, Object> claims = defaultClaims(user);
        claims.put("type", "refresh");
        return generateToken(user.getUserName(), claims, authProperties.getJwt().getRefreshTokenSeconds());
    }

    public long getAccessTokenSeconds() {
        return authProperties.getJwt().getAccessTokenSeconds();
    }

    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);
        Number value = claims.get("uid", Number.class);
        return value == null ? null : value.longValue();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isAccessTokenValid(String token) {
        return isTokenValid(token, "access");
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token, "refresh");
    }

    private boolean isTokenValid(String token, String type) {
        try {
            Claims claims = parseClaims(token);
            String tokenType = claims.get("type", String.class);
            if (!type.equals(tokenType)) {
                return false;
            }
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private String generateToken(String subject, Map<String, Object> claims, long expiresInSeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(authProperties.getJwt().getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiresInSeconds)))
                .id(UUID.randomUUID().toString())
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Map<String, Object> defaultClaims(NguoiDung user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", user.getId());
        claims.put("username", user.getUserName());
        claims.put("role", user.getNhomQuyen() == null ? "" : user.getNhomQuyen().getMaNhomQuyen());
        return claims;
    }

    private SecretKey buildSecretKey(String configuredSecret) {
        String secret = configuredSecret;
        if (!StringUtils.hasText(secret)) {
            secret = UUID.randomUUID() + UUID.randomUUID().toString().replace("-", "");
        }
        byte[] keyBytes = decodeSecret(secret);
        if (keyBytes.length < 32) {
            keyBytes = String.format("%-32s", secret).substring(0, 32).getBytes(StandardCharsets.UTF_8);
        }
        Key key = Keys.hmacShaKeyFor(keyBytes);
        return (SecretKey) key;
    }

    private byte[] decodeSecret(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (RuntimeException exception) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }
}
