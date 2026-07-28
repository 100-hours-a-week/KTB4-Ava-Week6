package org.ktb.week6.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ktb.week6.entity.Comment;
import org.ktb.week6.enums.StatusType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class CommentResponseDto {
    private Long id;
    private Long postId;
    private Long parentId;
    private Long userId;
    private String content;
    private String nickname;
    private Boolean isDeleted;
    private Boolean isEdited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<CommentResponseDto> comments;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.postId = comment.getPost().getId();
        this.parentId = getParentId(comment);
        this.userId = comment.getUser().getId();
        this.content = comment.getStatus() == StatusType.DELETED
                ? "삭제된 댓글입니다."
                : comment.getContent();
        this.isDeleted = comment.getStatus() == StatusType.DELETED;
        this.isEdited = comment.getIsEdited();
        this.nickname = comment.getUser().getStatus() == StatusType.DELETED ? "탈퇴한 사용자" : comment.getUser().getNickname();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }

    public CommentResponseDto(Comment comment, List<CommentResponseDto> comments) {
        this.id = comment.getId();
        this.postId = comment.getPost().getId();
        this.parentId = getParentId(comment);
        this.userId = comment.getUser().getId();
        this.content = comment.getStatus() == StatusType.DELETED
                ? "삭제된 댓글입니다."
                : comment.getContent();
        this.nickname = comment.getUser().getStatus() == StatusType.DELETED ? "탈퇴한 사용자" : comment.getUser().getNickname();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.comments = comments;
    }

    private Long getParentId(Comment comment) {
        return comment.getParent() == null ? null : comment.getParent().getId();
    }
}
