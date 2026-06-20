package org.ktb.week6.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.Auth;
import org.ktb.week6.auth.JwtProvider;
import org.ktb.week6.dto.CommentRequestDto;
import org.ktb.week6.dto.CommentResponseDto;
import org.ktb.week6.enums.ApiResultStatus;
import org.ktb.week6.response.ApiResponse;
import org.ktb.week6.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final JwtProvider jwtProvider;

    // 댓글 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponseDto>>> getComments(@PathVariable("postId") Long postId) {
        List<CommentResponseDto> result = commentService.getComments(postId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "comments_retrieved_success", result));
    }

    // 댓글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(@Auth Long userId, @PathVariable Long postId, @Valid @RequestBody CommentRequestDto request) {

        CommentResponseDto result = commentService.createComment(request, postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ApiResultStatus.SUCCESS, "create_comment_success", result));
    }

    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(@Auth Long userId, @PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId, @Valid @RequestBody CommentRequestDto request) {
        CommentResponseDto result = commentService.updateComment(postId, commentId, userId, request);
        return ResponseEntity.ok(ApiResponse.of(ApiResultStatus.SUCCESS, "update_comment_success", result));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> deleteComment(@Auth Long userId, @PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        commentService.deleteComment(postId, commentId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
