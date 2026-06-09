package org.example.week4.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.week4.auth.JwtProvider;
import org.example.week4.dto.CommentRequestDto;
import org.example.week4.dto.CommentResponseDto;
import org.example.week4.enums.ApiResultStatus;
import org.example.week4.response.ApiResponse;
import org.example.week4.service.CommentService;
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
    public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(HttpServletRequest httpServletRequest, @PathVariable Long postId, @Valid @RequestBody CommentRequestDto request) {
        Long userId = jwtProvider.getUserIdFromRequest(httpServletRequest);
        CommentResponseDto result = commentService.createComment(request, postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ApiResultStatus.SUCCESS, "create_comment_success", result));
    }

    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(HttpServletRequest httpRequest, @PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId, @Valid @RequestBody CommentRequestDto request) {
        Long userId = jwtProvider.getUserIdFromRequest(httpRequest);
        CommentResponseDto result = commentService.updateComment(postId, commentId, userId, request);
        return ResponseEntity.ok(ApiResponse.of(ApiResultStatus.SUCCESS, "update_comment_success", result));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> deleteComment(HttpServletRequest httpServletRequest, @PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        Long userId = jwtProvider.getUserIdFromRequest(httpServletRequest);
        commentService.deleteComment(postId, commentId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
