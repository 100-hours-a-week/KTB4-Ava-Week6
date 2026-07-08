package org.ktb.week6.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import org.ktb.week6.enums.ActionType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "POST_VERSION_UNIQUE",
                columnNames = {"post_id", "version"}
        )
})
@Getter
@EntityListeners(AuditingEntityListener.class)
public class PostHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;

    @Column(length = 26)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Positive
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected PostHistory() {}

    public PostHistory(ActionType action, String title, String content, Long version, Post post, User user, File file) {
        this.action = action;
        this.title = title;
        this.content = content;
        this.version = version;
        this.post = post;
        this.user = user;
        this.file = file;
    }

}
