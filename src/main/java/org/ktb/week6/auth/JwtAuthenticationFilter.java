package org.ktb.week6.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.TokenResultDto;
import org.ktb.week6.exception.AuthorizedException;
import org.ktb.week6.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 인증이 필요없는 요청 화이트리스트에 포함
    private static final String[] WHITE_LIST = {
            "/auth/**",
            "/users/register",
            "/public/images/**",
            "/h2-console",
            "/h2-console/**",
    };
    private final JwtProvider jwtProvider;
    private final AuthService authService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    // 토큰 검증 수행 안 할 요청 선별
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod())
                || PatternMatchUtils.simpleMatch(WHITE_LIST, request.getRequestURI());
    }

    // 필터 적용
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 토큰이 없거나 Bearer가 아닌 경우 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            handleUnauthorized(request, response, "unauthorized");
            return;
        }

        String token = authHeader.substring(7);

        // 액세스 토큰 체크
        try {
            Jws<Claims> claims = jwtProvider.parse(token);

            if (!"access".equals(claims.getPayload().get("typ", String.class))) {
                throw new IllegalArgumentException("Not access token");
            }

            request.setAttribute(
                    AuthConstants.AUTHENTICATED_USER_ID_ATTRIBUTE,
                    Long.valueOf(claims.getPayload().getSubject())
            );
        } catch (ExpiredJwtException e) {
            if (!refreshExpiredAccessToken(request, response)) {
                return;
            }
        } catch (JwtException | IllegalArgumentException e) {
            handleUnauthorized(request, response, "unauthorized");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean refreshExpiredAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getCookieValue(request, AuthConstants.REFRESH_TOKEN_COOKIE_NAME);

        if (refreshToken == null || refreshToken.isBlank()) {
            handleUnauthorized(request, response, "token_expired");
            return false;
        }

        try {
            TokenResultDto result = authService.refreshAccessToken(refreshToken);
            String newAccessToken = result.getToken().getAccessToken();

            request.setAttribute(
                    AuthConstants.AUTHENTICATED_USER_ID_ATTRIBUTE,
                    jwtProvider.getUserId(newAccessToken)
            );
            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);
            response.addHeader(
                    HttpHeaders.SET_COOKIE,
                    AuthCookieUtils.createRefreshTokenCookie(result.getNewRefreshToken()).toString()
            );
            return true;
        } catch (AuthorizedException e) {
            handleUnauthorized(request, response, e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            handleUnauthorized(request, response, "unauthorized");
            return false;
        }
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, String message) {
        handlerExceptionResolver.resolveException(
                request,
                response,
                null,
                new AuthorizedException(message)
        );
    }
}
