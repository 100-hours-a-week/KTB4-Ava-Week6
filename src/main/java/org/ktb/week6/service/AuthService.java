package org.ktb.week6.service;

import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.*;
import org.ktb.week6.entity.RefreshToken;
import org.ktb.week6.entity.User;
import org.ktb.week6.enums.StatusType;
import org.ktb.week6.exception.UnauthorizedException;
import org.ktb.week6.jwt.JwtProvider;
import org.ktb.week6.repository.RefreshTokenRepository;
import org.ktb.week6.repository.UserRepository;
import org.ktb.week6.utils.FileUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Validated
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 로그인
    @Transactional
    public AuthResultDto login(AuthRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UnauthorizedException("invalid_credentials"));

        if (user.getStatus() == StatusType.DELETED) throw new UnauthorizedException("invalid_credentials");

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("invalid_credentials");
        }

        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );

        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        String hashedRefreshToken = hash(refreshToken);

        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(
                new RefreshToken(
                        hashedRefreshToken,
                        user,
                        LocalDateTime.now().plusDays(14)
                )
        );

        String profileImageUrl = null;
        if (user.getFile() != null) {
            profileImageUrl = FileUtils.toFullUrl(user.getFile().getPath());
        }

        return new AuthResultDto(
                AuthResponseDto.of(user, profileImageUrl, accessToken, jwtProvider.getAccessTokenValidityInMilliseconds()), refreshToken
        );
    }

    // 액세스 토큰 재발급
    @Transactional
    public TokenResultDto refreshAccessToken(String refreshToken) {
        String hashedRefreshToken = hash(refreshToken);
        RefreshToken savedRefreshToken = refreshTokenRepository.findByRefreshToken(hashedRefreshToken)
                .orElseThrow(() -> new UnauthorizedException("unauthorized"));

        // 리프레시 토큰이 만료된 경우 예외
        if (savedRefreshToken.isExpired()) {
            refreshTokenRepository.delete(savedRefreshToken);
            // TODO: 기존 리프레시 토큰 블랙 리스트에 등록
            throw new UnauthorizedException("token_expired");
        }

        // 저장된 리프레시 토큰의 유저와 요청한 유저가 다르면 예외
        User user = userRepository.findById(savedRefreshToken.getUser().getId())
                .orElseThrow(() -> new UnauthorizedException("unauthorized"));

        // 삭제된 유저가 요청하면 예외
        if (user.getStatus() == StatusType.DELETED) throw new UnauthorizedException("invalid_credentials");

        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getNickname());

        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());
        String newHashedRefreshToken = hash(newRefreshToken);

        refreshTokenRepository.delete(savedRefreshToken);
        // TODO: 기존 리프레시 토큰 블랙리스트 등록
        refreshTokenRepository.save(
                new RefreshToken(
                        newHashedRefreshToken,
                        user,
                        LocalDateTime.now().plusDays(14)
                ));

        return new TokenResultDto(
                new TokenInfoDto(newAccessToken, jwtProvider.getAccessTokenValidityInMilliseconds()), newRefreshToken
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        String hashedRefreshToken = hash(refreshToken);
        refreshTokenRepository.deleteByRefreshToken(hashedRefreshToken);
    }

    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA_256_ALGORITHM_NOT_AVAILABLE", e);
        }
    }
}
