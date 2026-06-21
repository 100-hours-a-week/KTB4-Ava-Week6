package org.ktb.week6.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.ktb.week6.enums.StatusType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 26)
    @NotNull
    private String title;

    @Column(columnDefinition = "TEXT")
    @NotNull
    private String content;

    @NotNull
    private Long viewCount;

    @NotNull
    private Long likeCount;

    @NotNull
    private Long commentCount;

    @NotNull
    private Long reportCount;

    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusType status;

    @NotNull
    private Boolean isEdited;

    @ManyToOne()
    @JoinColumn(name = "user_id") // FK(post.user_id) -> user.user_id
    @NotNull
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @OneToMany(mappedBy = "post")
    private List<Comment> comment = new ArrayList<>();

    @CreatedDate
    @NotNull
    private LocalDateTime createdAt;

    @LastModifiedDate
    @NotNull
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

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
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
