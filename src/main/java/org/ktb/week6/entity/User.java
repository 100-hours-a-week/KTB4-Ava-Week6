package org.ktb.week6.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ktb.week6.enums.StatusType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    private String email;
    private String password;
    private String nickname;
    private StatusType status;

    @OneToOne()
    @JoinColumn(name = "file_id")
    private File file;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.status = StatusType.ACTIVE;
    }

    public User(String email, String password, String nickname, File file) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.file = file;
        this.status = StatusType.ACTIVE;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateStatus(StatusType status) {
        this.status = status;
    }

    public void deleteUser() {
        this.status = StatusType.DELETED;
    }

    public void updateFile(File file) {
        this.file = file;
    }
}
