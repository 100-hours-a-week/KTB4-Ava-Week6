package org.ktb.week6.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ktb.week6.dto.AuthRequestDto;
import org.ktb.week6.dto.AuthResultDto;
import org.ktb.week6.dto.TokenResultDto;
import org.ktb.week6.entity.RefreshToken;
import org.ktb.week6.entity.User;
import org.ktb.week6.exception.UnauthorizedException;
import org.ktb.week6.jwt.JwtProvider;
import org.ktb.week6.repository.RefreshTokenRepository;
import org.ktb.week6.repository.TokenBlacklistRepository;
import org.ktb.week6.repository.UserRepository;
import org.ktb.week6.utils.TokenUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    private Long userId;
    private String email;
    private String password;
    private String encodedPassword;
    private String nickname;
    private String accessToken;
    private String refreshToken;
    private String newAccessToken;
    private String newRefreshToken;
    private Long accessTokenExpiresIn;
    private User savedUser;
    private AuthRequestDto authRequestDto;

    @BeforeEach
    void initData() {
        userId = 1L;

        email = "test@test.com";
        password = "Test1234!!";
        encodedPassword = "encodedPassword";
        nickname = "test";

        accessToken = "accessToken";
        refreshToken = "refreshToken";
        newAccessToken = "newAccessToken";
        newRefreshToken = "newRefreshToken";
        accessTokenExpiresIn = 3_600_000L;

        savedUser = new User(email, encodedPassword, nickname);
        ReflectionTestUtils.setField(savedUser, "id", userId);

        authRequestDto = new AuthRequestDto(email, password);
    }

    @Test
    @DisplayName("로그인에 성공한다")
    void login_success() {
        // Given
        String hashedRefreshToken = TokenUtils.hash(refreshToken); // 리프레시 토큰 해시값 미리 계산

        given(userRepository.findByEmail(email)).willReturn(Optional.of(savedUser));
        given(passwordEncoder.matches(password, encodedPassword)).willReturn(true);
        given(jwtProvider.createAccessToken(userId, email, nickname)).willReturn(accessToken);
        given(jwtProvider.createRefreshToken(userId)).willReturn(refreshToken);
        given(jwtProvider.getAccessTokenValidityInMilliseconds()).willReturn(accessTokenExpiresIn);

        // When
        AuthResultDto resultDto = authService.login(authRequestDto);

        // Then
        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        assertThat(resultDto.getRefreshToken()).isEqualTo(refreshToken); // 쿠키로 내려갈 리프레시 토큰이 발급된 원본 토큰인지
        assertThat(resultDto.getResponse().getId()).isEqualTo(userId);
        assertThat(resultDto.getResponse().getEmail()).isEqualTo(email);
        assertThat(resultDto.getResponse().getNickname()).isEqualTo(nickname);
        assertThat(resultDto.getResponse().getProfileImageUrl()).isNull();
        assertThat(resultDto.getResponse().getAccessToken().getAccessToken()).isEqualTo(accessToken); // 응답 DTO에 액세스 토큰이 담기는지
        assertThat(resultDto.getResponse().getAccessToken().getExpiresIn()).isEqualTo(accessTokenExpiresIn); // 응답 DTO에 만료 시간이 담기는지

        verify(passwordEncoder).matches(password, encodedPassword);
        verify(jwtProvider).createAccessToken(userId, email, nickname); // 액세스 토큰이 발급됐는지
        verify(jwtProvider).createRefreshToken(userId); // 리프레시 토큰이 발급됐는지
        verify(refreshTokenRepository).deleteByUserId(userId); // 기존 리프레시 토큰을 삭제하는지
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture()); // 새 리프레시 토큰 저장 객체를 캡처

        RefreshToken capturedRefreshToken = refreshTokenCaptor.getValue();

        assertThat(capturedRefreshToken.getRefreshToken()).isEqualTo(hashedRefreshToken); // DB에는 해시된 리프레시 토큰이 저장되는지
        assertThat(capturedRefreshToken.getUser()).isEqualTo(savedUser); // 저장된 토큰이 로그인 유저와 연결되는지
        assertThat(capturedRefreshToken.getExpiresAt()).isAfter(LocalDateTime.now()); // 만료 시간이 현재보다 미래인지
    }

    @Test
    @DisplayName("요청한 유저의 이메일이 존재하지 않으면 로그인에 실패한다")
    void login_fail_emailNotFound() {
        // Given
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> authService.login(authRequestDto))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("invalid_credentials");

        verify(passwordEncoder, never()).matches(anyString(), anyString()); // 유저 조회 실패 시 비밀번호 검증을 하지 않는지
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class)); // 로그인 실패 시 리프레시 토큰을 저장하지 않는지
    }

    @Test
    @DisplayName("요청한 유저가 이미 탈퇴된 유저면 로그인에 실패한다")
    void login_fail_deletedUser() {
        // Given
        savedUser.deleteUser(); // 탈퇴한 유저 상태로 변경
        given(userRepository.findByEmail(email)).willReturn(Optional.of(savedUser));

        // When + Then
        assertThatThrownBy(() -> authService.login(authRequestDto))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("invalid_credentials");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
    void login_fail_passwordMismatch() {
        // Given
        given(userRepository.findByEmail(email)).willReturn(Optional.of(savedUser));
        given(passwordEncoder.matches(password, encodedPassword)).willReturn(false);

        // When + Then
        assertThatThrownBy(() -> authService.login(authRequestDto))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("invalid_credentials");

        verify(passwordEncoder).matches(password, encodedPassword);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("액세스 토큰 재발급에 성공한다")
    void refreshAccessToken_success() {
        // Given
        String hashedRefreshToken = TokenUtils.hash(refreshToken); // 요청 리프레시 토큰 조회용 해시값
        String newHashedRefreshToken = TokenUtils.hash(newRefreshToken); // 재발급된 리프레시 토큰 저장 검증용 해시값
        RefreshToken savedRefreshToken = new RefreshToken(
                hashedRefreshToken,
                savedUser,
                LocalDateTime.now().plusDays(1) // 아직 만료되지 않은 토큰
        );

        given(refreshTokenRepository.findByRefreshToken(hashedRefreshToken)).willReturn(Optional.of(savedRefreshToken));
        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));
        given(jwtProvider.createAccessToken(userId, email, nickname)).willReturn(newAccessToken);
        given(jwtProvider.createRefreshToken(userId)).willReturn(newRefreshToken);
        given(jwtProvider.getAccessTokenValidityInMilliseconds()).willReturn(accessTokenExpiresIn);

        // When
        TokenResultDto resultDto = authService.refreshAccessToken(refreshToken);

        // Then
        assertThat(resultDto.getToken().getAccessToken()).isEqualTo(newAccessToken);
        assertThat(resultDto.getToken().getExpiresIn()).isEqualTo(accessTokenExpiresIn);
        assertThat(resultDto.getNewRefreshToken()).isEqualTo(newRefreshToken); // 새 리프레시 토큰이 반환되는지

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        verify(jwtProvider).createAccessToken(userId, email, nickname);
        verify(jwtProvider).createRefreshToken(userId);
        verify(refreshTokenRepository).delete(savedRefreshToken); // 기존 리프레시 토큰을 삭제하는지
        verify(tokenBlacklistRepository).save(any()); // 기존 리프레시 토큰을 블랙리스트에 등록하는지
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture()); // 새 리프레시 토큰 저장 객체를 캡처

        RefreshToken capturedRefreshToken = refreshTokenCaptor.getValue();

        assertThat(capturedRefreshToken.getRefreshToken()).isEqualTo(newHashedRefreshToken); // 새 리프레시 토큰도 해시되어 저장되는지
        assertThat(capturedRefreshToken.getUser()).isEqualTo(savedUser);
        assertThat(capturedRefreshToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("요청한 리프레시 토큰이 DB에 존재하지 않으면 액세스 토큰 재발급에 실패한다")
    void refreshAccessToken_fail_refreshTokenNotFound() {
        // Given
        String hashedRefreshToken = TokenUtils.hash(refreshToken);
        given(refreshTokenRepository.findByRefreshToken(hashedRefreshToken)).willReturn(Optional.empty()); // DB에 저장된 리프레시 토큰이 없다고 가정

        // When + Then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("unauthorized");

        verify(refreshTokenRepository).findByRefreshToken(hashedRefreshToken);
        verify(userRepository, never()).findById(anyLong()); // 토큰 조회 실패 시 유저 조회 안하는지
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("요청한 리프레시 토큰이 만료되면 액세스 토큰 재발급에 실패한다")
    void refreshAccessToken_fail_expiredRefreshToken() {
        // Given
        String hashedRefreshToken = TokenUtils.hash(refreshToken);
        RefreshToken savedRefreshToken = new RefreshToken(
                hashedRefreshToken,
                savedUser,
                LocalDateTime.now().minusDays(1) // 이미 만료된 토큰
        );
        given(refreshTokenRepository.findByRefreshToken(hashedRefreshToken)).willReturn(Optional.of(savedRefreshToken));

        // When + Then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("token_expired");

        verify(refreshTokenRepository).findByRefreshToken(hashedRefreshToken);
        verify(refreshTokenRepository).delete(savedRefreshToken); // 만료된 리프레시 토큰은 삭제하는지
        verify(userRepository, never()).findById(anyLong());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("요청한 유저와 DB에 저장된 토큰의 유저가 다르면 액세스 토큰 재발급에 실패한다")
    void refreshAccessToken_fail_userMismatch() {
        // Given
        String hashedRefreshToken = TokenUtils.hash(refreshToken);
        RefreshToken savedRefreshToken = new RefreshToken(
                hashedRefreshToken,
                savedUser,
                LocalDateTime.now().plusDays(1)
        );
        given(refreshTokenRepository.findByRefreshToken(hashedRefreshToken)).willReturn(Optional.of(savedRefreshToken));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // When + Then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("unauthorized");

        verify(refreshTokenRepository).findByRefreshToken(hashedRefreshToken);
        verify(userRepository).findById(userId);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("요청한 유저가 삭제된 유저면 액세스 토큰 재발급에 실패한다")
    void refreshAccessToken_fail_deletedUser() {
        // Given
        String hashedRefreshToken = TokenUtils.hash(refreshToken);
        savedUser.deleteUser(); // 삭제된 유저 상태로 변경
        RefreshToken savedRefreshToken = new RefreshToken(
                hashedRefreshToken,
                savedUser,
                LocalDateTime.now().plusDays(1)
        );
        given(refreshTokenRepository.findByRefreshToken(hashedRefreshToken)).willReturn(Optional.of(savedRefreshToken));
        given(userRepository.findById(userId)).willReturn(Optional.of(savedUser));

        // When + Then
        assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("invalid_credentials");

        verify(refreshTokenRepository).findByRefreshToken(hashedRefreshToken);
        verify(userRepository).findById(userId);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }
}
