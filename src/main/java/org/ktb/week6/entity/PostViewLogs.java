package org.ktb.week6.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
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
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public class PostViewLogs {

    private static final long EXPIRATION_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "post_id")
    @NotNull
    private Post post;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;

    @CreatedDate
    @NotNull
    private LocalDateTime createdAt;

    protected PostViewLogs() {}

    public PostViewLogs(Post post, User user) {
        this.post = post;
        this.user = user;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(createdAt.plusHours(EXPIRATION_HOURS));
    }
}
