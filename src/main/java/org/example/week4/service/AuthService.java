package org.example.week4.service;

import lombok.RequiredArgsConstructor;
import org.example.week4.auth.JwtProvider;
import org.example.week4.domain.File;
import org.example.week4.domain.RefreshToken;
import org.example.week4.domain.User;
import org.example.week4.dto.*;
import org.example.week4.exception.AuthorizedException;
import org.example.week4.repository.FileRepository;
import org.example.week4.repository.RefreshTokenRepository;
import org.example.week4.repository.UserRepository;
import org.example.week4.utils.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Service
@Validated
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FileRepository fileRepository;
    private final JwtProvider jwtProvider;

    // 로그인
    public AuthResultDto login(AuthRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AuthorizedException("invalid_credentials"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new AuthorizedException("invalid_credentials");
        }

        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );

        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(
                new RefreshToken(
                        refreshToken,
                        user.getId(),
                        LocalDateTime.now().plusDays(14)
                )
        );

        String profileImageUrl = null;
        if (user.getFileId() != null) {
            File file = fileRepository.findById(user.getFileId()).orElseThrow(() -> new AuthorizedException("invalid_file"));
            profileImageUrl = FileUtils.toFullUrl(file.getPath());
        }

        return new AuthResultDto(
                AuthResponseDto.of(user, profileImageUrl, accessToken, jwtProvider.getAccessTokenValidityInMilliseconds()), refreshToken
        );
    }

    // 액세스 토큰 재발급
    public TokenResultDto refreshAccessToken(String refreshToken) {
        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthorizedException("unauthorized"));

        if (saved.isExpired()) {
            refreshTokenRepository.delete(saved);
            throw new AuthorizedException("unauthorized");
        }

        User user = userRepository.findById(saved.getUserId())
                .orElseThrow(() -> new AuthorizedException("unauthorized"));

        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getNickname());

        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());
        refreshTokenRepository.delete(saved);
        refreshTokenRepository.save(
                new RefreshToken(
                        newRefreshToken,
                        user.getId(),
                        LocalDateTime.now().plusDays(14)
                ));
        return new TokenResultDto(
                new TokenInfoDto(newAccessToken, jwtProvider.getAccessTokenValidityInMilliseconds()), newRefreshToken
        );
    }

    public void logout(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }
}
