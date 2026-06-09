package org.example.week4.repository;

import org.example.week4.domain.RefreshToken;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    void delete(RefreshToken refreshToken);

    void delete(String token);

    void deleteByUserId(Long userId);
}
