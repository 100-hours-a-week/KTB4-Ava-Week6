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
    private String content;
    private Long userId;
    private String nickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private StatusType status;

    private List<CommentResponseDto> comments;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.postId = comment.getPost().getId();
        this.parentId = comment.getParent().getId();
        this.content = comment.getStatus() == StatusType.DELETED
                ? "삭제된 댓글입니다."
                : comment.getContent();
        this.userId = comment.getAuth().getId();
        this.nickname = comment.getAuth().getNickname();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.status = comment.getStatus();
    }

    // TODO: 대댓글 응답 수정
    public CommentResponseDto(Comment comment, List<CommentResponseDto> comments) {
        this.id = comment.getId();
        this.postId = comment.getPost().getId();
        this.parentId = comment.getParent().getId();
        this.content = comment.getStatus() == StatusType.DELETED
                ? "삭제된 댓글입니다."
                : comment.getContent();
        this.userId = comment.getAuth().getId();
        this.nickname = comment.getAuth().getNickname();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.status = comment.getStatus();
        this.comments = comments;
    }
}
