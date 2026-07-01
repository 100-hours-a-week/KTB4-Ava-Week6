package org.ktb.week6.auth;

import org.springframework.http.ResponseCookie;

public final class AuthCookieUtils {
    private AuthCookieUtils() {
    }

    public static ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(AuthConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(AuthConstants.REFRESH_TOKEN_MAX_AGE_SECONDS)
                .sameSite("Lax")
                .build();
    }

    public static ResponseCookie expireRefreshTokenCookie() {
        return ResponseCookie.from(AuthConstants.REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
}
