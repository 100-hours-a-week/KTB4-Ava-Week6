package org.example.week4.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.week4.domain.User;

@Getter
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;

    public UserResponseDto(User user, String profileImageUrl) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImageUrl = profileImageUrl;
    }
}