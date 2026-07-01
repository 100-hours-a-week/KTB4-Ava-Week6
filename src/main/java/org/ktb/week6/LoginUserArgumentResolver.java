package org.ktb.week6;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.auth.AuthConstants;
import org.ktb.week6.auth.JwtProvider;
import org.ktb.week6.exception.AuthorizedException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtProvider jwtProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라미터에 @Auth 어노테이션이 있는지 확인
        return parameter.hasParameterAnnotation(Auth.class);
    }

    @Override
    public Long resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object authenticatedUserId = webRequest.getAttribute(
                AuthConstants.AUTHENTICATED_USER_ID_ATTRIBUTE,
                RequestAttributes.SCOPE_REQUEST
        );
        if (authenticatedUserId instanceof Long userId) {
            return userId;
        }

        // Request Header에서 JWT 토큰 추출
        String authorizationHeader = webRequest.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthorizedException("token_expired");
        }

        String token = authorizationHeader.substring(7);
        try {
            return jwtProvider.getUserId(token);
        } catch (ExpiredJwtException e) {
            throw new AuthorizedException("token_expired");
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthorizedException("unauthorized");
        }
    }
}
