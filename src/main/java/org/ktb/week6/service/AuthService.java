package org.ktb.week6.service;

import lombok.RequiredArgsConstructor;
import org.ktb.week6.auth.JwtProvider;
import org.ktb.week6.dto.*;
import org.ktb.week6.entity.File;
import org.ktb.week6.entity.RefreshToken;
import org.ktb.week6.entity.User;
import org.ktb.week6.exception.AuthorizedException;
import org.ktb.week6.repository.FileRepository;
import org.ktb.week6.repository.RefreshTokenRepository;
import org.ktb.week6.repository.UserRepository;
import org.ktb.week6.utils.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
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
                        user,
                        LocalDateTime.now().plusDays(14)
                )
        );

        String profileImageUrl = null;
        if (user.getFile() != null) {
            File file = fileRepository.findById(user.getFile().getId()).orElseThrow(() -> new AuthorizedException("invalid_file"));
            profileImageUrl = FileUtils.toFullUrl(file.getPath());
        }

        return new AuthResultDto(
                AuthResponseDto.of(user, profileImageUrl, accessToken, jwtProvider.getAccessTokenValidityInMilliseconds()), refreshToken
        );
    }

    // 액세스 토큰 재발급
    @Transactional
    public TokenResultDto refreshAccessToken(String refreshToken) {
        RefreshToken saved = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AuthorizedException("unauthorized"));

        if (saved.isExpired()) {
            refreshTokenRepository.delete(saved);
            throw new AuthorizedException("unauthorized");
        }

        User user = userRepository.findById(saved.getUser().getId())
                .orElseThrow(() -> new AuthorizedException("unauthorized"));

        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getNickname());

        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());
        refreshTokenRepository.delete(saved);
        refreshTokenRepository.save(
                new RefreshToken(
                        newRefreshToken,
                        user,
                        LocalDateTime.now().plusDays(14)
                ));

        return new TokenResultDto(
                new TokenInfoDto(newAccessToken, jwtProvider.getAccessTokenValidityInMilliseconds()), newRefreshToken
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }
}
