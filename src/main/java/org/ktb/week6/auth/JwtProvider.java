package org.ktb.week6.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtProperties;
    private Key key;

    // 서명 키 생성
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // 토큰 생성(access, refresh 공통)
    private String createToken(
            String type,
            Long userId,
            Map<String, Object> claims,
            long expSeconds
    ) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .claim("typ", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith((SecretKey) key, Jwts.SIG.HS256)
                .compact();
    }

    // 액세스 토큰 발급
    public String createAccessToken(Long userId, String email, String nickname) {
        return createToken(
                "access",
                userId,
                Map.of("email", email, "nickname", nickname),
                jwtProperties.getAccessTokenExpSeconds()
        );
    }

    // 재발급 전용 리프레시 토큰 발급
    public String createRefreshToken(Long userId) {
        return createToken(
                "refresh",
                userId,
                Map.of(),
                jwtProperties.getRefreshTokenExpSeconds()
        );
    }

    // 서명 키로 토큰 검증
    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token);
    }

    // 토큰 타입 구분
    public boolean isAccessToken(String token) {
        return "access".equals(parse(token).getPayload().get("typ", String.class));
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getPayload().getSubject());
    }

    // 액세스 토큰 만료 시간을 밀리초로 변환
    public Long getAccessTokenValidityInMilliseconds() {
        return jwtProperties.getAccessTokenExpSeconds() * 1000;
    }
}
