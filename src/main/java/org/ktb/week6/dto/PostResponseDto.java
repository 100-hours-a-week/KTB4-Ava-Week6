package org.ktb.week6.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ktb.week6.entity.EventPost;
import org.ktb.week6.entity.Post;
import org.ktb.week6.enums.EventPostStatusType;
import org.ktb.week6.enums.PostType;
import org.ktb.week6.enums.StatusType;
import org.ktb.week6.utils.FileUtils;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private Long userId;
    private String nickname;
    private String title;
    private String content;
    private String postImageUrl;
    private String userImageUrl;
    private Boolean isEdited;
    private Boolean isLiked;
    private PostType postType;
    private EventPostStatusType eventPostStatusType;
    private int capacity;
    private int applicationCount;
    private LocalDateTime deadline;
    private Long likeCount;
    private Long commentCount;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostResponseDto(Post post, EventPost eventPost, Boolean isLiked) {
        this.id = post.getId();
        this.userId = post.getUser().getId();
        this.nickname = post.getUser().getStatus() == StatusType.DELETED ? "탈퇴한 사용자" : post.getUser().getNickname();
        this.title = post.getStatus() == StatusType.BLIND ? "숨김 처리된 게시글입니다." : post.getTitle();
        this.content = post.getStatus() == StatusType.BLIND ? "누적 신고 5회 이상으로 숨겨진 게시글입니다." : post.getContent();
        this.postImageUrl = post.getFile() == null ? null : FileUtils.toFullUrl(post.getFile().getPath());
        this.userImageUrl = post.getUser().getFile() == null ? null : FileUtils.toFullUrl(post.getUser().getFile().getPath());
        this.isEdited = post.getIsEdited();
        this.isLiked = isLiked;
        this.postType = eventPost == null ? PostType.GENERAL : PostType.MEETING;
        if (eventPost != null) {
            this.capacity = eventPost.getCapacity();
            this.applicationCount = eventPost.getApplicationCount();
            this.deadline = eventPost.getDeadline();
            this.eventPostStatusType = resolveEventPostStatus(eventPost);
        }
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }

    private EventPostStatusType resolveEventPostStatus(EventPost eventPost) {
        if (eventPost.isExpired()) return eventPostStatusType.EXPIRED;
        if (eventPost.isFull()) return eventPostStatusType.FULL;
        return eventPostStatusType.OPEN;
    }

}
