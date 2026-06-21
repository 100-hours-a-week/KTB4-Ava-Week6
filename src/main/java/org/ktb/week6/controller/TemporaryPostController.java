package org.ktb.week6.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ktb.week6.Auth;
import org.ktb.week6.dto.TemporaryPostRequestDto;
import org.ktb.week6.dto.TemporaryPostResponseDto;
import org.ktb.week6.enums.ApiResultStatus;
import org.ktb.week6.response.ApiResponse;
import org.ktb.week6.service.TemporaryPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/temporary-posts")
@RequiredArgsConstructor
public class TemporaryPostController {

    private final TemporaryPostService temporaryPostService;

    @GetMapping
    public ResponseEntity<ApiResponse<TemporaryPostResponseDto>> getTemporaryPost(@Auth Long userId) {
        TemporaryPostResponseDto result = temporaryPostService.getTemporaryPost(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "temporary_post_retrieved_success", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TemporaryPostResponseDto>> createTemporaryPost(@Auth Long userId, @RequestPart(value = "image", required = false) MultipartFile image, @Valid @ModelAttribute TemporaryPostRequestDto request) {
        TemporaryPostResponseDto result = temporaryPostService.createTemporaryPost(userId, request, image);
        System.out.println(request.getTitle() + request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(ApiResultStatus.SUCCESS, "create_temporary_post_success", result));
    }

    @PatchMapping("/{temporaryId}")
    public ResponseEntity<ApiResponse<TemporaryPostResponseDto>> updateTemporaryPost(@Auth Long userId, @PathVariable("temporaryId") Long temporaryPostId, @RequestPart(value = "image", required = false) MultipartFile image, @Valid @ModelAttribute TemporaryPostRequestDto request) {
        TemporaryPostResponseDto result = temporaryPostService.updateTemporaryPost(userId, temporaryPostId, request, image);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(ApiResultStatus.SUCCESS, "temporary_post_updated_success", result));
    }
}
