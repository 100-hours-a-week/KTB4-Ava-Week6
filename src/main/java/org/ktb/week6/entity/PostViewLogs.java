package org.ktb.week6.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "POST_VIEW_LOGS_POST_USER_UNIQUE",
                columnNames = {"post_id", "user_id"}
        )
})
@Getter
@EntityListeners(AuditingEntityListener.class)
public class PostViewLogs {

    private static final long EXPIRATION_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastViewedAt;

    protected PostViewLogs() {}

    public PostViewLogs(Post post, User user, LocalDateTime lastViewedAt) {
        this.post = post;
        this.user = user;
        this.lastViewedAt = lastViewedAt;
    }

    public boolean canIncreaseViewCount(LocalDateTime now) {
        return !lastViewedAt.plusHours(24).isAfter(now);
    }

    public void updateLastViewedAt(LocalDateTime now) {
        this.lastViewedAt = now;
    }
}
