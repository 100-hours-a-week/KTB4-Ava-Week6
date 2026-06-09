package org.example.week4.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.week4.domain.Post;
import org.example.week4.domain.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private Long userId;
    private String nickname;
    private String title;
    private String content;
    private String imageUrl;
    private int likeCount;
    private int commentCount;
    private int viewsCount;
    private int reportsCount;
    private Boolean isBlind;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostResponseDto(Post post, User user) {
        this.id = post.getId();
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.viewsCount = post.getViewCount();
        this.reportsCount = post.getReportCount();
        this.isBlind = post.getIsBlind();
        this.isDeleted = post.getIsDeleted();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }

    public PostResponseDto(Post post, User user, String imageUrl) {
        this.id = post.getId();
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.imageUrl = imageUrl;
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.viewsCount = post.getViewCount();
        this.reportsCount = post.getReportCount();
        this.isBlind = post.getIsBlind();
        this.isDeleted = post.getIsDeleted();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}

