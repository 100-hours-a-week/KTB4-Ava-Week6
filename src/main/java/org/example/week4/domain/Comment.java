package org.example.week4.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class Comment {
    private Long id; // 댓글 UUID
    private Long postId; // 댓글 단 게시글의 UUID
    private Long parentId; // 댓글의 UUID(대댓글인 경우에만 사용)
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private Boolean isEdited;

    public Comment(Long id, Long postId, Long userId, Long parentId, String content) {
        this.id = id;
        this.postId = postId;
        this.parentId = parentId;
        this.userId = userId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isDeleted = false;
        this.isEdited = false;
    }

    public void updateContent(String newContent) {
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
        this.updatedAt = LocalDateTime.now();
    }


}
