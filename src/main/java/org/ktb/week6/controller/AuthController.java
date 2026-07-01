package org.ktb.week6.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.auth.AuthConstants;
import org.ktb.week6.auth.AuthCookieUtils;
import org.ktb.week6.dto.*;
import org.ktb.week6.enums.ApiResultStatus;
import org.ktb.week6.exception.AuthorizedException;
import org.ktb.week6.response.ApiResponse;
import org.ktb.week6.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody AuthRequestDto request, HttpServletResponse httpResponse) {
        AuthResultDto result = authService.login(request);

        ResponseCookie refreshCookie = AuthCookieUtils.createRefreshTokenCookie(result.getRefreshToken());

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.of(ApiResultStatus.SUCCESS, "login_success", result.getResponse()));

    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue(name = AuthConstants.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken, HttpServletResponse httpResponse) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        // 쿠키 삭제 명령
        ResponseCookie cookie = AuthCookieUtils.expireRefreshTokenCookie();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.of(ApiResultStatus.SUCCESS, "logout_success", null));
    }

    // 액세스 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenInfoDto>> refreshAccessToken(@CookieValue(name = AuthConstants.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
                                                                        HttpServletResponse httpResponse
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthorizedException("unauthorized");
        }

        TokenResultDto result = authService.refreshAccessToken(refreshToken);

        if (result.getNewRefreshToken() != null) {
            ResponseCookie cookie = AuthCookieUtils.createRefreshTokenCookie(result.getNewRefreshToken());
            httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.of(ApiResultStatus.SUCCESS, "token_refresh_success", result.getToken()));
    }
}
