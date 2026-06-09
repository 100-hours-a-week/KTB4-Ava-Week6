package org.example.week4.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PostListResponseDto {
    private List<PostResponseDto> posts;
    private Pagination pagination;

    @Getter
    @AllArgsConstructor
    public static class Pagination {
        private long totalPosts;
        private Long nextCursorId;
        private boolean hasNext;
    }
}
