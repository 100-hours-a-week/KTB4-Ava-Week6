package org.ktb.week6.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "EVENT_POST_USER_UNIQUE",
                columnNames = {"eventPost_id", "user_id"}
        )
})
@Getter
@EntityListeners(AuditingEntityListener.class)
public class EventApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventPost_id", nullable = false)
    private EventPost eventPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected EventApplication() {
    }

    public EventApplication(EventPost eventPost, User user, Comment comment) {
        this.eventPost = eventPost;
        this.user = user;
        this.comment = comment;
    }

    public EventApplication(EventPost eventPost, User user) {
        this.eventPost = eventPost;
        this.user = user;
    }

    public void updateComment(Comment comment) {
        this.comment = comment;
    }

}
