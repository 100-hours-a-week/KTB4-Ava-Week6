package org.ktb.week6.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.ktb.week6.enums.StatusType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 댓글 고유 ID

    @NotNull
    private String content; // 댓글 내용

    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusType status; // 댓글 상태 (ACTIVE, DELETED)

    @NotNull
    private Boolean isEdited; // 수정 여부 (수정됨 표기용)

    @ManyToOne
    @JoinColumn(name = "post_id") // FK (comment.post_id) -> post.post_id
    private Post post; // 댓글 단 게시글

    @ManyToOne
    @JoinColumn(name = "user_id") // FK (comment.user_id) -> comment.comment_id
    @NotNull
    private User user; // 댓글 단 유저

    @OneToOne // 대댓을 기준으로 대댓은 부모를 하나만 가질 수 있음
    @JoinColumn(name = "parent_id") // FK(comment.comment_id) -> comment.comment_id
    private Comment parent; // 댓글의 고유 ID (대댓글인 경우에만 사용)

    @CreatedDate
    @NotNull
    private LocalDateTime createdAt;

    @LastModifiedDate
    @NotNull
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    protected Comment() {
    }

    public Comment(String content, Post post, User user, Comment parentComment) {
        this.content = content;
        this.status = StatusType.ACTIVE;
        this.isEdited = false;
        this.post = post;
        this.user = user;
        this.parent = parentComment;
    }

    public void updateContent(String newContent) {
        this.content = newContent;
        this.isEdited = true;
    }

    public void updateStatus(StatusType status) {
        this.status = status;
    }

}
