package org.ktb.week6.auth;

public final class AuthConstants {
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    public static final String AUTHENTICATED_USER_ID_ATTRIBUTE = "authenticatedUserId";
    public static final int REFRESH_TOKEN_MAX_AGE_SECONDS = 14 * 24 * 60 * 60;

    private AuthConstants() {
    }
}
