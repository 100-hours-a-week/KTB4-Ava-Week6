package org.ktb.week6.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.ktb.week6.enums.StatusType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(
                name = "EMAIL_UNIQUE",
                columnNames = {"email"}
        ),
        @UniqueConstraint(
                name = "NICKNAME_UNIQUE",
                columnNames = {"nickname"}
        )
})
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String email;

    @NotNull
    private String password;

    @NotNull
    private String nickname;

    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusType status;

    @OneToOne()
    @JoinColumn(name = "file_id") // FK (user.file_id) -> file.id
    private File file;

    @CreatedDate
    @NotNull
    private LocalDateTime createdAt;

    @LastModifiedDate
    @NotNull
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    protected User() {}

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

    public void deleteUser() {
        this.status = StatusType.DELETED;
    }

    public void updateFile(File file) {
        this.file = file;
    }
}
