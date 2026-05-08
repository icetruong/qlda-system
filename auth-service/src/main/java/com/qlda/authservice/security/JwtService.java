package com.qlda.authservice.security;

import com.qlda.authservice.config.AuthProperties;
import com.qlda.authservice.entity.NguoiDung;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;
import java.util.Locale;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JwtService {

    private final AuthProperties authProperties;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public JwtService(AuthProperties authProperties) {
        this.authProperties = authProperties;
        KeyPair keyPair = buildKeyPair(authProperties.getJwt());
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
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
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Map<String, Object> defaultClaims(NguoiDung user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", user.getId());
        claims.put("username", user.getUserName());
        String role = user.getNhomQuyen() == null ? "USER" : user.getNhomQuyen().getMaNhomQuyen();
        claims.put("roles", List.of(role.toUpperCase(Locale.ROOT)));
        claims.put("authorities", List.of("ROLE_" + role.toUpperCase(Locale.ROOT)));
        return claims;
    }

    private KeyPair buildKeyPair(AuthProperties.Jwt jwt) {
        try {
            if (StringUtils.hasText(jwt.getPrivateKey()) && StringUtils.hasText(jwt.getPublicKey())) {
                return new KeyPair(parsePublicKey(jwt.getPublicKey()), parsePrivateKey(jwt.getPrivateKey()));
            }
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to initialize RSA key pair for JWT", exception);
        }
    }

    private PrivateKey parsePrivateKey(String value) throws Exception {
        byte[] keyBytes = decodeKeyMaterial(value);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey parsePublicKey(String value) throws Exception {
        byte[] keyBytes = decodeKeyMaterial(value);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private byte[] decodeKeyMaterial(String value) {
        String material = resolveKeySource(value);
        String normalized = material
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    private String resolveKeySource(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.startsWith("classpath:")) {
            String classpathLocation = trimmed.substring("classpath:".length()).replaceFirst("^/+", "");
            try {
                ClassPathResource resource = new ClassPathResource(classpathLocation);
                return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException exception) {
                throw new IllegalArgumentException("Cannot read classpath key material: " + trimmed, exception);
            }
        }

        Path path = Path.of(trimmed);
        if (Files.exists(path)) {
            try {
                return Files.readString(path, StandardCharsets.UTF_8);
            } catch (IOException exception) {
                throw new IllegalArgumentException("Cannot read key material file: " + trimmed, exception);
            }
        }
        return trimmed;
    }
}
