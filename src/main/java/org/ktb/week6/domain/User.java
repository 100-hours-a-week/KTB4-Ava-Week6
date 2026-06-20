package org.ktb.week6.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.enums.StatusType;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class User {
    private Long id;
    private String email;
    private String password;
    private String nickname;
    private Long fileId;
    private StatusType status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public User(Long id, String email, String password, String nickname) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.status = StatusType.ACTIVE;
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
        this.status = StatusType.ACTIVE;
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

    public void updateStatus(StatusType status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfileImage(Long fileId) {
        this.fileId = fileId;
        this.updatedAt = LocalDateTime.now();
    }
}
