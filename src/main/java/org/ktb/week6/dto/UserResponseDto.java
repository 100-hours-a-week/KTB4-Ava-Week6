package org.ktb.week6.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ktb.week6.entity.User;
import org.ktb.week6.utils.FileUtils;

@Getter
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String profileImageUrl;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getFile() == null ? null : FileUtils.toFullUrl(user.getFile().getPath());
    }
}