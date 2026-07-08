package org.ktb.week6.dto;

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
        private Long nextCursorId;
        private boolean hasNext;
    }
}
