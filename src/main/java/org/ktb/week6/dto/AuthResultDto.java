package org.ktb.week6.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResultDto {
    private AuthResponseDto response; // response body
    private String refreshToken; // cookie
}
