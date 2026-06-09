package org.example.week4.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.week4.enums.UserStatus;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class User {
    private Long id;
    private String email;
    private String password;
    private String nickname;
    private Long fileId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public User(Long id, String email, String password, String nickname) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.status = String.valueOf(UserStatus.ACTIVE);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deletedAt = null;
    }

    public User(Long id, String email, String password, String nickname, Long fileId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.fileId = fileId;
        this.status = String.valueOf(UserStatus.ACTIVE);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deletedAt = null;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePassword(String password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfileImage(Long fileId) {
        this.fileId = fileId;
        this.updatedAt = LocalDateTime.now();
    }
}
