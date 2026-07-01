package org.ktb.week6.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ktb.week6.entity.Post;
import org.ktb.week6.enums.StatusType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private String nickname;
    private String title;
    private String content;
    private String imageUrl;
    private Boolean isEdited;
    private Long likeCount;
    private Long commentCount;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.nickname = post.getUser().getStatus() == StatusType.DELETED ? "탈퇴한 사용자" : post.getUser().getNickname();
        this.title = post.getStatus() == StatusType.BLIND ? "숨김 처리된 게시글입니다." : post.getTitle();
        this.content = post.getStatus() == StatusType.BLIND ? "누적 신고 5회 이상으로 숨겨진 게시글입니다." : post.getContent();
        this.imageUrl = post.getFile() == null ? null : post.getFile().getUrl();
        this.isEdited = post.getIsEdited();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}

