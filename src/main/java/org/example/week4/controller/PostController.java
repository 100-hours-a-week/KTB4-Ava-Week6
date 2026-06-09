package org.example.week4.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.week4.auth.JwtProvider;
import org.example.week4.dto.PostListResponseDto;
import org.example.week4.dto.PostRequestDto;
import org.example.week4.dto.PostResponseDto;
import org.example.week4.dto.PostUpdateRequestDto;
import org.example.week4.enums.ApiResultStatus;
import org.example.week4.response.ApiResponse;
import org.example.week4.service.PostService;
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
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(HttpServletRequest httpServletRequest, @RequestPart(value = "image", required = false) MultipartFile image, @Valid @ModelAttribute PostRequestDto request) {
        Long userId = jwtProvider.getUserIdFromRequest(httpServletRequest);
        PostResponseDto result = postService.createPost(userId, request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ApiResultStatus.SUCCESS, "create_post_success", result));
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> getPost(@PathVariable Long postId) {
        PostResponseDto result = postService.getPost(postId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "post_retrieved_success", result));
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> updatePost(HttpServletRequest httpServletRequest, @PathVariable Long postId, @RequestPart(value = "image", required = false) MultipartFile image, @Valid @ModelAttribute PostUpdateRequestDto request) {
        Long userId = jwtProvider.getUserIdFromRequest(httpServletRequest);
        PostResponseDto result = postService.updatePost(userId, postId, request, image);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "post_updated_success", result));
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> deletePost(HttpServletRequest httpServletRequest, @PathVariable Long postId) {
        Long userId = jwtProvider.getUserIdFromRequest(httpServletRequest);
        postService.deletePost(userId, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 게시글 좋아요
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<PostResponseDto>> likePost(HttpServletRequest httpServletRequest, @PathVariable Long postId) {
        Long userId = jwtProvider.getUserIdFromRequest(httpServletRequest);
        PostResponseDto result = postService.likePost(userId, postId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "post_like_toggle_success", result));
    }

    // 게시글 신고
    @PostMapping("/{postId}/report")
    public ResponseEntity<ApiResponse<PostResponseDto>> reportPost(HttpServletRequest httpServletRequest, @PathVariable Long postId) {
        Long userId = jwtProvider.getUserIdFromRequest(httpServletRequest);
        PostResponseDto result = postService.reportPost(userId, postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ApiResultStatus.SUCCESS, "post_report_success", result));
    }

}
