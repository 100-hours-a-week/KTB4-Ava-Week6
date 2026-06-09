package org.example.week4.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.week4.domain.User;

@Getter
@AllArgsConstructor
public class AuthResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private TokenInfoDto accessToken;


    public static AuthResponseDto of(
            User user,
            String profileImageUrl,
            String accessToken,
            long expiresIn
    ) {
        return new AuthResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                profileImageUrl,
                new TokenInfoDto(accessToken, expiresIn));
    }
}
