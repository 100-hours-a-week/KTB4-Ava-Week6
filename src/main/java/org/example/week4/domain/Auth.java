package org.example.week4.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Auth {
    private String accessToken;
    private String refreshToken;
}
