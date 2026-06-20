package org.ktb.week6.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Auth {
    private String accessToken;
    private String refreshToken;
}
