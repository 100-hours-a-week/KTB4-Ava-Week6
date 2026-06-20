package org.ktb.week6.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenInfoDto {
    private String accessToken;
    private long expiresIn;
}
