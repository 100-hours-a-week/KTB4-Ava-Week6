package org.example.week4.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.week4.domain.Comment;
import org.example.week4.domain.User;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class CommentResponseDto {
    private Long id;
    private Long postId;
    private Long parentId;
    private String content;
    private Long userId;
    private String nickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private Boolean isEdited;

    private List<CommentResponseDto> comments;

    public CommentResponseDto(Comment comment, User user) {
        this.id = comment.getId();
        this.postId = comment.getPostId();
        this.parentId = comment.getParentId();
        this.content = comment.getIsDeleted()
                ? "삭제된 댓글입니다."
                : comment.getContent();
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.isDeleted = comment.getIsDeleted();
        this.isEdited = comment.getIsEdited();
    }

    public CommentResponseDto(Comment comment, User user, List<CommentResponseDto> comments) {
        this.id = comment.getId();
        this.postId = comment.getPostId();
        this.parentId = comment.getParentId();
        this.content = comment.getIsDeleted()
                ? "삭제된 댓글입니다."
                : comment.getContent();
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.isDeleted = comment.getIsDeleted();
        this.isEdited = comment.getIsEdited();
        this.comments = comments;
    }
}
