package org.example.week4.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenInfoDto {
    private String accessToken;
    private long expiresIn;
}
