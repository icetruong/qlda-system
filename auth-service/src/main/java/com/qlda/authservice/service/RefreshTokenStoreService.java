package com.qlda.authservice.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenStoreService {

    private final Map<String, RefreshTokenEntry> tokenStore = new ConcurrentHashMap<>();

    public void save(String token, Long userId, String username, Instant expiresAt) {
        tokenStore.put(token, new RefreshTokenEntry(userId, username, expiresAt, false));
    }

    public boolean isValid(String token) {
        RefreshTokenEntry entry = tokenStore.get(token);
        if (entry == null || entry.revoked()) {
            return false;
        }
        if (entry.expiresAt().isBefore(Instant.now())) {
            tokenStore.remove(token);
            return false;
        }
        return true;
    }

    public void revoke(String token) {
        RefreshTokenEntry entry = tokenStore.get(token);
        if (entry == null) {
            return;
        }
        tokenStore.put(token, new RefreshTokenEntry(entry.userId(), entry.username(), entry.expiresAt(), true));
    }

    public void replace(String oldToken, String newToken, Long userId, String username, Instant expiresAt) {
        revoke(oldToken);
        save(newToken, userId, username, expiresAt);
    }

    private record RefreshTokenEntry(Long userId, String username, Instant expiresAt, boolean revoked) {
    }
}
