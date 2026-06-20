package org.ktb.week6.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.ktb.week6.enums.StatusType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue
    @Column(name = "post_id")
    private Long id;

    private String title;
    private String content;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long reportCount;
    private StatusType status;

    @ManyToOne()
    @JoinColumn(name = "user_id") // FK(post.user_id) -> user.user_id
    private User user;

    @OneToOne()
    @JoinColumn(name = "file_id")
    private File file;

    @OneToMany(mappedBy = "post")
    private List<Comment> comment = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    public Post(String title, String content, User user, File file) {
        this.title = title;
        this.content = content;
        this.viewCount = 0L;
        this.status = StatusType.ACTIVE;
        this.user = user;
        this.file = file;
    }

    public void updateTitle(@NotBlank @Size(max = 26) String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateFile(File file) {
        this.file = file;
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
        this.updatedAt = LocalDateTime.now();
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseReportCount() {
        this.reportCount++;
        this.updatedAt = LocalDateTime.now();
    }
}
