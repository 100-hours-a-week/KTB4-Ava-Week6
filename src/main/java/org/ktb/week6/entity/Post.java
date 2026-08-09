package org.ktb.week6.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.ktb.week6.enums.StatusType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 26, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private Long viewCount;

    @Column(nullable = false)
    private Long likeCount;

    @Column(nullable = false)
    private Long commentCount;

    @Column(nullable = false)
    private Long reportCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status;

    @Column(nullable = false)
    private Boolean isEdited;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post")
    private List<Comment> comment = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "post")
    private EventPost eventPost;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    protected Post() {}

    public Post(String title, String content, User user, File file) {
        this.title = title;
        this.content = content;
        this.viewCount = 0L;
        this.likeCount = 0L;
        this.commentCount = 0L;
        this.reportCount = 0L;
        this.status = StatusType.ACTIVE;
        this.isEdited = false;
        this.user = user;
        this.file = file;
    }

    public void updateTitle(@NotBlank @Size(max = 26) String title) {
        this.title = title;
        this.isEdited = true;
    }

    public void updateContent(String content) {
        this.content = content;
        this.isEdited = true;
    }

    public void updateFile(File file) {
        this.file = file;
        this.isEdited = true;
    }

    public void updateStatus(StatusType status) {
        this.status = status;
        // 삭제일 경우 삭제일 시간도 반영
        if (status.equals(StatusType.DELETED)) {
            this.deletedAt = LocalDateTime.now();
        }
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseReportCount() {
        this.reportCount++;
    }
}
