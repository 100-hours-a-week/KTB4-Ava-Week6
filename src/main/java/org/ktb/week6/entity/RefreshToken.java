package org.ktb.week6.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue
    @Column(name = "refresh_token_id")
    private Long id;

    private String refreshToken;

    @ManyToOne()
    @JoinColumn(name = "user_id") // FK(refresh_token.user_id) -> user.user_id
    private User user;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public RefreshToken(String refreshToken, User user, LocalDateTime expiresAt) {
        this.refreshToken = refreshToken;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

}
