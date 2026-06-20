package org.ktb.week6.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class TokenBlacklist {

    @Id @GeneratedValue
    @Column(name = "token_blacklist_id")
    private Long id;

    @NotBlank
    private String token;

    @ManyToOne()
    @JoinColumn(name = "user_id") // FK (token_blacklist.user_id) -> user.user_id
    private User user;
    
    @NotBlank
    private LocalDateTime expiresAt;

    public TokenBlacklist(String token, User user, LocalDateTime expiresAt) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

}
