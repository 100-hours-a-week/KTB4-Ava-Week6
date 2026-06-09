package org.example.week4.repository;

import org.example.week4.domain.RefreshToken;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemoryRefreshTokenRepository implements RefreshTokenRepository {

    private final ConcurrentHashMap<String, RefreshToken> refreshTokens = new ConcurrentHashMap<>();

    public RefreshToken save(RefreshToken refreshToken) {
        deleteByUserId(refreshToken.getUserId());
        RefreshToken saved = refreshTokens.put(refreshToken.getToken(), refreshToken);
        return saved;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return Optional.ofNullable(refreshTokens.get(token));
    }

    public void delete(RefreshToken refreshToken) {
        refreshTokens.remove(refreshToken.getToken());
    }

    public void delete(String refreshToken) {
        refreshTokens.remove(refreshToken);
    }

    public void deleteByUserId(Long userId) {
        refreshTokens.values().removeIf(refreshToken -> Objects.equals(refreshToken.getUserId(), userId));
    }
}
