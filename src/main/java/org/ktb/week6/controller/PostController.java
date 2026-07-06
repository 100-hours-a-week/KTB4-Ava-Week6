package org.ktb.week6.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.Auth;
import org.ktb.week6.dto.*;
import org.ktb.week6.enums.ApiResultStatus;
import org.ktb.week6.jwt.JwtProvider;
import org.ktb.week6.response.ApiResponse;
import org.ktb.week6.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final JwtProvider jwtProvider;

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponseDto>> getPosts(@RequestParam(required = false) Long cursorId) {
        PostListResponseDto result = postService.getPosts(cursorId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "posts_retrieved_success", result));
    }

    // 게시글 저장
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(@Auth Long userId, @RequestPart(value = "file", required = false) MultipartFile file, @Valid @ModelAttribute PostRequestDto request) {
        PostResponseDto result = postService.createPost(userId, request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ApiResultStatus.SUCCESS, "create_post_success", result));
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> getPost(@Auth Long userId, @PathVariable Long postId) {
        PostResponseDto result = postService.getPost(postId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "post_retrieved_success", result));
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> updatePost(@Auth Long userId, @PathVariable Long postId, @RequestPart(value = "file", required = false) MultipartFile file, @Valid @ModelAttribute PostUpdateRequestDto request) {
        PostResponseDto result = postService.updatePost(userId, postId, request, file);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "post_updated_success", result));
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> deletePost(@Auth Long userId, @PathVariable Long postId) {
        postService.deletePost(userId, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 게시글 좋아요
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<PostResponseDto>> likePost(@Auth Long userId, @PathVariable Long postId) {
        PostResponseDto result = postService.likePost(userId, postId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "post_like_toggle_success", result));
    }

    // 게시글 신고
    @PostMapping("/{postId}/report")
    public ResponseEntity<ApiResponse<PostResponseDto>> reportPost(@Auth Long userId, @PathVariable Long postId, @Valid @RequestBody ReportRequestDto request) {
        System.out.println(request.getReason());
        PostResponseDto result = postService.reportPost(userId, postId, request.getReason());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ApiResultStatus.SUCCESS, "post_report_success", result));
    }

}
