package org.ktb.week6.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ktb.week6.entity.TemporaryPost;
import org.ktb.week6.enums.PostType;
import org.ktb.week6.utils.FileUtils;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TemporaryPostResponseDto {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private PostType type;
    private int capacity;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TemporaryPostResponseDto(TemporaryPost post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.imageUrl = post.getFile() == null ? null : FileUtils.toFullUrl(post.getFile().getPath());
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.type = post.getType();
        this.capacity = post.getCapacity();
        this.deadline = post.getDeadline();
    }
}
