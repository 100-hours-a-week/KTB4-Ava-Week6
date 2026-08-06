package org.ktb.week6.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.ktb.week6.dto.TemporaryPostRequestDto;
import org.ktb.week6.enums.PostType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "TEMPORARY_POST_USER_UNIQUE",
                columnNames = {"user_id"}
        )
})
@Getter
@EntityListeners(AuditingEntityListener.class)
public class TemporaryPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @Enumerated(EnumType.STRING)
    private PostType type;

    private int capacity;

    private LocalDateTime deadline;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected TemporaryPost() {
    }

    public TemporaryPost(TemporaryPostRequestDto request, User user, File file) {
        this.title = request.getTitle();
        this.content = request.getContent();
        this.user = user;
        this.file = file;
        this.type = request.getType();
        this.capacity = request.getCapacity();
        this.deadline = request.getDeadline();
    }

    public void updateTitle(@Size(max = 26) String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateFile(File file) {
        this.file = file;
    }

    public void updateType(PostType type) {
        this.type = type;
    }

    public void updateCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void updateDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}
