package org.ktb.week6.service;

import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.*;
import org.ktb.week6.entity.RefreshToken;
import org.ktb.week6.entity.TokenBlacklist;
import org.ktb.week6.entity.User;
import org.ktb.week6.enums.StatusType;
import org.ktb.week6.exception.UnauthorizedException;
import org.ktb.week6.jwt.JwtProvider;
import org.ktb.week6.repository.RefreshTokenRepository;
import org.ktb.week6.repository.TokenBlacklistRepository;
import org.ktb.week6.repository.UserRepository;
import org.ktb.week6.utils.FileUtils;
import org.ktb.week6.utils.TokenUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final TokenBlacklistRepository tokenBlacklistRepository;
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

        String hashedRefreshToken = TokenUtils.hash(refreshToken);

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
        String hashedRefreshToken = TokenUtils.hash(refreshToken);
        RefreshToken savedRefreshToken = refreshTokenRepository.findByRefreshToken(hashedRefreshToken)
                .orElseThrow(() -> new UnauthorizedException("unauthorized"));

        // 리프레시 토큰이 만료된 경우 예외
        if (savedRefreshToken.isExpired()) {
            refreshTokenRepository.delete(savedRefreshToken);
            throw new UnauthorizedException("token_expired");
        }

        // 저장된 리프레시 토큰의 유저와 요청한 유저가 다르면 예외
        User user = userRepository.findById(savedRefreshToken.getUser().getId())
                .orElseThrow(() -> new UnauthorizedException("unauthorized"));

        // 삭제된 유저가 요청하면 예외
        if (user.getStatus() == StatusType.DELETED) throw new UnauthorizedException("invalid_credentials");

        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getNickname());

        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());
        String newHashedRefreshToken = TokenUtils.hash(newRefreshToken);

        refreshTokenRepository.delete(savedRefreshToken);
        refreshTokenRepository.save(
                new RefreshToken(
                        newHashedRefreshToken,
                        user,
                        LocalDateTime.now().plusDays(14)
                ));
        tokenBlacklistRepository.save(new TokenBlacklist(hashedRefreshToken, savedRefreshToken.getUser(), savedRefreshToken.getExpiresAt()));

        return new TokenResultDto(
                new TokenInfoDto(newAccessToken, jwtProvider.getAccessTokenValidityInMilliseconds()), newRefreshToken
        );
    }

    @Transactional
    public void logout(String refreshToken, String accessToken) {
        String hashedRefreshToken = TokenUtils.hash(refreshToken);
        refreshTokenRepository.deleteByRefreshToken(hashedRefreshToken);

        if (jwtProvider.validateToken(accessToken) && jwtProvider.isAccessToken(accessToken)) {
            Long userId = jwtProvider.getUserId(accessToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("unauthorized"));

            tokenBlacklistRepository.save(
                    new TokenBlacklist(
                            TokenUtils.hash(accessToken),
                            user,
                            jwtProvider.getExpiration(accessToken)
                    )
            );
        }
    }

}
