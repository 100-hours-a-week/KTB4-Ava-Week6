package org.ktb.week6.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.dto.*;
import org.ktb.week6.enums.ApiResultStatus;
import org.ktb.week6.response.ApiResponse;
import org.ktb.week6.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponseDto>> getPosts(@RequestParam(required = false) Long cursorId) {
        PostListResponseDto result = postService.getPosts(cursorId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "posts_retrieved_success", result));
    }

    // 게시글 저장
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestPart(value = "file", required = false) MultipartFile file, @Valid @ModelAttribute PostRequestDto request) {
        PostResponseDto result = postService.createPost(userDetails.getId(), request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ApiResultStatus.SUCCESS, "create_post_success", result));
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> getPost(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long postId) {
        PostResponseDto result = postService.getPost(postId, userDetails.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "post_retrieved_success", result));
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> updatePost(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long postId, @RequestPart(value = "file", required = false) MultipartFile file, @Valid @ModelAttribute PostUpdateRequestDto request) {
        PostResponseDto result = postService.updatePost(userDetails.getId(), postId, request, file);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "post_updated_success", result));
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> deletePost(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long postId) {
        postService.deletePost(userDetails.getId(), postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 게시글 좋아요
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<PostResponseDto>> likePost(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long postId) {
        PostResponseDto result = postService.likePost(userDetails.getId(), postId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "post_like_toggle_success", result));
    }

    // 게시글 신고
    @PostMapping("/{postId}/report")
    public ResponseEntity<ApiResponse<PostResponseDto>> reportPost(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long postId, @Valid @RequestBody ReportRequestDto request) {
        System.out.println(request.getReason());
        PostResponseDto result = postService.reportPost(userDetails.getId(), postId, request.getReason());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ApiResultStatus.SUCCESS, "post_report_success", result));
    }

}
