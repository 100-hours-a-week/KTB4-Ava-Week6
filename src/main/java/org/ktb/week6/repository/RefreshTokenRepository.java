package org.ktb.week6.repository;

import org.ktb.week6.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    void delete(RefreshToken refreshToken);

    void delete(String token);

    void deleteByUserId(Long userId);
}