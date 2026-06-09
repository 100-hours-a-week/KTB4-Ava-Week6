package org.example.week4.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class Post {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Long fileId;
    private int likeCount;
    private int commentCount;
    private int viewCount;
    private int reportCount;
    private Boolean isBlind;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Post(Long id, Long userId, String title, String content) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.likeCount = 0;
        this.commentCount = 0;
        this.viewCount = 0;
        this.reportCount = 0;
        this.isBlind = false;
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Post(Long id, Long userId, String title, String content, Long fileId) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.fileId = fileId;
        this.likeCount = 0;
        this.commentCount = 0;
        this.viewCount = 0;
        this.reportCount = 0;
        this.isBlind = false;
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTitle(@NotBlank @Size(max = 26) String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateFileId(Long fileId) {
        this.fileId = fileId;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateIsBlind(boolean isBlind) {
        this.isBlind = isBlind;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
        this.updatedAt = LocalDateTime.now();
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
