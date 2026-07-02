package org.ktb.week6;

import lombok.RequiredArgsConstructor;
import org.ktb.week6.auth.JwtProvider;
import org.ktb.week6.exception.AuthorizedException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
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
        // Request Header에서 JWT 토큰 추출
        String authorizationHeader = webRequest.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthorizedException("토큰이 만료되었거나 유효하지 않습니다.");
        }

        String token = authorizationHeader.substring(7);
        return jwtProvider.getUserId(token);
    }
}
