package org.ktb.week6.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class Report {

    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;

    public Report(Long id, Long postId, Long userId) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    public Report(Long id, Long postId, Long userId, String reason) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    public Report(
            Long id,
            Long postId,
            Long userId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.createdAt = createdAt;
    }
}
